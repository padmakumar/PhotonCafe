package com.photon.phocafe

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction

import com.photon.phocafe.adapter.RatingAdapter
import com.photon.phocafe.model.Rating
import com.photon.phocafe.model.Restaurant
import com.photon.phocafe.util.RestaurantUtil
import kotlinx.android.synthetic.main.activity_menu_detail.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class MenuDetailActivity : AppCompatActivity(), EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener {

    private var mRatingDialog: RatingDialogFragment? = null

    private var mFirestore: FirebaseFirestore? = null
    private var mRestaurantRef: DocumentReference? = null
    private var mRestaurantRegistration: ListenerRegistration? = null

    private var mRatingAdapter: RatingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_detail)

        // Get restaurant ID from extras
        val restaurantId = intent.extras.getString(KEY_RESTAURANT_ID) ?: throw IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID)

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance()

        // Get reference to the restaurant
        mRestaurantRef = mFirestore!!.collection("restaurants").document(restaurantId)

        // Get ratings
        val ratingsQuery = mRestaurantRef!!
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

        // RecyclerView
        mRatingAdapter = object : RatingAdapter(ratingsQuery) {
            protected override fun onDataChanged() {
                if (getItemCount() === 0) {
                    recycler_ratings!!.visibility = View.GONE
                    view_empty_ratings!!.visibility = View.VISIBLE
                } else {
                    recycler_ratings!!.visibility = View.VISIBLE
                    view_empty_ratings!!.visibility = View.GONE
                }
            }
        }
        recycler_ratings!!.layoutManager = LinearLayoutManager(this)
        recycler_ratings!!.adapter = mRatingAdapter

        mRatingDialog = RatingDialogFragment()

        restaurant_button_back.setOnClickListener { onBackArrowClicked() }
        fab_show_rating_dialog.setOnClickListener { onAddRatingClicked() }
    }

    public override fun onStart() {
        super.onStart()

        mRatingAdapter!!.startListening()
        mRestaurantRegistration = mRestaurantRef!!.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()

        mRatingAdapter!!.stopListening()

        if (mRestaurantRegistration != null) {
            mRestaurantRegistration!!.remove()
            mRestaurantRegistration = null
        }
    }

    override fun finish() {
        super.finish()
      //  overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    /**
     * Listener for the Restaurant document ([.mRestaurantRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e)
            return
        }

        onRestaurantLoaded(snapshot!!.toObject<Restaurant>(Restaurant::class.java)!!)
    }

    private fun onRestaurantLoaded(restaurant: Restaurant) {
        restaurant_name!!.setText(restaurant.name)
        restaurant_rating!!.rating = restaurant.avgRating.toFloat()
        restaurant_num_ratings!!.text = getString(R.string.fmt_num_ratings, restaurant.numRatings)
        restaurant_city!!.setText(restaurant.city)
        restaurant_category!!.setText(restaurant.category)
        restaurant_price!!.setText(RestaurantUtil.getPriceString(restaurant))

        // Background image
        Glide.with(restaurant_image!!.context)
                .load(restaurant.photo)
                .into(restaurant_image)
    }

    fun onBackArrowClicked() {
        onBackPressed()
    }


    fun onAddRatingClicked() {
        mRatingDialog!!.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mRestaurantRef, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Rating added")

                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    recycler_ratings!!.smoothScrollToPosition(0)
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "Add rating failed", e)

                    // Show failure message and hide keyboard
                    hideKeyboard()
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                            Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun addRating(restaurantRef: DocumentReference?, rating: Rating): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = restaurantRef!!.collection("ratings").document()

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore!!.runTransaction { transaction ->
            val restaurant = transaction.get(restaurantRef).toObject<Restaurant>(Restaurant::class.java)

            // Compute new number of ratings
            val newNumRatings = restaurant!!.numRatings + 1

            // Compute new average rating
            val oldRatingTotal = restaurant.avgRating * restaurant.numRatings
            val newAvgRating = (oldRatingTotal + rating.rating) / newNumRatings

            // Set new restaurant info
            restaurant.numRatings=newNumRatings
            restaurant.avgRating=newAvgRating

            // Commit to Firestore
            transaction.set(restaurantRef, restaurant)
            transaction.set(ratingRef, rating)

            null
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        private val TAG = "RestaurantDetail"

        val KEY_RESTAURANT_ID = "key_restaurant_id"
    }
}
