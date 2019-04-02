package com.photon.phocafe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.firestore.WriteBatch
import com.photon.phocafe.fragment.BlankFragment
import com.photon.phocafe.fragment.TodaysSpecialFragment
import com.photon.phocafe.ui.AddTodaysSplActivity
import com.photon.phocafe.util.RatingUtil
import com.photon.phocafe.util.RestaurantUtil
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.photon.phocafe.ui.AddNewMenuActivity


class MainActivity : AppCompatActivity() {
    /*  override fun onFilter(filters: Filters) {
        var menuListFra = (MenuListFragment())
         getSupportFragmentManager().findFragmentById(R.id.rootLayout)

         if (menuListFra != null) {
             menuListFra.onFilter(filters)

         }
     }
 */

    private var mFirestore: FirebaseFirestore? = null
    private var mRestaurantRef: DocumentReference? = null

    // [START declare_auth]
    private var  mAuth: FirebaseAuth? = null
    // [END declare_auth]

    // [START on_start_check_user]
     override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth?.getCurrentUser()
        //updateUI(currentUser)
    }
    // [END on_start_check_user]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        mFirestore = FirebaseFirestore.getInstance()

        setupNavigationView()
        fab_upload.setOnClickListener {
            if (fab_upload.tag.equals("menu")) {
                startActivity(Intent(this, AddNewMenuActivity::class.java))
            } else {
                startActivity(Intent(this, AddTodaysSplActivity::class.java))
            }
        }

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]

    }

    private fun signOut() {
        mAuth?.signOut()
        var prefs: SharedPreferences? = this.getSharedPreferences(Constants.PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putBoolean(Constants.IS_LOGGEDIN, false)
        editor.apply()
        //updateUI(null)
    }

    private fun setupNavigationView() {
        if (bottom_navigation != null) {
            bottom_navigation!!.setBackgroundColor(Color.WHITE)

            // Select first menu item by default and show Fragment accordingly.
            val menu = bottom_navigation.menu
            selectFragment(menu.getItem(0))

            // Set action to perform when any menu-item is selected.
            bottom_navigation.setOnNavigationItemSelectedListener(
                    object : BottomNavigationView.OnNavigationItemSelectedListener {
                        override fun onNavigationItemSelected(item: MenuItem): Boolean {
                            selectFragment(item)
                            return false
                        }
                    })
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
//            R.id.menu_add_items -> onAddItemsClicked()
        R.id.menu_sign_out -> {
            signOut()
            startSignIn()
            finish()
        }
            R.id.menu_delete -> RestaurantUtil.deleteAll()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * go to sign in page
     */
    private fun startSignIn() {
       startActivity(Intent(this,StandardSignInActivity::class.java))
    }


    private fun onAddItemsClicked() {
        // Add a bunch of random restaurants
        var batch: WriteBatch? = mFirestore!!.batch()
        for (i in 0..9) {
            val restRef = mFirestore!!.collection("restaurants").document()

            // Create random restaurant / ratings
            val randomRestaurant = RestaurantUtil.getRandom(this)
//            val randomRatings = RatingUtil.getRandomList(randomRestaurant.numRatings)
//            randomRestaurant.avgRating= RatingUtil.getAverageRating(randomRatings)

            // Add restaurant
            batch?.set(restRef, randomRestaurant)

           /* // Add ratings to subcollection
            for (rating in randomRatings) {
                batch?.set(restRef.collection("ratings").document(), rating)
            }*/
        }

        batch?.commit()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Write batch succeeded.")
                Snackbar.make(findViewById(android.R.id.content),"Added Successful", Snackbar.LENGTH_SHORT).show()

            } else {
                Log.w(TAG, "write batch failed.", task.exception)
                Snackbar.make(findViewById(android.R.id.content),"Failed to add",Snackbar.LENGTH_SHORT).show()

            }
        }
    }

    /**
     * Perform action when any item is selected.
     *
     * @param item Item that is selected.
     */
    protected fun selectFragment(item: MenuItem) {

        item.isChecked = true

        when (item.itemId) {
            R.id.action_home -> {
                // Action to perform when Home Menu item is selected.
                pushFragment(MenuListFragment())
                fab_upload.setTag("menu")
                fab_upload.visibility= View.VISIBLE
            }
            R.id.action_todays_menu -> {

        // Action to perform when todays_menu Menu item is selected.
            pushFragment(TodaysSpecialFragment())
                fab_upload.setTag("todays_special")
                fab_upload.visibility= View.VISIBLE
        }
            R.id.action_account -> {
                // Action to perform when Account Menu item is selected.
                pushFragment(BlankFragment())

                fab_upload.visibility= View.INVISIBLE
            }
        }
    }

    /**
     * Method to push any fragment into given id.
     *
     * @param fragment An instance of Fragment to show into the given id.
     */
    protected fun pushFragment(fragment: Fragment?) {
        if (fragment == null)
            return

        val fragmentManager = supportFragmentManager
        if (fragmentManager != null) {
            val ft = fragmentManager.beginTransaction()
            if (ft != null) {
                ft.replace(R.id.rootLayout, fragment)
                ft.commit()
            }
        }
    }

    companion object {
        private val TAG = "MainActivity"

        fun getStartActivityIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            return intent

        }
    }
}
