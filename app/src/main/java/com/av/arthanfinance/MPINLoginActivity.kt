package com.av.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.CompletedScreensResponse
import com.av.arthanfinance.applyLoan.LoanEligibilitySubmittedActivity
import com.av.arthanfinance.applyLoan.UploadBusinessPhotos
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MPINLoginActivity : BaseActivity() {
    private lateinit var tvForgotMpin: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvDontHaveAccount: TextView
    private lateinit var mpintext: EditText
    private lateinit var mobileText: EditText
    private lateinit var apiClient: ApiClient
    override val layoutId: Int get() = R.layout.layout_mpin_login
    private var contextFrom = 1
    private lateinit var mobileNum: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()

        if (intent.hasExtra("context_from")) {
            contextFrom = intent.getIntExtra("context_from", 1)
        } else {
            contextFrom = 1
        }

        apiClient = ApiClient()
        tvForgotMpin = findViewById(R.id.tv_forgot_mpin)
        btnLogin = findViewById(R.id.btn_login)
        mpintext = findViewById(R.id.edt_mpin)
        mobileText = findViewById(R.id.edt_mobile)

        tvDontHaveAccount = findViewById(R.id.tv_dnt_hav_account)

        if (intent.hasExtra("mob")) {
            mobileNum = intent.extras?.get("mob") as String
            mobileText.setText(mobileNum)
        } else {
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if (json != null) {
                val customerData: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                mobileText.setText(customerData.mobNo)
            }
        }

        tvForgotMpin.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            verifyCustomerPin()
        }

        tvDontHaveAccount.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        mpintext.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCustomerPin()
            }
            false
        }
    }

    private fun verifyCustomerPin() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobNo", mobileText.text.toString())
        jsonObject.addProperty("pin", mpintext.text.toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).verifyCustomerPin(jsonObject).enqueue(object :
            Callback<CustomerHomeTabResponse> {
            override fun onFailure(call: Call<CustomerHomeTabResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@MPINLoginActivity, "Login Failed. Please enter Valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<CustomerHomeTabResponse>,
                response: Response<CustomerHomeTabResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()

                if (custData != null) {
                    val sharedPref: SharedPreferences? =
                        getSharedPreferences("customerData", Context.MODE_PRIVATE)
                    val prefsEditor = sharedPref?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(custData)
                    prefsEditor?.putString("customerData", json)
                    prefsEditor?.apply()

                    getLastCompletedScreen(custData)

                } else {
                    custData?.let {
                        Toast.makeText(
                            this@MPINLoginActivity,
                            "A technical error occurred. Please try after some time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun getLastCompletedScreen(custData: CustomerHomeTabResponse) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", custData.customerId)

        showProgressDialog()
        ApiClient().getAuthApiService(this).getCompletedScreens(jsonObject).enqueue(object :
            Callback<CompletedScreensResponse> {
            override fun onFailure(call: Call<CompletedScreensResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@MPINLoginActivity, "Login Failed. Please enter Valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<CompletedScreensResponse>,
                response: Response<CompletedScreensResponse>
            ) {
                hideProgressDialog()
                val screenData = response.body()

                if (screenData != null) {
                    val lastScreenArray = screenData.screens

                    if (lastScreenArray.isNullOrEmpty()) {
                        val intent =
                            Intent(this@MPINLoginActivity, CheckEligibilityActivity::class.java)
                        intent.putExtra("customerData", custData)
                        startActivity(intent)
                        finish()
                    } else {
                        for (i in 0 until lastScreenArray.size) {
                            when (screenData.screens!![i]) {
                                "PAN_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadPhotoActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "PIC_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadAadharActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "OFFLINE_AADHAR_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadAadharAddressActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "PERSONAL_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadBankDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BANK_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadBusinessDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "VKYC_PA" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadBusinessDetailsActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BUSINESS" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadBusinessPhotos::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "BUSINESS_PHOTOS" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadReferenceActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "TRADE_REF" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        LoanEligibilitySubmittedActivity::class.java
                                    )
                                    intent.putExtra("customerData", custData)
                                    startActivity(intent)
//                                    finish()
                                }
                                "ELIGIBILITY" -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
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
                    Toast.makeText(this@MPINLoginActivity, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
