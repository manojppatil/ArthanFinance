package com.av.arthanfinance.applyLoan.model

import com.google.gson.annotations.SerializedName

class DigioPanResponse (

    @SerializedName("id")
    var id : String,
    @SerializedName("created_at")
    var createdAt : String,
    @SerializedName("status")
    var status : String,
    @SerializedName("customer_identifier")
    var customerIdentifier : String,
    @SerializedName("reference_id")
    var referenceId : String,
    @SerializedName("transaction_id")
    var transactionId : String,
    @SerializedName("expire_in_days")
    var expireInDays : Int,
    @SerializedName("reminder_registered")
    var reminderRegistered : Boolean,
    @SerializedName("access_token")
    var accessToken : AccessToken

)
data class AccessToken (

    @SerializedName("created_at")
    var createdAt : String,
    @SerializedName("id")
    var id : String,
    @SerializedName("entity_id")
    var entityId : String,
    @SerializedName("valid_till")
    var validTill : String

)