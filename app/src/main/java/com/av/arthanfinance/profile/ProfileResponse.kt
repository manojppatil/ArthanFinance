package com.av.arthanfinance.profile


import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class ProfileResponse(
    @SerializedName("dob")
    val dob: String? = "",
    @SerializedName("emailId")
    val emailId: String? = "",
    @SerializedName("mobileNo")
    val mobileNo:String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("ofcAddress")
    val ofcAddress:String? = "",
    @SerializedName("panNo")
    val panNo:String? = "",
    @SerializedName("resiAddress")
    val resiAddress: String? = ""
) : Parcelable