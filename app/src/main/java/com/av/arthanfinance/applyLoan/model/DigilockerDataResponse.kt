package com.av.arthanfinance.applyLoan.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DigilockerDataResponse(
    @SerializedName("actions")
    var actions: ArrayList<AadharActions>
)

data class AadharActions(
    @SerializedName("details")
    var details: Details
)

data class Details(
    @SerializedName("aadhaar")
    var aadhaarDetails: AadhaarDetails
)

data class AadhaarDetails(
    @SerializedName("gender")
    @Expose
    var gender: String? = null,

    @SerializedName("image")
    @Expose
    var image: String? = null,

    @SerializedName("name")
    @Expose
    var name: String? = null,

    @SerializedName("dob")
    @Expose
    var dob: String? = null,

    @SerializedName("current_address_details")
    var aadhaarAddressDetails: AadhaarAddressDetails
)

data class AadhaarAddressDetails(
    @SerializedName("address")
    var address: String? = null,

    @SerializedName("locality_or_post_office")
    var localityPostOffice: String? = null,

    @SerializedName("district_or_city")
    var districtCity: String? = null,

    @SerializedName("state")
    var state: String? = null,

    @SerializedName("pincode")
    var pincode: String? = null,

)