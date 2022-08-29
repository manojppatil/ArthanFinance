package com.av.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.av.arthanfinance.applyLoan.CompletedScreensResponse
import com.av.arthanfinance.applyLoan.LoanEligibilitySubmittedActivity
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_check_eligibility.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckEligibilityActivity : AppCompatActivity() {

    var customerData: CustomerHomeTabResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_eligibility)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
        }

        checkEligibility.setOnClickListener {
            getLastCompletedScreen(customerData!!)
        }
    }

    private fun getLastCompletedScreen(custData: CustomerHomeTabResponse) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", custData.customerId)

//        context.showProgressDialog()
        ApiClient().getAuthApiService(this).getCompletedScreens(jsonObject).enqueue(object :
            Callback<CompletedScreensResponse> {
            override fun onFailure(call: Call<CompletedScreensResponse>, t: Throwable) {
//                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@CheckEligibilityActivity, "Something went wrong, Please try again later!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<CompletedScreensResponse>,
                response: Response<CompletedScreensResponse>
            ) {
//                context.hideProgressDialog()
                val screenData = response.body()

                if (screenData != null) {
                    val lastScreenArray = screenData.screens

                    if (lastScreenArray.isNullOrEmpty()) {
                        val intent =
                            Intent(this@CheckEligibilityActivity, UploadPanActivity::class.java)
                        intent.putExtra("customerData", custData)
                        startActivity(intent)
//                        context.finish()
                    } else {
                        for (i in 0 until lastScreenArray.size) {
                            when (screenData.screens!![i]) {
                                "PAN_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadPhotoActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "PIC_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadAadharActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "OFFLINE_AADHAR_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadAadharAddressActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "PERSONAL_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadBankDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BANK_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadBusinessDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "VKYC_PA" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadBusinessDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BUSINESS" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadBusinessDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BUSINESS_PHOTOS" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        UploadReferenceActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "TRADE_REF" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        LoanEligibilitySubmittedActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "ELIGIBILITY" -> {
                                    val intent = Intent(
                                        this@CheckEligibilityActivity,
                                        HomeDashboardActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@CheckEligibilityActivity, "failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }
}