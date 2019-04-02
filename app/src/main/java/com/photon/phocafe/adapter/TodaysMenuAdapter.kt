package com.photon.phocafe.adapter

import android.graphics.Typeface
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.photon.phocafe.R
import com.photon.phocafe.model.TodaysMenu
import kotlinx.android.synthetic.main.item_todays_menu.view.*

/**
 * Created by padmakumar_m on 11/2/2017.
 */
open class TodaysMenuAdapter(var menu: List<TodaysMenu>, val ctx: FragmentActivity?) : RecyclerView.Adapter<TodaysMenuAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return menu.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mTypeface = Typeface.createFromAsset(ctx?.getAssets(), "fonts/justaword.ttf")

        return TodaysMenuAdapter.ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_todays_menu, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menu.get(position))
    }

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(menu: TodaysMenu) {
            itemView!!.lbl_menu.setText(menu.name)
            itemView!!.lbl_price.setText(menu.price)

            itemView!!.lbl_menu.setTypeface(mTypeface)
            itemView!!.lbl_price.setTypeface(mTypeface)
            /*if (rating.timestamp != null) {
                itemView!!.rating_item_date.setText("11")
            }*/
        }
    }
    companion object {
        var mTypeface:Typeface?=null

    }
}
