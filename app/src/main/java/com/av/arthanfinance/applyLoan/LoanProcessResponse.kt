package com.av.arthanfinance.applyLoan

import com.av.arthanfinance.applyLoan.model.AccessToken
import com.google.gson.JsonObject
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

    @SerializedName("limitHit")
    @Expose
    var limitHit: String? = null

    @SerializedName("eligilityAmount")
    @Expose
    var eligilityAmount: String? = null
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

class GstReturnResponse : Serializable {

    @SerializedName("statusCode")
    @Expose
    val statusCode: Int = 0

    @SerializedName("statusMessage")
    @Expose
    val apiCode: String? = null

    @SerializedName("requestId")
    @Expose
    var requestId: String? = null

}

class OtpResponse : Serializable {

    @SerializedName("destination")
    @Expose
    val destination: String? = null

    @SerializedName("id")
    @Expose
    val id: String? = null

    @SerializedName("mrid")
    @Expose
    val mrid: String? = null

    @SerializedName("segment")
    @Expose
    val segment: String? = null

    @SerializedName("status")
    @Expose
    val status: String? = null

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

class BankDetilsResponse : Serializable {
    @SerializedName("verified")
    @Expose
    var verified: Boolean = false

    @SerializedName("beneficiary_name_with_bank")
    @Expose
    var name: String = ""

}

data class AccessToken(

    @SerializedName("created_at")
    var createdAt: String,
    @SerializedName("id")
    var id: String,
    @SerializedName("entity_id")
    var entityId: String,
    @SerializedName("valid_till")
    var validTill: String

)

class CompletedScreensResponse : Serializable {

    @SerializedName("customerId")
    @Expose
    var customerId: String = ""

    @SerializedName("screens")
    @Expose
    var screens: ArrayList<String>? = null
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

class UserKYCDetailsResponse : Serializable {

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("panUrl")
    @Expose
    var panUrl: String? = null

    @SerializedName("afUrl")
    @Expose
    var aadharFrontUrl: String? = null

    @SerializedName("abUrl")
    @Expose
    var aadharBackUrl: String? = null

    @SerializedName("picUrl")
    @Expose
    var applicantImgUrl: String? = null

}

/*name = intent.getStringExtra("name")!!
fatherName = intent.getStringExtra("fatherName")!!
dob = intent.getStringExtra("dob")!!
gender = intent.getStringExtra("gender")!!
aadharImage = intent.getStringExtra("image1")!!
addressLine1 = intent.getStringExtra("addressLine1")!!
addressLine2 = intent.getStringExtra("addressLine2")!!
addressLine3 = intent.getStringExtra("addressLine3")!!
state = intent.getStringExtra("state")!!
pincode = intent.getStringExtra("pincode")!!*/
class UserDetailsResponse : Serializable {

    @SerializedName("fullName")
    @Expose
    var fullName: String = ""

    @SerializedName("dob")
    @Expose
    var dob: String? = null

    @SerializedName("gender")
    @Expose
    var gender: String? = null

    @SerializedName("addressLine1")
    @Expose
    var addressLine1: String? = null

    @SerializedName("addressLine2")
    @Expose
    var addressLine2: String? = null

    @SerializedName("addressLine3")
    @Expose
    var addressLine3: String? = null

    @SerializedName("pinCode")
    @Expose
    var pinCode: String? = null

    @SerializedName("state")
    @Expose
    var state: String? = null

    @SerializedName("customerPic")
    @Expose
    var customerPic: String? = null

    @SerializedName("aadharFrontUrl")
    @Expose
    var aadharFrontUrl: String? = null

    @SerializedName("aadharBackUrl")
    @Expose
    var aadharBackUrl: String? = null

    @SerializedName("apiCode")
    @Expose
    var apiCode: String? = null
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

class UdyamDetailsResponse : Serializable {
    @SerializedName("requestId")
    var requestId: String? = null

    @SerializedName("result")
    var result: Result? = Result()

