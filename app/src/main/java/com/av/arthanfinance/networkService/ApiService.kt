package com.av.arthanfinance.networkService

import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.applyLoan.model.*
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

    @POST("updateCustomerLoan")
    @Headers("Content-Type: application/json")
    fun updateCustomerLoan(@Body userData: JsonObject): Call<LoanProcessResponse>

    @POST("verifyCustomerKYCDocs")
    @Headers("Content-Type: application/json")
    fun verifyKYCDocs(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("verifyCustomerKYCDocsV2")
    @Headers("Content-Type: application/json")
    fun verifyKYCDocs2(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("uploadCustomerPan")
    @Headers("Content-Type: application/json")
    fun uploadCustomerPan(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("getCustomerKYCDetails")
    @Headers("Content-Type: application/json")
    fun getCustomerKYCDetails(@Body documentsData: JsonObject): Call<UserKYCDetailsResponse>

    @POST("getKYCData")
    @Headers("Content-Type: application/json")
    fun getCustomerDetails(@Body documentsData: JsonObject): Call<UserDetailsResponse>

    @POST("saveBankDetails")
    @Headers("Content-Type: application/json")
    fun uploadCustomerBankDetails(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("uploadCustomerAadhar")
    @Headers("Content-Type: application/json")
    fun uploadCustomerAadhar(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("saveCustomerDetails")
    @Headers("Content-Type: application/json")
    fun saveCustomerDetails(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("uploadCustomerImage")
    @Headers("Content-Type: application/json")
    fun uploadCustomerImage(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("verifyCustomerPan")
    @Headers("Content-Type: application/json")
    fun verifyCustomerPan(@Body documentsData: JsonObject): Call<AuthenticationResponse>

    @POST("updatePan")
    @Headers("Content-Type: application/json")
    fun updatePan(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("request")
    @Headers("Content-Type: application/json")
    fun request(
        @Header("Authorization") authHeader: String,
        @Body documentsData: JsonObject
    ): Call<DigioPanResponse>

    @POST("orders")
    @Headers("Content-Type: application/json")
    fun order(
        @Header("Authorization") authHeader: String,
        @Body documentsData: JsonObject
    ): Call<DigioPanResponse>

    @POST("response")
    @Headers("Content-Type: application/json")
    fun getDataFromDigiLocker(@Header("Authorization") authHeader: String): Call<DigilockerDataResponse>

    @POST("verify/bank_account")
    @Headers("Content-Type: application/json")
    fun verifyBank(
        @Header("Authorization") authHeader: String,
        @Body documentsData: JsonObject
    ): Call<BankDetilsResponse>

    @POST("kyc/v2/request")
    @Headers("Content-Type: application/json")
    fun createRequestDigilocker(
        @Header("Authorization") authHeader: String,
        @Body documentsData: JsonObject
    ): Call<DigilockerTokenResponse>

    @POST("updatePhoto")
    @Headers("Content-Type: application/json")
    fun updatePhoto(@Body documentsData: JsonObject): Call<LoanProcessResponse>

    @POST("updateAadhar")
    @Headers("Content-Type: application/json")
    fun updateAadhar(@Body aadharData: JsonObject): Call<LoanProcessResponse>

    @POST("updateAadharV2")
    @Headers("Content-Type: application/json")
    fun updateAadhar2(@Body aadharData: JsonObject): Call<LoanProcessResponse>

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
    fun setCustomerMPIN(@Body pinDetails: JsonObject): Call<CustomerHomeTabResponse>

    @POST("resetMpin")
    @Headers("Content-Type: application/json")
    fun resetMpin(@Body pinDetails: JsonObject): Call<GenericResponse>

    @POST("setCustomerBiometric")
    @Headers("Content-Type: application/json")
    fun setCustomerBiometric(@Body biometricDetails: JsonObject): Call<AuthenticationResponse>

    @POST("saveCustBusiness")
    @Headers("Content-Type: application/json")
    fun saveCustBusiness(@Body customerBusinessData: JsonObject): Call<AuthenticationResponse>

    @POST("gst-return-auth-advance")
    @Headers("Content-Type: application/json")
    fun gstReturnStatus(
        @Header("x-karza-key") authHeader: String,
        @Body documentsData: JsonObject
    ): Call<GstReturnResponse>

    @POST("saveCustReference")
    @Headers("Content-Type: application/json")
    fun saveCustReference(@Body custReferenceData: JsonObject): Call<LoanProcessResponse>

    @POST("verifyCustomerPin")
    @Headers("Content-Type: application/json")
    fun verifyCustomerPin(@Body custReferenceData: JsonObject): Call<CustomerHomeTabResponse>

    @POST("getCompletedScreens")
    @Headers("Content-Type: application/json")
    fun getCompletedScreens(@Body custReferenceData: JsonObject): Call<CompletedScreensResponse>

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

    @POST("response")
    @Headers("Content-Type: application/json")
    fun getDigilockerData(@Body customerId: JsonObject): Call<LoansResponse>

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
    fun getPRLoanDetails(
        @Query("loanId") loanId: String,
        @Query("applicantType") applicantType: String
    ): Call<LoanProcessResponse>


    @GET("getPRPanDetails")
    fun getPRPanDetails(
        @Query("loanId") loanId: String,
        @Query("applicantType") applicantType: String
    ): Call<LoanKYCDetailsResponse>

    @GET("getPRAadharDetails")
    fun getPRAadharDetails(
        @Query("loanId") loanId: String,
        @Query("applicantType") applicantType: String
    ): Call<LoanKYCDetailsResponse>

    @GET("getPRAddressDetails")
    fun getPRAddressDetails(
        @Query("loanId") loanId: String,
        @Query("applicantType") applicantType: String
    ): Call<LoanProcessResponse>

    @GET("getPRApplicantPhoto")
    fun getPRApplicantPhoto(
        @Query("loanId") loanId: String,
        @Query("applicantType") applicantType: String
    ): Call<LoanKYCDetailsResponse>

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

    @Multipart
    @POST("rest/file/upload")
    suspend fun uploadStatement(
        @Part file: MultipartBody.Part,
        @Part("loanId") loanId: String?,
        @Part("customerId") customerId: String?
    ): UploadStatementResponse?

    @POST("saveGstRequestId1")
    @Headers("Content-Type: application/json")
    fun saveGstRequestId1(@Body updateJson: JsonObject): Call<AuthenticationResponse>

    @POST("saveGstRequestId2")
    @Headers("Content-Type: application/json")
    fun saveGstRequestId2(@Body updateJson: JsonObject): Call<AuthenticationResponse>

    @POST("auth")
    @Headers("Content-Type: application/json")
    fun verifyUdyamAadhar(
        @Header("x-karza-key") authHeader: String,
        @Body udyamRequestData: JsonObject
    ): Call<UdyamDetailsResponse>

    @POST("updateStage")
    @Headers("Content-Type: application/json")
    fun updateStage(@Body updateJson: JsonObject): Call<AuthenticationResponse>

    @POST("getBank")
    @Headers("Content-Type: application/json")
    fun getBank(@Body updateJson: JsonObject): Call<Bank>

    @POST("getBranch")
    @Headers("Content-Type: application/json")
    fun getBranch(@Body updateJson: JsonObject): Call<Bank>

    @POST("getAAReferenceId")
    @Headers("Content-Type: application/json")
    fun getAAReferenceId(@Body updateJson: JsonObject): Call<AggregatorConsentResponse>

}