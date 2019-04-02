package com.photon.phocafe

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import com.google.firebase.firestore.Query
import com.photon.phocafe.model.Restaurant


/**
 * Created by padmakumar_m on 10/30/2017.
 */


class Filters {

    var category: String? = null
    var city: String? = null
    var price = -1
    var sortBy: String? = null
    var sortDirection: Query.Direction? = null

    fun hasCategory(): Boolean {
        return !TextUtils.isEmpty(category)
    }

    fun hasCity(): Boolean {
        return !TextUtils.isEmpty(city)
    }

    fun hasPrice(): Boolean {
        return price > 0
    }

    fun hasSortBy(): Boolean {
        return !TextUtils.isEmpty(sortBy)
    }

    fun getSearchDescription(context: FragmentActivity?): String {
        val desc = StringBuilder()

        if (category == null && city == null) {
            desc.append("<b>")
            desc.append(context!!.getString(R.string.all_restaurants))
            desc.append("</b>")
        }

        if (category != null) {
            desc.append("<b>")
            desc.append(category)
            desc.append("</b>")
        }

        if (category != null && city != null) {
            desc.append(" in ")
        }

        if (city != null) {
            desc.append("<b>")
            desc.append(city)
            desc.append("</b>")
        }

        if (price > 0) {
            desc.append(" for ")
            desc.append("<b>")
            desc.append(/*RestaurantUtil.getPriceString(price)*/"$$")
            desc.append("</b>")
        }

        return desc.toString()
    }

    fun getOrderDescription(context: Context): String {
        return if (Restaurant.FIELD_PRICE == sortBy) {
            context.getString(R.string.sorted_by_price)
        } else if (Restaurant.FIELD_POPULARITY == sortBy) {
            context.getString(R.string.sorted_by_popularity)
        } else {
            context.getString(R.string.sorted_by_rating)
        }
    }



    companion object {

        fun getDefault(): Filters {
            var filters = Filters()
            filters.sortBy= Restaurant.FIELD_AVG_RATING
            filters.sortDirection=Query.Direction.DESCENDING
            return filters
        }
    }
}