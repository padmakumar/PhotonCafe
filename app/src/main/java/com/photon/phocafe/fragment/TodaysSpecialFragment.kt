package com.photon.phocafe.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.photon.phocafe.R
import android.graphics.Typeface
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.photon.phocafe.adapter.TodaysMenuAdapter
import kotlinx.android.synthetic.main.activity_menu_detail.*
import kotlinx.android.synthetic.main.fragment_todays_special.view.*
import com.google.firebase.firestore.Query
import com.photon.phocafe.model.TodaysMenu


/**
 * A simple [Fragment] subclass.
 */
class TodaysSpecialFragment : Fragment() {

var mRootView: View?=null
    private var mFirestore: FirebaseFirestore? = null
    private var menuRef: DocumentReference? = null
//    private var mRestaurantRegistration: ListenerRegistration? = null
var listMenu:ArrayList<TodaysMenu>?=null

    var typeface:Typeface?=null
    var todaysMenu:TodaysMenu?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mRootView= inflater!!.inflate(R.layout.fragment_todays_special, container, false)
        typeface = Typeface.createFromAsset(activity?.getAssets(), "fonts/justaword.ttf")
        mRootView!!.text_view.setTypeface(typeface)

        listMenu= ArrayList()

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance()

        // Get reference to the restaurant
//        menuRef = mFirestore!!.collection("test_menu").document("menu_chart")

        // Get ratings
//        val ratingsQuery = menuRef!!.collection("test_menu").orderBy("price", Query.Direction.DESCENDING)
//                .limit(10)

//        val docRef = mFirestore!!.collection("test_menu").document()
        /*docRef.get().addOnSuccessListener(OnSuccessListener<DocumentSnapshot> {
            documentSnapshot ->
             todaysMenu = documentSnapshot.toObject<TodaysMenu>(TodaysMenu::class.java!!)
            Log.d("~~~"+todaysMenu?.name,"-todaysMenu-"+todaysMenu?.price)
            mRootView!!.text_view.setText(todaysMenu?.name+"\t"+todaysMenu?.price)
        })
*/

        getSpecial()
        return mRootView
    }

    override fun onResume() {
        super.onResume()

        if(specialItemCount==listMenu!!.size){

        }else{
            //Refresh collection
            getSpecial()
        }
    }

    fun getSpecial(){
        mFirestore!!.collection("test_menu")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        listMenu= ArrayList()
                        for (document in task.result!!) {
                            Log.d("TodaysSpl", document.id + " => " + document.data)
                            todaysMenu = document.toObject<TodaysMenu>(TodaysMenu::class.java!!)
                            listMenu!!.add(todaysMenu!!)

                        }
                        specialItemCount=listMenu!!.size
                        setTodaysMenuAdapter(listMenu!!)
                        Log.d("TodaysSpl", " => ~~~" + specialItemCount)
                        if(specialItemCount>0)
                        mRootView!!.text_view.setText(listMenu?.get(0)?.name+"\t"+todaysMenu?.price)
                    } else {
                        Log.d("TOdaysSpl", "Error getting documents: ", task.exception)
                    }
                };
    }

    fun setTodaysMenuAdapter(listItem:ArrayList<TodaysMenu>){
         // RecyclerView
      var todaysSpecialAdapter = object : TodaysMenuAdapter(listItem,activity) {
          protected fun onDataChanged() {
              if (getItemCount() === 0) {
                  recycler_ratings!!.visibility = View.GONE
                  view_empty_ratings!!.visibility = View.VISIBLE
              } else {
                  recycler_ratings!!.visibility = View.VISIBLE
                  view_empty_ratings!!.visibility = View.GONE
              }
          }
      }
      mRootView!!.rc_todays_spl.layoutManager = LinearLayoutManager(activity)
      mRootView!!.rc_todays_spl.adapter = todaysSpecialAdapter
    }

    companion object {
        var specialItemCount=0
    }

}