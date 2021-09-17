package com.av.arthanfinance

import `in`.aabhasjindal.otptextview.OtpTextView
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.OtpResponse
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.android.volley.toolbox.HttpHeaderParser

import com.android.volley.NetworkResponse

import com.android.volley.VolleyLog

import com.android.volley.AuthFailureError

import com.android.volley.VolleyError

import com.android.volley.RequestQueue


class OTPActivity : BaseActivity() {
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageButton
    private lateinit var otpView: OtpTextView
    private lateinit var mobileTextView: TextView
    private lateinit var apiClient: ApiClient
    private var customerId: String = " "
    override val layoutId: Int
        get() = R.layout.layout_otp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null)
            supportActionBar?.hide()

        val mobileNo = intent.extras?.get("mobNo") as String
        customerId = intent.extras?.get("customerId") as String
        val maskedMobileNo = mobileNo.substring(mobileNo.length - 2, mobileNo.length)
        //mobileTextView.text = "Enter the 6 digit OTP sent on mobile number XXXXXXXX${maskedMobileNo}"

        btnBack = findViewById(R.id.img_back_otp)

        apiClient = ApiClient()
        otpView = findViewById(R.id.otp_view)
        mobileTextView = findViewById(R.id.tv_enter_6)
        btnSubmit = findViewById(R.id.btn_submit)

        sendOtp(mobileNo)

        btnSubmit.setOnClickListener {
            val otp = otpView.otp
            if (otp != null && otp != "" && otp.length == 6) {
                verifyOTP(mobileNo, otp)
            } else {
                Toast.makeText(this@OTPActivity, "Please enter the valid OTP", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnBack.setOnClickListener {
            this.finish()
        }
    }

    private fun sendOtp(mobileNo: String) {
        val jsonObject = JsonObject()
        showProgressDialog()
        jsonObject.addProperty("mobNo", mobileNo)
        jsonObject.addProperty("role", "Customer")

        apiClient.getOtpApiService(this).sendOTP(jsonObject)
            .enqueue(object : Callback<OtpResponse> {
                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    t.printStackTrace()
                    hideProgressDialog()
                    Toast.makeText(
                        this@OTPActivity,
                        "Something went wrong, please try after sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    hideProgressDialog()
                    val otpData = response.body()

                    /*if (otpData != null) {
                        if (otpData.status == "success") {
                            Toast.makeText(this@OTPActivity,"An OTP has been sent to your mobile number", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@OTPActivity,"Could not generate OTP, please try after sometime",
                                Toast.LENGTH_SHORT).show()
                        }
                    }*/
                }
            })
    }

    private fun verifyOTP(mobileNo: String, otp: String) {

        val jsonObject = JsonObject()
        showProgressDialog()
        jsonObject.addProperty("mobileNo", mobileNo)
        jsonObject.addProperty("otp", otp)
        ApiClient().getAuthApiService(this).verifyOTPForCustomer(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(this@OTPActivity, "Please enter the valid OTP", Toast.LENGTH_SHORT)
                    .show()
            }
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
                        val intent = Intent(this@OTPActivity, SetNewMpinActivity::class.java)
                        intent.putExtra("customerId", customerId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@OTPActivity, "Please enter the valid OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }else{

                }
            }
        })
    }
}