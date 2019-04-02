package com.photon.phocafe.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.photon.phocafe.R
import kotlinx.android.synthetic.main.activity_image.*

import java.util.Collections
import java.util.UUID

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class ImageActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private var mImageRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        // By default, Cloud Storage files require authentication to read or write.
        // For this sample to function correctly, enable Anonymous Auth in the Firebase console:
        // https://console.firebase.google.com/project/_/authentication/providers
        /*  FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnCompleteListener(new SignInResultNotifier(this));*/

        button_choose_photo!!.setOnClickListener { choosePhoto() }
        button_download_direct!!.setOnClickListener { downloadDirect() }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                val selectedImage = data!!.data
                uploadPhoto(selectedImage)
            } else {
                Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && EasyPermissions.hasPermissions(this, PERMS)) {
            choosePhoto()
        }
    }

    //    @OnClick(R.id.button_choose_photo)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    protected fun choosePhoto() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_image_perm),
                    RC_IMAGE_PERMS, PERMS)
            return
        }

        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RC_CHOOSE_PHOTO)
    }

    private fun uploadPhoto(uri: Uri) {
        // Reset UI
        hideDownloadUI()
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        // Upload to Cloud Storage
        val uuid = UUID.randomUUID().toString()
        mImageRef = FirebaseStorage.getInstance("gs://phocafe-be388.appspot.com").getReference("phocafe/"+uuid)
//        mImageRef=mImageRef!!.child("phocafe")
        mImageRef!!.putFile(uri)
                .addOnSuccessListener(this) { taskSnapshot ->
                    Log.d(TAG, "uploadPhoto:onSuccess:" + taskSnapshot.metadata!!.reference!!.path)
                 /*   Toast.makeText(this@ImageActivity, "Image uploaded"+taskSnapshot.downloadUrl,
                            Toast.LENGTH_SHORT).show()
*/
                    showDownloadUI()
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "uploadPhoto:onError", e)
                    Toast.makeText(this@ImageActivity, "Upload failed",
                            Toast.LENGTH_SHORT).show()
                }
    }

    //    @OnClick(R.id.button_download_direct)
    protected fun downloadDirect() {
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        GlideApp.with(this)
                .load(mImageRef)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(first_image!!)
    }

    private fun hideDownloadUI() {
        button_download_direct!!.isEnabled = false

        first_image!!.setImageResource(0)
        first_image!!.visibility = View.INVISIBLE
    }

    private fun showDownloadUI() {
        button_download_direct!!.isEnabled = true

        first_image!!.visibility = View.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // See #choosePhoto with @AfterPermissionGranted
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,
                listOf(PERMS))) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    companion object {

        private val TAG = "ImageDemo"
        private val RC_CHOOSE_PHOTO = 101
         const val RC_IMAGE_PERMS = 102
        private val PERMS = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
