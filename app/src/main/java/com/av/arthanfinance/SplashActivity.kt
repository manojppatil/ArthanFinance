package com.av.arthanfinance

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.applyLoan.LoanEligibilitySubmittedActivity
import com.av.arthanfinance.applyLoan.model.CompletedScreensResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.introductionPager.IntroductionPagerActivity
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadAadharActivity
import com.av.arthanfinance.user_kyc.UploadBankDetailsActivity
import com.av.arthanfinance.user_kyc.UploadPanActivity
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private var customerData: CustomerHomeTabResponse? = null
    private var customerID: String = ""
    var clevertapDefaultInstance: CleverTapAPI? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)   //Initializing the CleverTap SDK
        CleverTapAPI.createNotificationChannel(applicationContext,
            "arthik_channel",
            "mychannel",
            "lDescription",
            NotificationManager.IMPORTANCE_MAX,
            true)        //added by CleverTap Assistant

        clevertapDefaultInstance?.pushEvent("Splash Launched")

        if (supportActionBar != null)
            supportActionBar?.hide()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if (json != null) {
                val obj: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                customerData = obj
                customerID = customerData?.customerId.toString()
            }

            if (customerID == "") {
                val intent = Intent(this@SplashActivity, IntroductionPagerActivity::class.java)
                startActivity(intent)
                finish()
            } else if (mPrefs!!.getBoolean(ArthanFinConstants.isMpinSet, true)) {
                val intent = Intent(this@SplashActivity, MPINLoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                getLastCompletedScreen(customerID)
            }
        }, 2000) // 3000 is the delayed time in milliseconds.
    }

    private fun getLastCompletedScreen(customerId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)

        ApiClient().getAuthApiService(this).getCompletedScreens(jsonObject).enqueue(object :
            Callback<CompletedScreensResponse> {
            override fun onFailure(call: Call<CompletedScreensResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@SplashActivity, "Logging you in...",
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
                            Intent(this@SplashActivity, UploadPanActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        for (i in 0 until lastScreenArray.size) {
                            when (screenData.screens!![i]) {
                                ArthanFinConstants.register -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        UploadPanActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.pan -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        UploadAadharActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.digilocker -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        UploadBankDetailsActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.offline_aadhar -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        UploadBankDetailsActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.bank, ArthanFinConstants.bank_stmt -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        LoanEligibilitySubmittedActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                                ArthanFinConstants.eligibility, ArthanFinConstants.business, ArthanFinConstants.business_photos,
                                ArthanFinConstants.skip_business_photos, ArthanFinConstants.enach, ArthanFinConstants.pic_pa,
                                ArthanFinConstants.agreement, ArthanFinConstants.apply_loan, ArthanFinConstants.withdraw,
                                -> {
                                    val intent = Intent(
                                        this@SplashActivity,
                                        HomeDashboardActivity::class.java
                                    )
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@SplashActivity, "Please wait...", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
//MAPS_API_KEY=AIzaSyCe9epHZuDTREfqN_WL9U1wOPq0w6m4BE0
