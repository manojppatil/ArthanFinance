package com.av.arthanfinance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.GenericResponse
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_forgot_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPasswordActivity : BaseActivity() {

    private lateinit var apiClient: ApiClient

    private var isOtpViewVisible = false


    override val layoutId: Int
        get() = R.layout.layout_forgot_password



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()

        apiClient = ApiClient()

        btnAction.setOnClickListener {
            if (!isOtpViewVisible) {
                if (edtMobile.text.toString().isEmpty() || edtMobile.text.toString().length != 10) {
                    Toast.makeText(this, "Invalid Mobile Number", Toast.LENGTH_SHORT).show()
                }else{
                    reguestForOTP()
                }

            }else{
                if (edtOtp.text.toString().isEmpty()){
                    Toast.makeText(this, "Otp Empty", Toast.LENGTH_SHORT).show()
                }else{
                    validateOtp()
                }
            }

        }
    }

    private fun validateOtp() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobileNo", edtMobile.text.toString())
        jsonObject.addProperty("otp", edtOtp.text.toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).verifyOTPForCustomer(jsonObject).enqueue(object : Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {

                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@ForgotPasswordActivity, "OTP Validation Failed. Please Try Again",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val genericResponse = response.body()
                if (genericResponse != null && genericResponse.apiCode == 200.toString()) {
                    val intent = Intent(this@ForgotPasswordActivity, SetNewMpinActivity::class.java)
                    intent.putExtra("MOBILE",edtMobile.text.toString())
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    private fun reguestForOTP() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobileNo", edtMobile.text.toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).forgotMPin(jsonObject).enqueue(object :
            Callback<GenericResponse> {
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {

                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@ForgotPasswordActivity, "OTP Request Failed. Please Try Again",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>
            ) {
                hideProgressDialog()
                val genericResponse = response.body()
                if (genericResponse != null && genericResponse.apiCode == "200") {
                    lytOtp.visibility = View.VISIBLE
                    isOtpViewVisible = true
                }
            }
        })
    }
}
