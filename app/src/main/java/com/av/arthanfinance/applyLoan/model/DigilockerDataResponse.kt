package com.av.arthanfinance.applyLoan.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DigilockerDataResponse(
    @SerializedName("id")
    @Expose
    var id: String? = null,
    @SerializedName("updated_at")
    @Expose
    var updated_at: String? = null,
    @SerializedName("created_at")
    @Expose
    var created_at: String? = null,
    @SerializedName("status")
    @Expose
    var status: String? = null,
    @SerializedName("customer_identifier")
    @Expose
    var customer_identifier: String? = null,
    @SerializedName("actions")
    var actions: ArrayList<AadharActions>,
    @SerializedName("reference_id")
    @Expose
    var reference_id: String? = null,
    @SerializedName("transaction_id")
    @Expose
    var transaction_id: String? = null,
    @SerializedName("customer_name")
    @Expose
    var customer_name: String? = null,
    @SerializedName("expire_in_days")
    @Expose
    var expire_in_days: String? = null,
    @SerializedName("reminder_registered")
    @Expose
    var reminder_registered: String? = null,
)

data class AadharActions(
    @SerializedName("details")
    var details: ADetails
)

data class ADetails(
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

    @SerializedName("id_number")
    @Expose
    var id_number: String? = null,

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