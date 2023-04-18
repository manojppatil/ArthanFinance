package com.av.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.model.CompletedScreensResponse
import com.av.arthanfinance.applyLoan.LoanEligibilitySubmittedActivity
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.serviceRequest.Emi_calculator
import com.av.arthanfinance.serviceRequest.Getintouch
import com.av.arthanfinance.serviceRequest.LatestOffers
import com.av.arthanfinance.serviceRequest.LocateUs
import com.av.arthanfinance.user_kyc.*
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MPINLoginActivity : BaseActivity() {
    private lateinit var tvForgotMpin: TextView
    private lateinit var tvMobErrormsg: TextView
    private lateinit var tvMpinErrormsg: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvDontHaveAccount: TextView
    private lateinit var mpintext: EditText
    private lateinit var mobileText: EditText
    private lateinit var apiClient: ApiClient
    private lateinit var emiLayout: LinearLayout
    private lateinit var offersLayout: LinearLayout
    private lateinit var locateLayout: LinearLayout
    private lateinit var touchLayout: LinearLayout
    override val layoutId: Int get() = R.layout.layout_mpin_login
    private var contextFrom: Int = 0
    private lateinit var mobileNum: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()

//        if (intent.hasExtra("context_from")) {
//            contextFrom = intent.getIntExtra("context_from", 1)
//            Log.e("newconetxt", contextFrom.toString())
//        } else {
//            contextFrom = 1
//        }
//        Log.e("newconetxt2", contextFrom.toString())
//        if (contextFrom == 1) {
//            val fingerPref: SharedPreferences =
//                getSharedPreferences("customerData", Context.MODE_PRIVATE)
//            if (fingerPref.getBoolean("isFingerprintSet", true)) {
//                BiometricAuthentication.startScanning(this, "Fingerprint Login",
//                    "",
//                    "",
//                    object :
//                        MSFCallback {
//                        override fun onSuccess(message: String) {
//
//                            runOnUiThread {
//                                Toast.makeText(this@MPINLoginActivity, message, Toast.LENGTH_LONG)
//                                    .show()
//                                val intent =
//                                    Intent(
//                                        this@MPINLoginActivity,
//                                        HomeDashboardActivity::class.java
//                                    )
//                                startActivity(intent)
//                            }
//                        }
//
//                        override fun onFailure(errorString: String) {
//                            runOnUiThread {
//                                Toast.makeText(
//                                    this@MPINLoginActivity,
//                                    errorString,
//                                    Toast.LENGTH_LONG
//                                )
//                                    .show()
//                            }
//                        }
//                    })
//            }
//        }

        apiClient = ApiClient()
        tvForgotMpin = findViewById(R.id.tv_forgot_mpin)
        tvMobErrormsg = findViewById(R.id.tv_mob_error_msg)
        tvMpinErrormsg = findViewById(R.id.tv_mpin_error_msg)
        btnLogin = findViewById(R.id.btn_login)
        mpintext = findViewById(R.id.edt_mpin)
        mobileText = findViewById(R.id.edt_mobile)
        emiLayout = findViewById(R.id.lyt_emicalc)
        offersLayout = findViewById(R.id.lyt_latest_offers)
        locateLayout = findViewById(R.id.lyt_locateus)
        touchLayout = findViewById(R.id.lyt_getintouch)
        tvDontHaveAccount = findViewById(R.id.tv_dnt_hav_account)

        emiLayout.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, Emi_calculator::class.java)
            startActivity(intent)
        }
        offersLayout.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, LatestOffers::class.java)
            startActivity(intent)
        }
        locateLayout.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, LocateUs::class.java)
            startActivity(intent)
        }
        touchLayout.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, Getintouch::class.java)
            startActivity(intent)
        }

        if (intent.hasExtra("mob")) {
            mobileNum = intent.extras?.get("mob") as String
            mobileText.setText(mobileNum)
        } else {
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)

            if (mPrefs != null) {
                mobileText.setText(mPrefs.getString("mobNo", null))
            }

        }

        tvForgotMpin.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
//            if (!validateMPIN()) {
//                return@setOnClickListener
//            } else {
                verifyCustomerPin()
//            }

        }

        tvDontHaveAccount.setOnClickListener {
            val intent = Intent(this@MPINLoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        mpintext.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCustomerPin()
            }
            false
        }
    }

    private fun validateMPIN(): Boolean {
        if (mpintext.text.toString().isEmpty()) {
            Toast.makeText(
                this, "Please enter the valid MPIN",
                Toast.LENGTH_SHORT
            ).show()
            return true
        } else if (mobileText.text.toString()
                .isEmpty() || mobileText.text.toString().length != 10
        ) {
            Toast.makeText(
                this, "Please enter the valid Mobile number",
                Toast.LENGTH_SHORT
            ).show()
            return true
        }
        return false
    }

    private fun verifyCustomerPin() {
        tvMpinErrormsg.visibility = View.GONE
        tvMobErrormsg.visibility = View.GONE
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
                response: Response<CustomerHomeTabResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()

                if (custData != null) {
                    when (custData.errCode) {
                        "401" -> {
                            tvMobErrormsg.visibility = View.VISIBLE
                            tvMobErrormsg.text = custData.errDesc
                        }
                        "402" -> {
                            tvMpinErrormsg.visibility = View.VISIBLE
                            tvMpinErrormsg.text = custData.errDesc
                        }
                        else -> {
                            val sharedPref: SharedPreferences? =
                                getSharedPreferences("customerData", Context.MODE_PRIVATE)
                            val prefsEditor = sharedPref?.edit()
                            prefsEditor?.putString("customerId", custData.customerId)
                            prefsEditor?.putString("mobNo", mobileText.text.toString())
                            prefsEditor?.putBoolean(ArthanFinConstants.isMpinSet, true)
                            val gson = Gson()
                            val json: String = gson.toJson(custData)
                            prefsEditor?.putString("customerData", json)
                            prefsEditor?.apply()

                            getLastCompletedScreen(custData)
                        }
                    }
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
                response: Response<CompletedScreensResponse>,
            ) {
                val screenData = response.body()

                if (screenData != null) {
                    val lastScreenArray = screenData.screens

                    if (lastScreenArray.isNullOrEmpty()) {
                        val intent =
                            Intent(this@MPINLoginActivity, UploadBankDetailsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        for (i in 0 until lastScreenArray.size) {
                            when (screenData.screens!![i]) {
                                ArthanFinConstants.register -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadBankDetailsActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.bank -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        UploadPanActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.pan -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        LoanEligibilitySubmittedActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.eligibility, ArthanFinConstants.digilocker, ArthanFinConstants.offline_aadhar,
                                ArthanFinConstants.business, ArthanFinConstants.business_photos, ArthanFinConstants.skip_business_photos,
                                ArthanFinConstants.enach, ArthanFinConstants.pic_pa, ArthanFinConstants.agreement,
                                ArthanFinConstants.apply_loan, ArthanFinConstants.withdraw,
                                -> {
                                    val intent = Intent(
                                        this@MPINLoginActivity,
                                        HomeDashboardActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
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
