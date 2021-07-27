package com.av.arthanfinance

import `in`.aabhasjindal.otptextview.OtpTextView
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OTPActivity : BaseActivity() {
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageButton
    private lateinit var otpView: OtpTextView
    private lateinit var mobileTextView: TextView
    private lateinit var apiClient: ApiClient
    override val layoutId: Int
        get() = R.layout.layout_otp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null)
            supportActionBar?.hide()

        apiClient = ApiClient()
        otpView = findViewById(R.id.otp_view)
        mobileTextView = findViewById(R.id.tv_enter_6)
        btnSubmit = findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener{
            val otp = otpView.otp
            if(otp != null && otp != "") {
                verifyOTP(otp)
            }
        }

        val mobileNo = intent.extras?.get("mobNo") as String
        val maskedMobileNo = mobileNo.substring(mobileNo.length - 2, mobileNo.length)
        mobileTextView.setText("Enter the 6 digit OTP sent on mobile number XXXXXXXX${maskedMobileNo}")

        btnBack = findViewById(R.id.img_back_otp)
        btnBack.setOnClickListener {
            this.finish()
        }
    }

    private fun verifyOTP(otp: String) {
        val customerId = intent.extras?.get("customerId") as String
        val jsonObject = JsonObject()
        showProgressDialog()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("otp", otp)
        apiClient.getApiService(this).verifyRegistrationOTP(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(this@OTPActivity,"Please enter the valid OTP",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.status == "200") {
                        val intent = Intent(this@OTPActivity, SetNewMpinActivity::class.java)
                        intent.putExtra("customerId",customerId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@OTPActivity,"Please enter the valid OTP",
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })
    }
}