    @SerializedName("statusCode")
    var statusCode: Int? = null
}

data class Result(
    @SerializedName("udyamRegistrationNo") var udyamRegistrationNo: String? = null,
    @SerializedName("dateOfRegistration") var dateOfRegistration: String? = null,
    @SerializedName("interestedInTreds") var interestedInTreds: String? = null,
    @SerializedName("interestedInGem") var interestedInGem: String? = null,
    @SerializedName("profile") var profile: Profile? = Profile(),
    @SerializedName("bankDetails") var bankDetails: BankDetails? = BankDetails(),
    @SerializedName("employmentDetails") var employmentDetails: EmploymentDetails? = EmploymentDetails(),
    @SerializedName("financials") var financials: Financials? = Financials(),
    @SerializedName("branchDetails") var branchDetails: ArrayList<BranchDetails> = arrayListOf(),
    @SerializedName("officialAddress") var officialAddress: OfficialAddress? = OfficialAddress(),
    @SerializedName("industry") var industry: ArrayList<Industry> = arrayListOf()
)

data class Profile(
    @SerializedName("name") var name: String? = null,
    @SerializedName("enterpriseType") var enterpriseType: String? = null,
    @SerializedName("majorActivity") var majorActivity: String? = null,
    @SerializedName("organizationType") var organizationType: String? = null,
    @SerializedName("ownerName") var ownerName: String? = null,
    @SerializedName("pan") var pan: String? = null,
    @SerializedName("previousITRStatus") var previousITRStatus: String? = null,
    @SerializedName("itrType") var itrType: String? = null,
    @SerializedName("gstnStatus") var gstnStatus: String? = null,
    @SerializedName("mobile") var mobile: String? = null,
    @SerializedName("emailId") var emailId: String? = null,
    @SerializedName("socialCategory") var socialCategory: String? = null,
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("speciallyAbled") var speciallyAbled: String? = null,
    @SerializedName("dateOfIncorporation") var dateOfIncorporation: String? = null,
    @SerializedName("dateOfCommencement") var dateOfCommencement: String? = null

)

data class BankDetails(
    @SerializedName("bank") var bank: String? = null,
    @SerializedName("ifsc") var ifsc: String? = null,
    @SerializedName("acNo") var acNo: String? = null
)

data class EmploymentDetails(
    @SerializedName("male") var male: String? = null,
    @SerializedName("female") var female: String? = null,
    @SerializedName("others") var others: String? = null,
    @SerializedName("total") var total: String? = null
)

data class Details(
    @SerializedName("wdvPme") var wdvPme: String? = null,
    @SerializedName("exclusion") var exclusion: String? = null,
    @SerializedName("netInvestmentInPme") var netInvestmentInPme: String? = null,
    @SerializedName("totalTurnover") var totalTurnover: String? = null,
    @SerializedName("exportTurnover") var exportTurnover: String? = null,
    @SerializedName("netTurnover") var netTurnover: String? = null
)

data class Financials(
    @SerializedName("financialYear") var financialYear: String? = null,
    @SerializedName("details") var details: Details? = Details()
)

data class BranchDetails(
    @SerializedName("name") var name: String? = null,
    @SerializedName("flat") var flat: String? = null,
    @SerializedName("premises") var premises: String? = null,
    @SerializedName("village") var village: String? = null,
    @SerializedName("block") var block: String? = null,
    @SerializedName("road") var road: String? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("pincode") var pincode: String? = null,
    @SerializedName("district") var district: String? = null
)

data class OfficialAddress(
    @SerializedName("flat") var flat: String? = null,
    @SerializedName("premises") var premises: String? = null,
    @SerializedName("village") var village: String? = null,
    @SerializedName("block") var block: String? = null,
    @SerializedName("road") var road: String? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("pincode") var pincode: String? = null,
    @SerializedName("district") var district: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("mobile") var mobile: String? = null
)

data class Industry(
    @SerializedName("industry") var industry: String? = null,
    @SerializedName("subSector") var subSector: String? = null,
    @SerializedName("activityDescription") var activityDescription: String? = null,
    @SerializedName("industryCode") var industryCode: String? = null,
    @SerializedName("subSectorCode") var subSectorCode: String? = null,
    @SerializedName("nicCode") var nicCode: String? = null,
    @SerializedName("activity") var activity: String? = null
)
