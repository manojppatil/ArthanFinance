package com.av.arthanfinance.applyLoan

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

enum class LoanStatus {
    COMPLETE,
    PENDING
}
/*
{
"loanId": null,
"customerId": null,
"applicantType": null,
"idType": null,
"imageBase64": null,
"applicationType": null,
"customerPic": null,
"tenure": "18",
"loanAmount": "550000",
"purpose": "Business Expansion",
"customerName": null,
"mobileNo": null,
"dob": null,
"addressLine1": null,
"addressLine2": null,
"addressLine3": null,
"city": null,
"state": null,
"pincode": null,
"roi": "27",
"emi": "8789"
}
 */
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

    // this attribute is for get request
    @SerializedName("loanAmount")
    @Expose
    var loanAmount: String? = null

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
    var applicantType: String = "PA"

    @SerializedName("paytmUrl")
    @Expose
    var paytmUrl: String? = null

    @SerializedName("tenure")
    @Expose
    var tenure: String? = null

    @SerializedName("purpose")
    @Expose
    var purpose: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("cnt")
    @Expose
    var loanCount: Int = 0
}

class AuthenticationResponse : Serializable {

    @SerializedName("message")
    @Expose
    val message: String = ""

    @SerializedName("apiCode")
    @Expose
    val apiCode: String? = null

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

/*
{
"loanId": "L123",
"customerId": null,
"applicantType": "PA",
"panUrl": "https://s3.ap-south-1.amazonaws.com/test-doc-repo2/W8SE-X9YH-Z6PJ_MZPQ8BPOGM_PA_PAN.jpg",
"aadharFrontUrl": null,
"aadharBackUrl": null,
"applicantImgUrl": null,
"voterUrl": null
}
 */

class LoanKYCDetailsResponse : Serializable {
    @SerializedName("loanId")
    @Expose
    var loanId: String? = null

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("applicantType")
    @Expose
    var applicantType: String? = null

    @SerializedName("panUrl")
    @Expose
    var panUrl: String? = null

    @SerializedName("aadharFrontUrl")
    @Expose
    var aadharFrontUrl: String? = null

    @SerializedName("aadharBackUrl")
    @Expose
    var aadharBackUrl: String? = null

    @SerializedName("applicantImgUrl")
    @Expose
    var applicantImgUrl: String? = null

    @SerializedName("voterUrl")
    @Expose
    var voterUrl: String? = null

    @SerializedName("customerName")
    @Expose
    var customerName: String? = null

    @SerializedName("mobileNo")
    @Expose
    var mobileNo: String? = null

    @SerializedName("dob")
    @Expose
    var dob: String? = null

}