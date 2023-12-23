package com.av.arthanfinance.aa_sdk_integration;

import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface FiuAPIInterface {

    @POST("finpro_uat/v2/requestconsent")
    Call<InitiateConsentResponse> createConsent(@Body InitiateConsentRequest vuaBody, @HeaderMap Map<String, String> headers);

    @GET("web/fetch/json")
    Call<JsonArray> getData(@QueryMap HashMap<String, String> queryMap, @HeaderMap Map<String, String> headers);

}
