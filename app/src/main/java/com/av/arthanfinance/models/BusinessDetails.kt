package com.av.arthanfinance.models


import com.google.gson.annotations.SerializedName

data class BusinessDetails(
    @SerializedName("accountNo")
    val accountNo: String?,
    @SerializedName("applicantType")
    val applicantType: String?,
    @SerializedName("bankName")
    val bankName: String?,
    @SerializedName("businessAddress")
    val businessAddress: String?,
    @SerializedName("businessName")
    val businessName: String?,
    @SerializedName("businessPan")
    val businessPan: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("constitution")
    val constitution: String?,
    @SerializedName("customerId")
    val customerId: String?,
    @SerializedName("dateOfIncorp")
    val dateOfIncorp: String?,
    @SerializedName("expenses")
    val expenses: String?,
    @SerializedName("gstNo")
    val gstNo: String?,
    @SerializedName("ifscCode")
    val ifscCode: String?,
    @SerializedName("income")
    val income: String?,
    @SerializedName("loanId")
    val loanId: String?,
    @SerializedName("segment")
    val segment: String?,
    @SerializedName("turnover")
    val turnover: String?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("udhyogAadhar")
    val udhyogAadhar: String?,
    @SerializedName("margin")
    val margin: Double?,
    @SerializedName("businessImage")
    val businessImage: String?,
    @SerializedName("udyam_no")
    val udyam_no: String?,
    @SerializedName("udyam_verified")
    val udyam_verified: String?,
    @SerializedName("pics_verified")
    val pics_verified: String?,
    @SerializedName("stock_photo")
    val stock_photo: String?,
    @SerializedName("business_photo")
    val business_photo: String?,
    @SerializedName("apiCode")
    val apiCode: String?,
    @SerializedName("apiDesc")
    val apiDesc: String?,
)