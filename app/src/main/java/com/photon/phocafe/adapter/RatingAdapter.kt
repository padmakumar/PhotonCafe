package com.photon.phocafe.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.firebase.firestore.Query
import com.photon.phocafe.R
import com.photon.phocafe.model.Rating
import kotlinx.android.synthetic.main.item_rating.view.*

import java.text.SimpleDateFormat
import java.util.Locale

import me.zhanghai.android.materialratingbar.MaterialRatingBar

/**
 * RecyclerView adapter for a list of [Rating].
 */
open class RatingAdapter(query: Query) : FirestoreAdapter<RatingAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_rating, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject<Rating>(Rating::class.java)!!)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(rating: Rating) {
            itemView.rating_item_name.setText(rating.userName)
            itemView.rating_item_rating.setRating(rating.rating.toFloat())
            itemView.rating_item_text.setText(rating.text)

            if (rating.timestamp != null) {
                itemView.rating_item_date.setText(FORMAT.format(rating.timestamp))
            }
        }

        companion object {

            private val FORMAT = SimpleDateFormat(
                    "MM/dd/yyyy", Locale.US)
        }
    }

}
