package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityLoanSummaryBinding
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_upi_mandate.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoanSummaryActivity : BaseActivity() {
    private lateinit var activity2: ActivityLoanSummaryBinding
    private var loanResponse: LoanProcessResponse? = null
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_loan_summary

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity2 = ActivityLoanSummaryBinding.inflate(layoutInflater)
        setContentView(activity2.root)

        if (supportActionBar != null)
            supportActionBar?.hide()
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        if (intent.hasExtra("customerData")) {
            customerData = intent.extras?.get("customerData") as CustomerHomeTabResponse
        } else {
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if (json != null) {
                val obj: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                customerData = obj
            }
        }
        apiClient = ApiClient()

        activity2.tvLoanAmntRs.text = "Rs. " + intent.getStringExtra("loanAmount")
//        activity2.tvLoanTenureRs.text = intent.getStringExtra("tenure") + " Days"
        activity2.tvRepayDateRs.text = intent.getStringExtra("repaymentDate")
//        activity2.tvTotalInterestRs.text = intent.getStringExtra("totalInterest")
//        activity2.tvPayableAmtRs.text = intent.getStringExtra("payableAmt")
        activity2.tvNetDisbursedAmtRs.text = "Rs. " + intent.getStringExtra("loanAmount")

        activity2.btnProceed.setOnClickListener {
            confirmLoanRequest()
        }

        activity2.imgBack.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Back from confirm loan")//added by CleverTap Assistant
            finish()
        }
    }

    private fun updateStage(stage: String, loanId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("stage", stage)
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@LoanSummaryActivity, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                if (response.body()?.apiCode == "200") {
                    val intent = Intent(
                        this@LoanSummaryActivity,
                        LoanApplicationSubmittedActivity::class.java
                    )
                    intent.putExtra("loanId", loanId)
                    intent.putExtra("loanResponse", loanResponse)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun confirmLoanRequest() {
        val loanAmount = intent.getStringExtra("loanAmount")?.replace(",", "")
        val repaymentDate =
            intent.getStringExtra("repaymentDate")?.replace("\u20B9", "")

        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmount", loanAmount)
        jsonObject.addProperty("repaymentDate", repaymentDate)
        jsonObject.addProperty("loanId", intent.getStringExtra("loanId"))
        jsonObject.addProperty("tenure", intent.getStringExtra("tenure"))
//        jsonObject.addProperty("consent", "Y")
        clevertapDefaultInstance?.pushEvent("Confirm loan requested")//added by CleverTap Assistant

        apiClient.getAuthApiService(this).confirmLoan(jsonObject)
            .enqueue(object : Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>,
                ) {
                    hideProgressDialog()
                    try {
                        loanResponse = response.body()
                        Log.e("TAGR", loanResponse.toString())
                        if (loanResponse != null) {
                            val loanId = loanResponse!!.applicationId
                            clevertapDefaultInstance?.pushEvent("Confirm loan success")//added by CleverTap Assistant

                            Log.e("TAGid", loanResponse!!.applicationId.toString())
                            updateStage(ArthanFinConstants.withdraw, loanId!!)
                        }
                    } catch (ex: NullPointerException) {
                        ex.printStackTrace()
                    }

                }

                override fun onFailure(
                    call: Call<LoanProcessResponse>,
                    t: Throwable,
                ) {
                    t.printStackTrace()
                    hideProgressDialog()

                    clevertapDefaultInstance?.pushEvent("Confirm loan service failure")//added by CleverTap Assistant
                    Toast.makeText(
                        this@LoanSummaryActivity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}