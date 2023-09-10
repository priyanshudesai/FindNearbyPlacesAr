package com.google.codelabs.findnearbyplacesar.model

import com.google.gson.annotations.SerializedName

data class NearPlacePlace(
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: MyGeometry,
    @SerializedName("vicinity") val address: String? = null,
    @SerializedName("photos") val photos: List<Photo>? = null,
    @SerializedName("opening_hours") val openingHours: OpeningHours? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("user_ratings_total") val userRatingsTotal: String? = null,
    var distanceInMeter: Double? = 0.0
)

data class MyGeometry(
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null
)

data class Photo(@SerializedName("photo_reference_actual") val image: String)

data class OpeningHours(@SerializedName("open_now") val openNow: Boolean? = null)