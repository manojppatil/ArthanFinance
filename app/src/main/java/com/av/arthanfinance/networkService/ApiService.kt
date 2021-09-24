package com.av.arthanfinance.networkService

import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.applyLoan.model.BusinessTypesResponse
import com.av.arthanfinance.applyLoan.model.CustomerReferenceResponse
import com.av.arthanfinance.applyLoan.model.GenericResponse
import com.av.arthanfinance.models.BusinessDetails
import com.av.arthanfinance.models.CustomerBankDetailsResponse
import com.av.arthanfinance.models.CustomerTotalLoansResponse
import com.av.arthanfinance.profile.ProfileResponse
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

    /*@POST("verifyKYCDocs")
    @Headers("Content-Type: application/json")
    fun verifyKYCDocs(@Body documentsData: JsonObject): Call<LoanProcessResponse>*/

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

    @POST("sendOTP")
    @Headers("Content-Type: application/json")
    fun sendOTP(@Body customerDetails: JsonObject): Call<OtpResponse>

    @POST("verifyOTPforCustomer")
    @Headers("Content-Type: application/json")
    fun verifyOTPForCustomer(@Body optDetails: JsonObject): Call<AuthenticationResponse>

    @POST("setCustomerPin")
    @Headers("Content-Type: application/json")
    fun setCustomerMPIN(@Body pinDetails: JsonObject): Call<AuthenticationResponse>

    @POST("resetMpin")
    @Headers("Content-Type: application/json")
    fun resetMpin(@Body pinDetails: JsonObject): Call<GenericResponse>

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

    @POST("forgotMpin")
    @Headers("Content-Type: application/json")
    fun forgotMPin(@Body custReferenceData: JsonObject): Call<GenericResponse>

    @POST("verifyOTP2")
    @Headers("Content-Type: application/json")
    fun verifyOTP(@Body custReferenceData: JsonObject): Call<GenericResponse>

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
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("loanId") loanId: RequestBody,
        @Part("customerId") customerId: RequestBody
    ): Call<FileUploadResponse>

    @POST("getEmiRoi")
    @Headers("Content-Type: application/json")
    fun getEmiRoi(@Body loanData: JsonObject): Call<LoanProcessResponse>

    @POST("getCustomerId")
    @Headers("Content-Type: application/json")
    fun getCustomerId(@Body coAppData: JsonObject): Call<LoanProcessResponse>

    @POST("getPaytmTransanctionUrl")
    @Headers("Content-Type: application/json")
    fun getPaytmTransanctionUrl(@Body loanInfo: JsonObject): Call<LoanProcessResponse>

    @GET("getBusinessPurpose")
    @Headers("Content-Type: application/json")
    fun getBusinnessPurposeTypes(): Call<BusinessTypesResponse>

    @GET("getPRLoanDetails")
    fun getPRLoanDetails(@Query("loanId") loanId: String,@Query("applicantType")applicantType:String): Call<LoanProcessResponse>

    @GET("getPRPanDetails")
    fun getPRPanDetails(@Query("loanId") loanId: String,@Query("applicantType")applicantType:String): Call<LoanKYCDetailsResponse>

    @GET("getPRAadharDetails")
    fun getPRAadharDetails(@Query("loanId") loanId: String,@Query("applicantType")applicantType:String): Call<LoanKYCDetailsResponse>

    @GET("getPRAddressDetails")
    fun getPRAddressDetails(@Query("loanId") loanId: String,@Query("applicantType")applicantType:String): Call<LoanProcessResponse>

    @GET("getPRApplicantPhoto")
    fun getPRApplicantPhoto(@Query("loanId") loanId: String,@Query("applicantType")applicantType:String): Call<LoanKYCDetailsResponse>

    @GET("getPRBusinessDetails")
    fun getPRBusinessDetails(@Query("loanId") loanId: String): Call<BusinessDetails>

    @GET("getPRReferenceDetails")
    fun getPRReferenceDetails(@Query("loanId") loanId: String): Call<CustomerReferenceResponse>

    @GET("getCustomerLoanCnt")
    fun getCustomerLoanCnt(@Query("customerId") customerId: String): Call<CustomerTotalLoansResponse>

    @POST("saveCustBank")
    @Headers("Content-Type: application/json")
    fun saveCustomerBankDetails(@Body loanInfo: JsonObject): Call<LoanProcessResponse>

    @GET("getProfile")
    fun getProfile(@Query("mobileNo") mobileNo: String): Call<ProfileResponse>

    @GET("getPRBankDetails")
    fun getPRBankDetails(@Query("loanId") loanId: String): Call<CustomerBankDetailsResponse>

    @POST("updateProfile")
    @Headers("Content-Type: application/json")
    fun updateProfile(@Body updateJson: JsonObject): Call<AuthenticationResponse>

}