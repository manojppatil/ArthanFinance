package com.example.arthanfinance.networkService

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response


class AuthInterceptor(context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiY20iLCJpYXQiOjE2MTQxODE1NjZ9.eRyexqyI4eezdYXUsz6_4Eo1pYG2mOUp6teKE2XIzHLqoHK_LOTMixfCChM2z4osGOjWbOQ7D3pb1hCgUdXkBw")
        return chain.proceed(requestBuilder.build())
    }
}