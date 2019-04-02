package com.photon.phocafe

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.photon.phocafe.adapter.RestaurantAdapter
import com.photon.phocafe.model.Restaurant
import com.photon.phocafe.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_landing.*
import kotlinx.android.synthetic.main.activity_landing.view.*
import android.text.Spanned
import com.photon.phocafe.model.FilterEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by padmakumar_m on 10/30/2017.
 */
class MenuListFragment : Fragment(), RestaurantAdapter.OnRestaurantSelectedListener {
//     var text_current_search: TextView? = null
//    var text_current_sort_by: TextView? = null
//    lateinit var recycler_restaurants: RecyclerView

//    var view_empty: ViewGroup? = null
    private var mFirestore: FirebaseFirestore? = null
    var mQuery: Query? = null

    private var mFilterDialog: FilterDialogFragment? = null
    private var mAdapter: RestaurantAdapter? = null

    private var mViewModel: MainActivityViewModel? = null
    var v: View?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        v=inflater!!.inflate(R.layout.activity_landing, container, false)
        // Inflate the layout for this fragment
//        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        mFirestore = FirebaseFirestore.getInstance()

        // Get ${LIMIT} restaurants
        mQuery = mFirestore!!.collection("restaurants")
                /*.orderBy("Tea", Query.Direction.DESCENDING)*/
                .limit(LIMIT.toLong())

        // RecyclerView
        mAdapter = object : RestaurantAdapter(mQuery!!, this@MenuListFragment) {
            protected override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    v!!.recycler_restaurants.setVisibility(View.GONE)
                    view_empty!!.visibility = View.VISIBLE
                } else {
                    recycler_restaurants!!.setVisibility(View.VISIBLE)
                    view_empty!!.visibility = View.GONE
                }
            }

            protected override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
//                Snackbar.make(android.R.id.content,
//                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show()
            }
        }

        v!!.recycler_restaurants.setLayoutManager(LinearLayoutManager(this@MenuListFragment.activity))
        v!!.recycler_restaurants.setAdapter(mAdapter)

        // Filter Dialog
        mFilterDialog = FilterDialogFragment()

        v!!.filter_bar.setOnClickListener{onFilterClicked()}
        v!!.button_clear_filter.setOnClickListener{onClearFiterClicked() }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

    }


    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn()
            return
        }

        // Apply filters
        onFilter(mViewModel!!.filters)

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter!!.startListening()
        }
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        if (mAdapter != null) {
            mAdapter!!.stopListening()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            mViewModel!!.isSigningIn = false

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn()
            }
        }
    }

    private fun onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog!!.show(activity!!.supportFragmentManager, FilterDialogFragment.TAG)
//        mFilterDialog!!.show(childFragmentManager, FilterDialogFragment.TAG)
    }

    private fun onClearFiterClicked() {
        mFilterDialog!!.resetFilters()

        onFilter(Filters.getDefault())
    }

    override fun onRestaurantSelected(restaurant: DocumentSnapshot) {
        // Go to the details page for the selected restaurant
        val intent = Intent(activity, MenuDetailActivity::class.java)
        intent.putExtra(MenuDetailActivity.KEY_RESTAURANT_ID, restaurant.id)

        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
    }

      fun onFilter(filters: Filters) {
        // Construct query basic query
          if(mFirestore==null){
              mFirestore= FirebaseFirestore.getInstance()
          }
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
        text_current_search!!.text = filters.getSearchDescription(activity).toSpanned()
        text_current_sort_by!!.setText(context?.let { filters.getOrderDescription(it) })

        // Save filters
        mViewModel!!.filters = filters
    }

    fun String.toSpanned(): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            return Html.fromHtml(this)
        }
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFilterEvent(event: FilterEvent) {
    onFilter(event.filters)
    }

    companion object {
        private val TAG = "MainActivity"
        const val RC_SIGN_IN = 9001
        const val LIMIT = 50
        fun getLandingActivityIntent(context: Context): Intent {
            val intent = Intent(context, MenuListFragment::class.java)
            return intent

        }
    }
}