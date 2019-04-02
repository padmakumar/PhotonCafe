package com.photon.phocafe


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.photon.phocafe.Constants.PREFS_FILENAME
import kotlinx.android.synthetic.main.login_activity.*
import com.google.firebase.auth.FirebaseUser
import android.util.Log


class StandardSignInActivity : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        login.setOnClickListener { login() }
        clear.setOnClickListener { resetFields() }
        create.setOnClickListener { create() }
        mAuth = FirebaseAuth.getInstance()
    }

    private fun resetFields() {
        usernameField.setText("")
        passwordField.setText("")
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.getCurrentUser()
        //updateUI(currentUser)
    }
    /**
     * Emulates a craete new user action.
     */
    private fun create() {
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()
        createAccount(username, password)
    }
    /**
     * Emulates a login action.
     */
    private fun login() {
        val username = usernameField.text.toString()
        val password = passwordField.text.toString()
        doLogin(username, password)
//        val valid = isValidCredentials(username, password)
       /* if (valid) {

        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }*/
    }


    /**
     * save the user details
     */
    private fun saveCredentials(username: String, isVerifiedUser: Boolean) {
        var prefs: SharedPreferences? = this.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putBoolean(Constants.IS_LOGGEDIN, true)
        editor.putString(Constants.USER_NAME, username)
        editor.putString(Constants.PASSWORD, "******")
        editor.putBoolean(Constants.IS_USER_VERFIED, isVerifiedUser)
        editor.apply()
    }

    /**
     * Dummy implementation for demo purposes. A real service should use secure mechanisms to
     * authenticate users.
     */
    fun isValidCredentials(username: String, password: String): Boolean {
        /* mAuth!!.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth?.getCurrentUser()
                        Log.d("StandardSignInActivity", "createUserWithEmail:success"+user?.photoUrl)
                        //  updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FAiled", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@StandardSignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                       // updateUI(null)
                    }


                }*/

        return username == password
    }

    fun doLogin(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("~~Success", "signInWithEmail:success")
                        val user = mAuth?.getCurrentUser()
                        //updateUI(user)
                        goLanding(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("~~Failed", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@StandardSignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        //   updateUI(null)
                    }
                }
    }

    private fun goLanding(user: FirebaseUser?) {
        saveCredentials(user?.email!!, user.isEmailVerified)
//        var intent = LandingActivity.getLandingActivityIntent(this@StandardSignInActivity)
        var intent = Intent(this@StandardSignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createAccount(email: String, password: String) {
        Log.d("Signin", "createAccount:" + email)
       /* if (!validateForm()) {
            return
        }*/
//        showProgressDialog()

        // [START create_user_with_email]
        mAuth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Signin", "createUserWithEmail:success")
                        val user = mAuth?.getCurrentUser()
//                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Signin", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@StandardSignInActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
//                        updateUI(null)
                    }

                    // [START_EXCLUDE]
//                    hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END create_user_with_email]
    }

    companion object {

        fun getStartActivityIntent(context: Context): Intent {
            val intent = Intent(context, StandardSignInActivity::class.java)
            return intent
        }
    }
}