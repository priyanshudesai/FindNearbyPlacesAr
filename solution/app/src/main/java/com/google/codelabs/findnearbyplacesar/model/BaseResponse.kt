package com.google.codelabs.findnearbyplacesar.model

import com.google.gson.annotations.SerializedName

open class BaseResponse {
    @SerializedName("status")
    var status: Boolean = false

    @SerializedName("message")
    var message: String = ""

    @SerializedName("request_id")
    var requestId: Int? = null

    @SerializedName("authentication")
    val authentication: Boolean = false
}