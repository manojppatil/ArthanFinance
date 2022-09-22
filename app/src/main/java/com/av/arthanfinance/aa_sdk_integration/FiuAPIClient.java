package com.av.arthanfinance.aa_sdk_integration;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class FiuAPIClient {

    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl("https://uat.moneyone.in/")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build();
    }
}