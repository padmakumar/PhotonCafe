package com.photon.phocafe.model

import android.text.TextUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Model POJO for a Todays special menu.
 */
class TodaysMenu {

//    var userId: String? = null
//    var userName: String? = null
    var price: String = "0.0"
    var name: String? = null
    var photo: String? = null

    constructor() {}

    constructor(/*user: FirebaseUser,*/ price: String, text: String,photo:String?) {
//        this.userId = user.uid
//        this.userName = user.displayName
//        if (TextUtils.isEmpty(this.userName)) {
//            this.userName = user.email
//        }

        this.price = price
        this.name = text
        this.photo = photo
    }
}