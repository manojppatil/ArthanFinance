package com.av.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityUploadReferenceBinding
import com.av.arthanfinance.manager.DataManager
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadPanActivity
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UploadReferenceActivity : BaseActivity() {
    private lateinit var activityUploadReferenceBinding: ActivityUploadReferenceBinding

    private var relationshiplist =
        arrayOf("Select Relationship", "Father", "Mother", "Brother", "Sister")

    private var loanResponse: LoanProcessResponse? = null
    var customerData: CustomerHomeTabResponse? = null

    private var kycCompleteStatus = "90"

    override val layoutId: Int
        get() = R.layout.activity_upload_reference

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadReferenceBinding =
            ActivityUploadReferenceBinding.inflate(layoutInflater)
        setContentView(activityUploadReferenceBinding.root)

        activityUploadReferenceBinding.pbKyc.max = 100
        ObjectAnimator.ofInt(activityUploadReferenceBinding.pbKyc, "progress", 90)
            .setDuration(1000).start()
        activityUploadReferenceBinding.tvPercent.text = "${kycCompleteStatus}%"
        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
        }

        if (intent.hasExtra("customerData")) {
            customerData = intent.getSerializableExtra("customerData") as CustomerHomeTabResponse
        }

        val qualificationAdapter =
            this.let { ArrayAdapter(it, R.layout.emi_options, relationshiplist) }
        activityUploadReferenceBinding.spRelaionShipApplicant.adapter = qualificationAdapter

        activityUploadReferenceBinding.btnNext.setOnClickListener {
            saveCustomerReferenceData()
        }
    }

//    private fun fetchRelationshipAsync() {
//        try {
//            ApiClient().getMasterApiService(this@UploadReferenceActivity).getRelationship()
//                ?.enqueue(object :
//                    Callback<DetailsResponseData> {
//                    override fun onFailure(call: Call<DetailsResponseData>, t: Throwable) {
//                        hideProgressDialog()
//                        t.printStackTrace()
//                        Toast.makeText(
//                            this@UploadReferenceActivity, "API call Failed.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    override fun onResponse(
//                        call: Call<DetailsResponseData>,
//                        response: Response<DetailsResponseData>
//                    ) {
//                        hideProgressDialog()
//                        if (response.body() != null) {
//                            try {
//                                if (sp_relaionShipApplicant?.adapter == null) {
//                                    sp_relaionShipApplicant?.adapter = DataSpinnerAdapter(
//                                        this@UploadReferenceActivity,
//                                        response.body()?.data?.toMutableList()
//                                            ?: mutableListOf()
//                                    ).also {
//                                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//
//                            }
//
//                        }
//                    }
//                })
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//
//        }
//    }

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
        val familyNameText = activityUploadReferenceBinding.edtNameFamily.text.toString()
        val familyMobileText = activityUploadReferenceBinding.edtMobNumFamily.text.toString()
        val familyRelation =
            activityUploadReferenceBinding.spRelaionShipApplicant.selectedItem.toString()

        if (neightbourNameText.isEmpty() || neighbourMobileText.isEmpty() || supplierNameText.isEmpty() || supplierMobileText.isEmpty() || familyNameText.isEmpty() || familyMobileText.isEmpty()) {
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

        if (neighbourMobileText == supplierMobileText) {
            Toast.makeText(
                this,
                "Neighbour and Supplier Mobile Number are same ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (neighbourMobileText == familyMobileText) {
            Toast.makeText(
                this,
                "Neighbour and Family Member Mobile Number are same ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (familyMobileText == supplierMobileText) {
            Toast.makeText(
                this,
                "Family Member and Supplier Mobile Number are same ",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val jsonObject = JsonObject()
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

        val referenceJson2 = JsonObject()
        referenceJson2.addProperty("refName", familyNameText)
        referenceJson2.addProperty("refMobNo", familyMobileText)
        referenceJson2.addProperty("relation", familyRelation)
        referenceJson2.addProperty("refType", "Family Member")

        jsonArray.add(referenceJson2)
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

                val intent =
                    Intent(applicationContext, LoanEligibilitySubmittedActivity::class.java)
                intent.putExtra("loanResponse", processingResponse)
                intent.putExtra("customerData", customerData)
                startActivity(intent)
                finish()
//                showCoApplicantDialog(processingResponse)
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

    private fun isValidPhoneNumber(phoneNumber: CharSequence?): Boolean {
        return if (phoneNumber == null || phoneNumber.length < 10) false else Patterns.PHONE.matcher(
            phoneNumber
        ).matches()
    }
}