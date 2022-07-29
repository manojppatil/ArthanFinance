package com.av.arthanfinance.applyLoan.model

import com.google.gson.annotations.SerializedName

class DigilockerTokenResponse (
    @SerializedName("access_token")
    var accessToken1 : AccessToken1

)
data class AccessToken1 (
    @SerializedName("id")
    var token : String,
    @SerializedName("entity_id")
    var kId : String,
)