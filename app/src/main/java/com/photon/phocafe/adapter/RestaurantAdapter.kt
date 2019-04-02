package com.photon.phocafe.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.photon.phocafe.R
import com.photon.phocafe.model.Restaurant
import kotlinx.android.synthetic.main.item_restaurant.view.*

/**
 * RecyclerView adapter for a list of Restaurants.
 */
open class RestaurantAdapter(query: Query, val mListener: OnRestaurantSelectedListener) : FirestoreAdapter<RestaurantAdapter.ViewHolder>(query) {

    interface OnRestaurantSelectedListener {

        fun onRestaurantSelected(restaurant: DocumentSnapshot)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        return ViewHolder(inflater.inflate(R.layout.item_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bind(getSnapshot(position), mListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

//        @BindView(R.id.restaurant_item_rating)
//        var ratingBar: MaterialRatingBar? = null
        fun bind(snapshot: DocumentSnapshot,
                 listener: OnRestaurantSelectedListener?) {

            val restaurant = snapshot.toObject<Restaurant>(Restaurant::class.java!!)
            val resources = itemView.resources

            // Load image
            Glide.with(itemView.restaurant_item_image!!.context)
                    .load(restaurant?.photo)
                    .into(itemView.restaurant_item_image)

    itemView.restaurant_item_name!!.setText(restaurant?.name)
//            ratingBar!!.setRating(restaurant.getAvgRating() as Float)
    itemView.restaurant_item_city!!.setText(restaurant?.city)
    itemView.restaurant_item_category?.setText(restaurant?.category)
    itemView.restaurant_item_num_ratings!!.text = resources.getString(R.string.fmt_num_ratings,
                    restaurant?.numRatings)
    itemView.restaurant_item_price!!.setText("\u20B9"+restaurant?.price)

            // Click listener
            itemView.setOnClickListener {
                listener?.onRestaurantSelected(snapshot)
            }
        }

    }
}
