package com.av.arthanfinance.models


import com.google.gson.annotations.SerializedName

data class CustomerBankDetailsResponse(
    @SerializedName("accountName")
    val accountName: String?,
    @SerializedName("accountNo")
    val accountNo: String?,
    @SerializedName("bankName")
    val bankName: String?,
    @SerializedName("ifscCode")
    val ifscCode: String?,
    @SerializedName("loanId")
    val loanId: String?
)