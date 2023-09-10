package com.google.codelabs.findnearbyplacesar.model

import com.google.gson.annotations.SerializedName

class NearByListResponse : BaseResponse() {

    @SerializedName("nearBySearch")
    var locationList: ArrayList<NearPlacePlace>? = null
}