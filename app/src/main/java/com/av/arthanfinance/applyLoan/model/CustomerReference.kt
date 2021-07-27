package com.av.arthanfinance.applyLoan.model


import com.google.gson.annotations.SerializedName

data class CustomerReference(
    @SerializedName("refMobNo")
    val refMobNo: String?,
    @SerializedName("refName")
    val refName: String?,
    @SerializedName("refType")
    val refType: String?
)