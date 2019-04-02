package com.photon.phocafe

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import com.google.firebase.auth.FirebaseAuth
import com.photon.phocafe.model.Rating
import kotlinx.android.synthetic.main.dialog_rating.*
import kotlinx.android.synthetic.main.dialog_rating.view.*
import me.zhanghai.android.materialratingbar.MaterialRatingBar

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment() {

    private var mRatingListener: RatingListener? = null
    private var v: View? = null

    internal interface RatingListener {

        fun onRating(rating: Rating)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        v = inflater?.inflate(R.layout.dialog_rating, container, false)
        v!!.restaurant_form_button.setOnClickListener { onSubmitClicked() }
        v!!.restaurant_form_cancel.setOnClickListener { onCancelClicked() }
        return v
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is RatingListener) {
            mRatingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    fun onSubmitClicked() {
        val rating = Rating(
                FirebaseAuth.getInstance().currentUser!!,
                restaurant_form_rating!!.rating.toDouble(),
                restaurant_form_text!!.text.toString())

        if (mRatingListener != null) {
            mRatingListener!!.onRating(rating)
        }

        dismiss()
    }

    fun onCancelClicked() {
        dismiss()
    }

    companion object {

        val TAG = "RatingDialog"
    }
}
