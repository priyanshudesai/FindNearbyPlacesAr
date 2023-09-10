// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.findnearbyplacesar.api

import android.content.Context
import com.google.codelabs.findnearbyplacesar.BuildConfig
import com.google.codelabs.findnearbyplacesar.model.NearByListResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface definition for a service that interacts with the Places API.
 *
 * @see [Place Search](https://developers.google.com/places/web-service/search)
 */
interface PlacesService {

    @GET("nearbysearch/json")
    fun nearbyPlaces(
        @Query("key") apiKey: String,
        @Query("location") location: String,
        @Query("radius") radiusInMeters: Int,
        @Query("type") placeType: String
    ): Call<NearbyPlacesResponse>

    @FormUrlEncoded
    @POST("user/near_by_search")
    fun getNearByPlaces(
        @Field("access_token") accessToken: String,
        @Field("type") type: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double
    ): Call<NearByListResponse>

    companion object {
//        private const val ROOT_URL = "https://maps.googleapis.com/maps/api/place/"

        private const val COMMON_URL = "https://lifenine.in/apis/"
        /*private const val COMMON_URL = "https://lifenine.in/staging/"*/
        const val ROOT_URL = COMMON_URL + "api/v2/"

//        fun create(): PlacesService {
//            val logger = HttpLoggingInterceptor()
//            logger.level = HttpLoggingInterceptor.Level.BODY
//            val okHttpClient = OkHttpClient.Builder()
//                .addInterceptor(logger)
//                .build()
//            val converterFactory = GsonConverterFactory.create()
//            val retrofit = Retrofit.Builder()
//                .baseUrl(ROOT_URL)
//                .client(okHttpClient)
//                .addConverterFactory(converterFactory)
//                .build()
//            return retrofit.create(PlacesService::class.java)


            fun getMyBase(context: Context?): PlacesService {
//                if (retrofit == null) {
                    val httpClient = OkHttpClient.Builder().apply {
                        addInterceptor(HeaderInterceptor(context))
                        if (BuildConfig.DEBUG) {
                            val logger = HttpLoggingInterceptor()
                            logger.setLevel(HttpLoggingInterceptor.Level.BODY)
                            addInterceptor(logger)
                        }
                    }.build()
                        val retrofit = Retrofit.Builder()
                        .baseUrl(ROOT_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient)
                        .build()
//                }
                return retrofit.create(PlacesService::class.java)
            }
//        }
    }
}