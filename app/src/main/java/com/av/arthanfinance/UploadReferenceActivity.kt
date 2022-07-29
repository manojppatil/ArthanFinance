package com.av.arthanfinance

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.ApplyForLoanActivity
import com.av.arthanfinance.applyLoan.LoanEligibilitySubmittedActivity
import com.av.arthanfinance.applyLoan.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityUploadReferenceBinding
import com.av.arthanfinance.manager.DataManager
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadPanActivity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadReferenceActivity : BaseActivity() {
    private lateinit var apiClient: ApiClient
    private lateinit var activityUploadReferenceBinding: ActivityUploadReferenceBinding

    private var loanResponse: LoanProcessResponse? = null
    var customerData: CustomerHomeTabResponse? = null
    private val isViewShown = false

    override val layoutId: Int
        get() = R.layout.activity_upload_reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadReferenceBinding =
            ActivityUploadReferenceBinding.inflate(layoutInflater)
        setContentView(activityUploadReferenceBinding.root)

        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
        }

        if (intent.hasExtra("customerData")) {
            customerData = intent.getSerializableExtra("customerData") as CustomerHomeTabResponse
        }
        activityUploadReferenceBinding.btnNext.setOnClickListener {
            saveCustomerReferenceData()
        }
    }

    private fun showCoApplicantDialog(loanResponse: LoanProcessResponse) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to add Co-Applicant")
            // if the dialog is cancelable
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                dialog.dismiss()
                addCoApplicantData()
            }.setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                Log.d("LOANR", loanResponse.toString())
                val intent =
                    Intent(this, LoanEligibilitySubmittedActivity::class.java)
                intent.putExtra("loanResponse", loanResponse)
                startActivity(intent)
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Co-Applicant Data")
        alert.show()
    }

    private fun addCoApplicantData() {

        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        ApiClient().getApiService(this).getCustomerId(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadReferenceActivity,
                    "Not able to add coApplicant, proceeding further",
                    Toast.LENGTH_SHORT
                ).show()

                /*val intent = Intent(this@UploadReferenceActivity, UploadPanActivity::class.java)
                intent.putExtra("loanResponse", loanResponse)
                startActivity(intent)*/
            }

            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                hideProgressDialog()
                val coApplicantResponse = response.body() as LoanProcessResponse
                val custID = coApplicantResponse.customerId
                DataManager.applicantType = "CA"
                loanResponse!!.applicantType = "CA"

                val intent = Intent(this@UploadReferenceActivity, UploadPanActivity::class.java)
                intent.putExtra("loanResponse", loanResponse)
                intent.putExtra("coCustomerId", custID)
                startActivity(intent)

            }
        })
    }

    private fun saveCustomerReferenceData() {
        val neightbourNameText = activityUploadReferenceBinding.edtName.text.toString()
        val neighbourMobileText = activityUploadReferenceBinding.edtMobNum.text.toString()
        val supplierNameText = activityUploadReferenceBinding.edtNameSup.text.toString()
        val supplierMobileText = activityUploadReferenceBinding.edtMobNumSup.text.toString()

        if (neightbourNameText.isEmpty() || neighbourMobileText.isEmpty() || supplierNameText.isEmpty() || supplierMobileText.isEmpty()) {
            Toast.makeText(
                this,
                "Enter all required fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isValidPhoneNumber(neighbourMobileText)) {
            Toast.makeText(
                this,
                "Please enter VALID Neightbour Mobile Number.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isValidPhoneNumber(supplierMobileText)) {
            Toast.makeText(
                this,
                "Please enter VALID Supplier Mobile Number.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val jsonObject = JsonObject()
//        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", customerData?.customerId)

        val jsonArray = JsonArray()

        val referenceJson = JsonObject()
        referenceJson.addProperty("refName", neightbourNameText)
        referenceJson.addProperty("refMobNo", neighbourMobileText)
        referenceJson.addProperty("refType", "customer")

        val referenceJson1 = JsonObject()
        referenceJson1.addProperty("refName", supplierNameText)
        referenceJson1.addProperty("refMobNo", supplierMobileText)
        referenceJson1.addProperty("refType", "Supplier")

        jsonArray.add(referenceJson1)
        jsonArray.add(referenceJson)
        jsonObject.add("refDetails", jsonArray)
        showProgressDialog()

        ApiClient().getAuthApiService(this).saveCustReference(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                hideProgressDialog()

                val processingResponse = response.body() as LoanProcessResponse
                loanResponse?.fee = processingResponse.fee
                loanResponse?.pf = processingResponse.pf
                loanResponse?.roi = processingResponse.roi
                loanResponse?.loanTerm = processingResponse.loanTerm
                loanResponse?.emi = processingResponse.emi
                loanResponse?.loanAmt = processingResponse.loanAmt

                showCoApplicantDialog(processingResponse)


            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadReferenceActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    fun isValidPhoneNumber(phoneNumber: CharSequence?): Boolean {
        return if (phoneNumber == null || phoneNumber.length < 10) {
            false
        } else {
            Patterns.PHONE.matcher(phoneNumber).matches()
        }
    }
}