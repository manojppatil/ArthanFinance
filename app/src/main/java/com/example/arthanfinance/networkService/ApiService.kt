package com.example.arthanfinance.networkService

import com.example.arthanfinance.CustomerHomeTabResponse
import com.example.arthanfinance.applyLoan.*
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("applyLoan")
    @Headers("Content-Type: application/json")
    fun applyForLoan(@Body userData: JsonObject): Call<LoanProcessResponse>

    @POST("verifyCustomerKYCDocs")
    @Headers("Content-Type: application/json")
    fun verifyKYCDocs(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("updatePan")
    @Headers("Content-Type: application/json")
    fun updatePan(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("updatePhoto")
    @Headers("Content-Type: application/json")
    fun updatePhoto(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("updateAadhar")
    @Headers("Content-Type: application/json")
    fun updateAadhar(@Body aadharData: JsonObject): Call<LoanProcessResponse>

    @POST("updateAadharAddress")
    @Headers("Content-Type: application/json")
    fun updateAadharAddress(@Body aadharAddressData: JsonObject): Call<LoanProcessResponse>

    @POST("register")
    @Headers("Content-Type: application/json")
    fun registerCustomer(@Body customerDetails: JsonObject): Call<AuthenticationResponse>

    @POST("verifyRegistrationOTP")
    @Headers("Content-Type: application/json")
    fun verifyRegistrationOTP(@Body optDetails: JsonObject): Call<AuthenticationResponse>

    @POST("setCustomerPin")
    @Headers("Content-Type: application/json")
    fun setCustomerMPIN(@Body pinDetails: JsonObject): Call<AuthenticationResponse>

    @POST("setCustomerBiometric")
    @Headers("Content-Type: application/json")
    fun setCustomerBiometric(@Body biometricDetails: JsonObject): Call<AuthenticationResponse>

    @POST("saveCustBusiness")
    @Headers("Content-Type: application/json")
    fun saveCustBusiness(@Body customerBusinessData: JsonObject): Call<AuthenticationResponse>

    @POST("saveCustReference")
    @Headers("Content-Type: application/json")
    fun saveCustReference(@Body custReferenceData: JsonObject): Call<LoanProcessResponse>

    @POST("verifyCustomerPin")
    @Headers("Content-Type: application/json")
    fun verifyCustomerPin(@Body custReferenceData: JsonObject): Call<CustomerHomeTabResponse>

    @GET("getCategorySegment")
    @Headers("Content-Type: application/json")
    fun getCategorySegment(): Call<Categories>

    @POST("logout")
    @Headers("Content-Type: application/json")
    fun logOut(@Body userData: JsonObject): Call<AuthenticationResponse>

    @POST("getLoans")
    @Headers("Content-Type: application/json")
    fun getLoans(@Body customerId: JsonObject): Call<LoansResponse>

    @Multipart
    @POST("upload")
    fun uploadFile(@Part file: MultipartBody.Part, @Part("loanId") loanId: RequestBody, @Part("customerId") customerId: RequestBody): Call<FileUploadResponse>

    @POST("getEmiRoi")
    @Headers("Content-Type: application/json")
    fun getEmiRoi(@Body loanData: JsonObject): Call<LoanProcessResponse>

    @POST("getCustomerId")
    @Headers("Content-Type: application/json")
    fun getCustomerId(@Body coAppData: JsonObject): Call<LoanProcessResponse>

    @POST("getPaytmTransanctionUrl")
    @Headers("Content-Type: application/json")
    fun getPaytmTransanctionUrl(@Body loanInfo: JsonObject): Call<LoanProcessResponse>

}