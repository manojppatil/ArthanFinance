package com.av.arthanfinance.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CustomerHomeTabResponse : Serializable {
    @SerializedName("customerImage")
    @Expose
    val customerImage: String? = null

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("customerName")
    @Expose
    var customerName: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("mobNo")
    @Expose
    var mobNo: String? = null

    @SerializedName("emailId")
    @Expose
    var emailId: String? = null

    @SerializedName("customerConsent")
    @Expose
    var customerConsent: String? = null

    @SerializedName("customerLanguage")
    @Expose
    var customerLanguage: String? = null

    @SerializedName("customerPin")
    @Expose
    var customerPin: String? = null

    @SerializedName("customerBiometric")
    @Expose
    var customerBiometric: String? = null

    @SerializedName("lastLoginTime")
    @Expose
    var lastLoginTime: String? = null

    @SerializedName("customerImg")
    @Expose
    var customerImg: String? = null

    @SerializedName("profileCompleted")
    @Expose
    var profileCompleted: String? = null

    @SerializedName("loansApplied")
    @Expose
    var loansApplied: String? = null

    @SerializedName("appVersion")
    @Expose
    var appVersion: String? = null

    @SerializedName("errCode")
    @Expose
    val errCode: String? = null

    @SerializedName("errDesc")
    @Expose
    val errDesc: String? = null

    @SerializedName("applicantType")
    @Expose
    var applicantType: String = "PA"

}
