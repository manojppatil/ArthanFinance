package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.databinding.ActivityCalculateKfsBinding
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

class CalculateKfs : BaseActivity() {
    private lateinit var activity2: ActivityCalculateKfsBinding
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_calculate_kfs

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity2 = ActivityCalculateKfsBinding.inflate(layoutInflater)
        setContentView(activity2.root)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        if (supportActionBar != null)
            supportActionBar?.hide()

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
        activity2.tvProcessingFeesRs.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
//        activity2.tvLoanTenureRs.text = intent.getStringExtra("tenure") + " Days"
//        activity2.tvRepayDateRs.text = intent.getStringExtra("repaymentDate")
//        activity2.tvTotalInterestRs.text = intent.getStringExtra("totalInterest")
//        activity2.tvPayableAmtRs.text = intent.getStringExtra("payableAmt")
//        activity2.tvNetDisbursedAmtRs.text = "Rs. " + intent.getStringExtra("netDisbursedAmt")

        activity2.tvReadmoreKfs.setOnClickListener {
            activity2.tvReadmoreKfs.visibility = View.GONE
            activity2.lytNgrDetails.visibility = View.VISIBLE
        }

        activity2.btnProceed.setOnClickListener {
            if (!accept_tc.isChecked) {
                Toast.makeText(
                    this@CalculateKfs,
                    "Please accept terms and Conditions.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            initiateAgreement()
        }

        activity2.imgBack.setOnClickListener {
            finish()
        }

    }

    private fun initiateAgreement() {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        clevertapDefaultInstance?.pushEvent("Agreement Initiate started")//added by CleverTap Assistant
        ApiClient().getAuthApiService(this).initiateAgreement(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Agreement Initiate failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@CalculateKfs,
                    "Agreement not initiated. Try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
//                    if (custData.apiCode == "200") {
                    clevertapDefaultInstance?.pushEvent("Agreement Initiate success")//added by CleverTap Assistant
                    updateStage(ArthanFinConstants.agreement)
//                    } else {
//                        Log.e("Error", custData.message + "")
//                    }
                }
            }
        })
    }

    private fun updateStage(stage: String) {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("stage", stage)
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@CalculateKfs, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    clevertapDefaultInstance?.pushEvent("Agreement stage updated")//added by CleverTap Assistant
                    val intent = Intent(this@CalculateKfs, UpiMandateSuccess::class.java)
                    intent.putExtra("from", "agreement")
                    startActivity(intent)
                    finish()
                }

            }
        })
    }
}