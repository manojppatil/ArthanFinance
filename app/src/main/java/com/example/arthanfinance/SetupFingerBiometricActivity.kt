package com.example.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.arthanfinance.applyLoan.AuthenticationResponse
import com.example.arthanfinance.homeTabs.HomeDashboardActivity
import com.example.arthanfinance.introductionPager.Intro1Fragment
import com.example.arthanfinance.introductionPager.Intro2Fragment
import com.example.arthanfinance.introductionPager.IntroPagerAdapter
import com.example.arthanfinance.introductionPager.LoginFragment
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetupFingerBiometricActivity : AppCompatActivity() {
    private lateinit var btnNotNow: Button
    private lateinit var btnEnableFingerPrint: Button
    private lateinit var btnBack: ImageButton
    private lateinit var apiClient: ApiClient
    private var isLoansAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_set_up_bio_finger)

        if (supportActionBar != null)
            supportActionBar?.hide()

        apiClient = ApiClient()
        btnNotNow = findViewById(R.id.btn_not_now)
        btnNotNow.setOnClickListener{
            setCustomerBiometric("N")
        }

        btnBack = findViewById(R.id.img_back_biometricfinger)
        btnBack.setOnClickListener {
            this.finish()
        }

        btnEnableFingerPrint = findViewById(R.id.btn_enable_fingerprint)
        btnEnableFingerPrint.setOnClickListener {
            setCustomerBiometric("Y")
        }
    }

    private fun setCustomerBiometric(bioFlag: String) {
        val customerId = intent.extras?.get("customerId") as String
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("bioFlag", bioFlag)
        apiClient.getApiService(this).setCustomerBiometric(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@SetupFingerBiometricActivity,"BioMetric setup Failed Please try after some time",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                val custData = response.body()
                if (custData != null) {
                    if (custData.status == "200") {
//                        val intent = Intent(this@SetupFingerBiometricActivity, HomeDashboardActivity::class.java)
//                        intent.putExtra("customerId",customerId)
//                        startActivity(intent)

                        val intent = Intent(this@SetupFingerBiometricActivity, FingerPrintLoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SetupFingerBiometricActivity,"BioMetric setup Failed Please try after some time",
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })
    }
}