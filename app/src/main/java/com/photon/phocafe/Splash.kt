package com.photon.phocafe

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Splash : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        val handler = Handler()
        handler.postDelayed({
            if(isLoggedin()){
                var intent = Intent(this@Splash, MainActivity::class.java)
//                var intent = LandingActivity.getLandingActivityIntent(this@Splash)
                startActivity(intent)
                finish()
            }else {
                startActivity(Intent(this,StandardSignInActivity::class.java))
                finish()
            }
        }, DELAY_MILLIS.toLong())

    }

    private fun isLoggedin(): Boolean {
        var prefs: SharedPreferences? = this.getSharedPreferences(Constants.PREFS_FILENAME, 0)
       return prefs!!.getBoolean(Constants.IS_LOGGEDIN,false)

    }

    companion object {

        private val DELAY_MILLIS = 3000
    }
}
