package com.google.codelabs.findnearbyplacesar.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val context: Context?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Basic YWRtaW46YWRtaW5AMTIz")
                .build()
        )
    }
}