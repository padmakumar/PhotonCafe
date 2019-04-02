package com.photon.phocafe

import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.firestore.*
import com.photon.phocafe.adapter.RestaurantAdapter
import com.photon.phocafe.model.Restaurant
import com.photon.phocafe.util.RatingUtil
import com.photon.phocafe.util.RestaurantUtil
import com.photon.phocafe.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_landing.*


/**
 * Created by padmakumar_m on 10/30/2017.
 */
class LandingActivity : AppCompatActivity(), RestaurantAdapter.OnRestaurantSelectedListener {

//     var text_current_search: TextView? = null
//    var text_current_sort_by: TextView? = null
//    lateinit var recycler_restaurants: RecyclerView

//    var view_empty: ViewGroup? = null
    var testRef: DocumentReference? =null

    private var mFirestore: FirebaseFirestore? = null
    var mQuery: Query? = null

    private var mFilterDialog: FilterDialogFragment? = null
    private var mAdapter: RestaurantAdapter? = null

    private var mViewModel: MainActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)
//        setSupportActionBar(toolbar)

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        mFirestore = FirebaseFirestore.getInstance()

        // Get ${LIMIT} restaurants
        mQuery = mFirestore!!.collection("restaurants")
                .orderBy("avgRating", Query.Direction.DESCENDING)
                .limit(LIMIT.toLong())

        testRef = mFirestore?.collection("test_menu")?.document("menu_chart")


        // RecyclerView
        mAdapter = object : RestaurantAdapter(mQuery!!, this@LandingActivity) {
            protected override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    recycler_restaurants!!.setVisibility(View.GONE)
                    view_empty!!.visibility = View.VISIBLE
                } else {
                    recycler_restaurants!!.setVisibility(View.VISIBLE)
                    view_empty!!.visibility = View.GONE
                }
            }

            protected override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Snackbar.make(findViewById<View>(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }
        }

        recycler_restaurants!!.setLayoutManager(LinearLayoutManager(this))
        recycler_restaurants!!.setAdapter(mAdapter)

        // Filter Dialog
        mFilterDialog = FilterDialogFragment()

        filter_bar.setOnClickListener{onFilterClicked()}
        button_clear_filter.setOnClickListener { onClearFilterClicked() }
    }

    public override fun onStart() {
        super.onStart()

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        }

        // Apply filters
//        onFilter(mViewModel!!.filters)

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter!!.startListening()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (mAdapter != null) {
            mAdapter!!.stopListening()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            mViewModel!!.isSigningIn = false

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
//            R.id.menu_add_items -> onAddItemsClicked()
            /*R.id.menu_sign_out -> {
                AuthUI.getInstance().signOut(this)
                startSignIn()
            }*/
            R.id.menu_delete -> RestaurantUtil.deleteAll()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog!!.show(supportFragmentManager, FilterDialogFragment.TAG)
    }

    private fun onClearFilterClicked() {
        mFilterDialog!!.resetFilters()

//        onFilter(Filters.getDefault())
    }

    override fun onRestaurantSelected(restaurant: DocumentSnapshot) {
        // Go to the details page for the selected restaurant
        val intent = Intent(this, MenuDetailActivity::class.java)
        intent.putExtra(MenuDetailActivity.KEY_RESTAURANT_ID, restaurant.id)

        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

      /*override fun onFilter(filters: Filters) {
        // Construct query basic query
        var query: Query = mFirestore!!.collection("restaurants")

        // Category (equality filter)
        if (filters!!.hasCategory()) {
            query = query.whereEqualTo(Restaurant.FIELD_CATEGORY, filters.category)
        }

        // City (equality filter)
        if (filters.hasCity()) {
            query = query.whereEqualTo(Restaurant.FIELD_CITY, filters.city)
        }

        // Price (equality filter)
        if (filters.hasPrice()) {
            query = query.whereEqualTo(Restaurant.FIELD_PRICE, filters.price)
        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
//            query = query.orderBy(filters.sortBy, filters.sortDirection)
        }

        // Limit items
        query = query.limit(LIMIT.toLong())

        // Update the query
        mAdapter!!.setQuery(query)

        // Set header
        text_current_search!!.text = filters.getSearchDescription(this)
        text_current_sort_by!!.setText(filters.getOrderDescription(this))

        // Save filters
        mViewModel!!.filters = filters
    }
*/
    private fun shouldStartSignIn(): Boolean {
        return !mViewModel!!.isSigningIn && FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        // Sign in with FirebaseUI
      /*  val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .build()

        startActivityForResult(intent, RC_SIGN_IN)*/
        mViewModel!!.isSigningIn = true
    }

    private fun onAddItemsClicked() {
        // Add a bunch of random restaurants
        var batch:WriteBatch? = mFirestore!!.batch()
        for (i in 0..9) {
            val restRef = mFirestore!!.collection("restaurants").document()

            // Create random restaurant / ratings
            val randomRestaurant = RestaurantUtil.getRandom(this)
            val randomRatings = RatingUtil.getRandomList(randomRestaurant.numRatings)
            randomRestaurant.avgRating=RatingUtil.getAverageRating(randomRatings)

            // Add restaurant
            batch?.set(restRef, randomRestaurant)

            // Add ratings to subcollection
            for (rating in randomRatings) {
                batch?.set(restRef.collection("ratings").document(), rating)
            }
        }

        batch?.commit()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Write batch succeeded.")
            } else {
                Log.w(TAG, "write batch failed.", task.exception)
            }
        }
    }

    companion object {

        private val TAG = "MainActivity"

        private val RC_SIGN_IN = 9001

        private val LIMIT = 50
        fun getLandingActivityIntent(context: Context): Intent {
            val intent = Intent(context, LandingActivity::class.java)
            return intent

        }
    }
}