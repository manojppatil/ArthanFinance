package com.av.arthanfinance.applyLoan.model


import com.google.gson.annotations.SerializedName

data class CustomerReferenceResponse(
    @SerializedName("applicantType")
    val applicantType: String?,
    @SerializedName("customerId")
    val customerId: String?,
    @SerializedName("loanId")
    val loanId: String?,
    @SerializedName("refDetails")
    val customerReferences: List<CustomerReference>?
)