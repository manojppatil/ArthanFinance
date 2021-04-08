package com.example.arthanfinance.applyLoan

import androidx.lifecycle.ViewModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LoanProcessResponse : Serializable {
    @SerializedName("loanId")
    @Expose
    var loanId: String? = null

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("customerName")
    @Expose
    var customerName: String? = null

    @SerializedName("mobileNo")
    @Expose
    var mobileNo: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("nextScreen")
    @Expose
    var nextScreen: String? = null

    @SerializedName("apiMessage")
    @Expose
    var apiMessage: String? = null

    @SerializedName("apiStatus")
    @Expose
    var apiStatus: String? = null

    @SerializedName("addressLine1")
    @Expose
    var addressLine1: String? = null

    @SerializedName("addressLine2")
    @Expose
    var addressLine2: String? = null

    @SerializedName("addressLine3")
    @Expose
    var addressLine3: String? = null

    @SerializedName("city")
    @Expose
    var city: String? = null

    @SerializedName("state")
    @Expose
    var state: String? = null

    @SerializedName("pincode")
    @Expose
    var pincode: String? = null

    @SerializedName("loanAmt")
    @Expose
    var loanAmt: String? = null

    @SerializedName("loanTerm")
    @Expose
    var loanTerm: String? = null

    @SerializedName("roi")
    @Expose
    var roi: String? = null

    @SerializedName("emi")
    @Expose
    var emi: String? = null

    @SerializedName("fee")
    @Expose
    var fee: String? = null

    @SerializedName("pf")
    @Expose
    var pf: String? = null

    @SerializedName("applicantType")
    @Expose
    var applicantType: String? = null

    @SerializedName("paytmUrl")
    @Expose
    var paytmUrl: String? = null
}

class AuthenticationResponse : Serializable {
    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

}

class LoanDetails : Serializable {
    @SerializedName("loanId")
    @Expose
    var loanId: String? = null

    @SerializedName("loanAmount")
    @Expose
    var loanAmount: String? = null

    @SerializedName("loanSactionedDate")
    @Expose
    var loanSactionedDate: String? = null

    @SerializedName("loanType")
    @Expose
    var loanType: String? = null

    @SerializedName("loanAccountNum")
    @Expose
    var loanAccountNum: String? = null

    @SerializedName("percentCompleted")
    @Expose
    var percentCompleted: String? = null

}

class LoansResponse : Serializable {
    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("loans")
    @Expose
    var loans: ArrayList<LoanDetails>? = null

}


class FileUploadResponse : Serializable {
    @SerializedName("docId")
    @Expose
    var docId: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("bankStatement")
    @Expose
    var bankStatement: String? = null

    @SerializedName("fileName")
    @Expose
    var fileName: String? = null

    @SerializedName("uploadTime")
    @Expose
    var uploadTime: String? = null

    @SerializedName("ocrFile")
    @Expose
    var ocrFile: String? = null

    @SerializedName("fileCount")
    @Expose
    var fileCount: String? = null

    @SerializedName("totalPages")
    @Expose
    var totalPages: String? = null

    @SerializedName("error")
    @Expose
    var error: String? = null

}