package com.photon.phocafe.ui

import android.Manifest
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.photon.phocafe.R
import com.photon.phocafe.fragment.TodaysSpecialFragment.Companion.specialItemCount
import com.photon.phocafe.model.TodaysMenu
import kotlinx.android.synthetic.main.add_todays_spl.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class AddTodaysSplActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private var mFirestore: FirebaseFirestore? = null
    private var mImageRef: StorageReference? = null
    var mItemUrl: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_todays_spl)
        // Firestore
        mFirestore = FirebaseFirestore.getInstance()

        itemAdd.setOnClickListener { addItem() }
        selectImage.setOnClickListener { choosePhoto()/*startActivity(Intent(this,ImageActivity::class.java))*/ }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                val selectedImage = data!!.data
                uploadPhoto(selectedImage)
                selectImage.setImageURI(selectedImage)
            } else {
                Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE && EasyPermissions.hasPermissions(this, PERMS)) {
            choosePhoto()
        }
    }

    private fun uploadPhoto(uri: Uri) {
        // Reset UI
//        hideDownloadUI()
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        // Upload to Cloud Storage
        val uuid = UUID.randomUUID().toString()
        mImageRef = FirebaseStorage.getInstance("gs://phocafe-be388.appspot.com").getReference("phocafe/"+uuid)
//        mImageRef=mImageRef!!.child("phocafe")
        mImageRef!!.putFile(uri)
                .addOnSuccessListener(this) { taskSnapshot ->
                    Log.d(TAG, "uploadPhoto:onSuccess:" + taskSnapshot.metadata!!.reference!!.path)
                   /* Toast.makeText(this@AddTodaysSplActivity, "Image uploaded"+taskSnapshot.downloadUrl,
                            Toast.LENGTH_SHORT).show()
                */    mItemUrl= taskSnapshot.uploadSessionUri.toString()
//                    showDownloadUI()
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "uploadPhoto:onError", e)
                    Toast.makeText(this@AddTodaysSplActivity, "Upload failed",
                            Toast.LENGTH_SHORT).show()
                }
    }
    @AfterPermissionGranted(ImageActivity.RC_IMAGE_PERMS)
    protected fun choosePhoto() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rational_image_perm),
                    ImageActivity.RC_IMAGE_PERMS, PERMS)
            return
        }

        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RC_CHOOSE_PHOTO)
    }

    /**
     * Add the item to the fire store to display in todays menu page
     */
    private fun addItem() {

        // Add a bunch of random restaurants
        var batch: WriteBatch? = mFirestore!!.batch()
        /*for (i in 0..9) {
            val restRef = mFirestore!!.collection("restaurants").document()

            // Create random restaurant / ratings
            val randomRestaurant = RestaurantUtil.getRandom(this)
            val randomRatings = RatingUtil.getRandomList(randomRestaurant.numRatings)
            randomRestaurant.avgRating= RatingUtil.getAverageRating(randomRatings)

            // Add restaurant
            batch?.set(restRef, randomRestaurant)

            // Add ratings to subcollection
            for (rating in randomRatings) {
                batch?.set(restRef.collection("ratings").document(), rating)
            }
        }*/

        specialItemCount=specialItemCount+1

        val todaysMnuRef =mFirestore!!.collection("test_menu").document(""+specialItemCount)

        val tdMnu = TodaysMenu(itemPrice.text.toString(),itemName.text.toString(),mItemUrl)

        // Add todays special
        batch?.set(todaysMnuRef, tdMnu)

        batch?.commit()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TodaysSpecial", "Write batch succeeded.")
//                Snackbar.make(findViewById(android.R.id.content),"Added Successful",Snackbar.LENGTH_SHORT).show()
                finish()
            } else {
                Log.w("TodaysSpecial", "write batch failed.", task.exception)
                Snackbar.make(findViewById(android.R.id.content),"Failed to add",Snackbar.LENGTH_SHORT).show()

            }
        }

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

        private val TAG = "AddTodaysSpecial"
        private val RC_CHOOSE_PHOTO = 101
        const val RC_IMAGE_PERMS = 102
        private val PERMS = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
