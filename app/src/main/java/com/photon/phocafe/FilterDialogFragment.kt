package com.photon.phocafe

import android.content.Context
import android.view.ViewGroup
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.firestore.Query
import com.photon.phocafe.model.FilterEvent
import com.photon.phocafe.model.Restaurant
import kotlinx.android.synthetic.main.dialog_filters.*
import kotlinx.android.synthetic.main.dialog_filters.view.*
import org.greenrobot.eventbus.EventBus


/**
 * Created by padmakumar_m on 10/30/2017.
 */
/**
 * Dialog Fragment containing filter form.
 */
class FilterDialogFragment : DialogFragment() {

    private var mRootView: View? = null

    private val selectedCategory: String?
        get() {
            val selected = spinner_category!!.selectedItem as String
            return if (getString(R.string.value_any_category).equals(selected)) {
                null
            } else {
                selected
            }
        }

    private val selectedCity: String?
        get() {
            val selected = spinner_city!!.selectedItem as String
            return if (getString(R.string.value_any_city).equals(selected)) {
                null
            } else {
                selected
            }
        }

    private val selectedPrice: Int
        get() {
            val selected = spinner_price!!.selectedItem as String
            return if (selected == getString(R.string.price_1)) {
                1
            } else if (selected == getString(R.string.price_2)) {
                2
            } else if (selected == getString(R.string.price_3)) {
                3
            } else {
                -1
            }
        }

    private val selectedSortBy: String?
        get() {
            val selected = spinner_sort!!.selectedItem as String
            if (getString(R.string.sort_by_rating).equals(selected)) {
                return Restaurant.FIELD_AVG_RATING
            }
            if (getString(R.string.sort_by_price).equals(selected)) {
                return Restaurant.FIELD_PRICE
            }
            return if (getString(R.string.sort_by_popularity).equals(selected)) {
                Restaurant.FIELD_POPULARITY
            } else null

        }

    private val sortDirection: Query.Direction?
        get() {
            val selected = spinner_sort!!.selectedItem as String
            if (getString(R.string.sort_by_rating).equals(selected)) {
                return Query.Direction.DESCENDING
            }
            if (getString(R.string.sort_by_price).equals(selected)) {
                return Query.Direction.ASCENDING
            }
            return if (getString(R.string.sort_by_popularity).equals(selected)) {
                Query.Direction.DESCENDING
            } else null

        }

    val filters: Filters
        get() {
            val filters = Filters()

            if (mRootView != null) {
                filters.category=selectedCategory
                filters.city=selectedCity
                //filters.price=selectedPrice
                filters.sortBy=selectedSortBy
                filters.sortDirection=sortDirection
            }

            return filters
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater?.inflate(R.layout.dialog_filters, container, false)

        mRootView!!.button_search.setOnClickListener { onSearchClicked() }
        mRootView!!.button_cancel.setOnClickListener { onCancelClicked() }

        return mRootView
    }


    override fun onResume() {
        super.onResume()
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }


    fun onSearchClicked() {
        EventBus.getDefault().post(FilterEvent(filters))

        dismiss()
    }

    fun onCancelClicked() {
        dismiss()
    }

    fun resetFilters() {
        if (mRootView != null) {
            mRootView!!.spinner_category.setSelection(0)
            mRootView!!.spinner_city.setSelection(0)
            mRootView!!.spinner_price.setSelection(0)
            mRootView!!.spinner_sort.setSelection(0)
        }
    }

    companion object {
        val TAG = "FilterDialog"
    }
}