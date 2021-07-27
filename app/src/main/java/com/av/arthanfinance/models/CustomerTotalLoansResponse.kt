package com.av.arthanfinance.models


import com.google.gson.annotations.SerializedName

data class CustomerTotalLoansResponse(
    @SerializedName("cnt")
    val cnt: Int?,
    @SerializedName("customerId")
    val customerId: String?
)