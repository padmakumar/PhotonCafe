package com.photon.phocafe.model

/**
 * Created by padmakumar_m on 10/30/2017.
 */
class Restaurant {

    var name: String? = null
    var city: String? = null
    var category: String? = null
    var photo: String? = null
    var price: Int = 0
    var numRatings: Int = 0
    var avgRating: Double = 0.toDouble()

    constructor() {}

    constructor(name: String, city: String, category: String, photo: String,
                price: Int, numRatings: Int, avgRating: Double) {
        this.name = name
        this.city = city
        this.category = category
        this.price = price
        this.photo=photo
        this.numRatings = numRatings
        this.avgRating = avgRating
    }

    companion object {

        val FIELD_CITY = "city"
        val FIELD_CATEGORY = "category"
        val FIELD_PRICE = "price"
        val FIELD_POPULARITY = "numRatings"
        val FIELD_AVG_RATING = "avgRating"
    }
}