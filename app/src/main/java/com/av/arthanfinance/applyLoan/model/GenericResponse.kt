package com.av.arthanfinance.applyLoan.model


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class GenericResponse(
    @SerializedName("apiCode")
    val apiCode: String? = "",
    @SerializedName("apiDesc")
    val apiDesc: String? = ""
) : Parcelable