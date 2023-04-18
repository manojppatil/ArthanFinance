package com.av.arthanfinance.biometric

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.av.arthanfinance.FingerPrintLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetupFingerBiometricActivity : AppCompatActivity() {
    private lateinit var btnNotNow: Button
    private lateinit var btnEnableFingerPrint: Button
    private lateinit var btnBack: ImageButton
    private lateinit var apiClient: ApiClient
    private var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_set_up_bio_finger)

        if (supportActionBar != null)
            supportActionBar?.hide()

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: UserDetailsResponse =
                gson.fromJson(json, UserDetailsResponse::class.java)
            customerData = obj
            mCustomerId = customerData!!.customerId
        }

        apiClient = ApiClient()
        btnNotNow = findViewById(R.id.btn_not_now)
        btnNotNow.setOnClickListener {
            val sharedPref: SharedPreferences =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(ArthanFinConstants.isFingerPrintSet, false)
            editor.apply()
//            setCustomerBiometric("N")
        }

        btnBack = findViewById(R.id.img_back_biometricfinger)
        btnBack.setOnClickListener {
            this.finish()
        }

        btnEnableFingerPrint = findViewById(R.id.btn_enable_fingerprint)
        btnEnableFingerPrint.setOnClickListener {
            val sharedPref: SharedPreferences =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(ArthanFinConstants.isFingerPrintSet, true)
            editor.apply()
//            setCustomerBiometric("Y")
        }
    }

    private fun setCustomerBiometric(bioFlag: String) {
        val progressDialog = ProgressDialog(this@SetupFingerBiometricActivity)
        progressDialog.show()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("bioFlag", bioFlag)
        apiClient.getApiService(this).setCustomerBiometric(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                progressDialog.dismiss()
                Toast.makeText(
                    this@SetupFingerBiometricActivity,
                    "BioMetric setup Failed Please try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                val custData = response.body()
                progressDialog.dismiss()
                if (custData != null) {
                    if (custData.status == "200") {
                        val sharedPref: SharedPreferences =
                            getSharedPreferences("isFingerprintSet", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isFingerprintSet", true)
                        editor.apply()
                        val intent = Intent(
                            this@SetupFingerBiometricActivity,
                            FingerPrintLoginActivity::class.java
                        )
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@SetupFingerBiometricActivity,
                            "BioMetric setup Failed Please try after some time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        })
    }
}