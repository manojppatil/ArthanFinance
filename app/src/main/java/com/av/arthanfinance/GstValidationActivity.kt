package com.av.arthanfinance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.GstReturnResponse
import com.av.arthanfinance.databinding.ActivityGstValidationBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadBusinessDetailsActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GstValidationActivity : BaseActivity() {
    private lateinit var gstIn: String
    private lateinit var activityGstValidationBinding: ActivityGstValidationBinding
    private lateinit var requestId: String
    override val layoutId: Int
        get() = R.layout.activity_gst_validation

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGstValidationBinding = ActivityGstValidationBinding.inflate(layoutInflater)
        setContentView(activityGstValidationBinding.root)

        setSupportActionBar(activityGstValidationBinding.tbGstValidation)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "GST Validation"

        //gstIn = intent.getStringExtra("gst_num").toString()
        activityGstValidationBinding.tieGstNum.setText("27AARCA6629H1Z8")

        activityGstValidationBinding.btnSubmitGst.setOnClickListener {
            val userName = activityGstValidationBinding.tieUserName.text.toString()
            val gstIn = activityGstValidationBinding.tieGstNum.text.toString()

            when {
                userName == "" -> {
                    Toast.makeText(this, "Provide your GST portal username", Toast.LENGTH_SHORT)
                        .show()
                }
                gstIn == "" -> {
                    Toast.makeText(this, "Provide your valid GST number", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    generateOTP(userName, gstIn)
                }
            }
        }

        activityGstValidationBinding.tbGstValidation.setNavigationOnClickListener {
            finish()
        }

        activityGstValidationBinding.btnSubmitOtp.setOnClickListener {
            val otpText = activityGstValidationBinding.etOtp.text.toString()

            if (otpText == "") {
                Toast.makeText(this, "Provide OTP before submitting.", Toast.LENGTH_SHORT).show()
            } else {
                submitOtp(otpText)
                showProgressDialog()
            }
        }

    }

    private fun submitOtp(otpText: String) {
        val jsonObject = JsonObject()
        showProgressDialog()

        jsonObject.addProperty("otp", otpText)
        jsonObject.addProperty("requestId", requestId)

        val clientId = "8jn2bAml0S4xcorY5EQM"

        val authHeader = "x-karza-key " + Base64.encodeToString(
            clientId.toByteArray(),
            Base64.NO_WRAP
        )

        ApiClient().getKarzaApiService(this).gstReturnStatus(clientId, jsonObject).enqueue(object :
            Callback<GstReturnResponse> {
            override fun onResponse(
                call: Call<GstReturnResponse>,
                response: Response<GstReturnResponse>
            ) {
                hideProgressDialog()
                val gstReturnResponse = response.body()
                val statusCode = gstReturnResponse!!.statusCode
                val requestId = gstReturnResponse!!.requestId
                saveGstRequestId2(requestId!!)
                if (statusCode == 101) {
                    val intent = Intent(
                        this@GstValidationActivity,
                        UploadBusinessDetailsActivity::class.java
                    )
                    setResult(102, intent)
                    finish()
                } else {
                    val intent = Intent(
                        this@GstValidationActivity,
                        UploadBusinessDetailsActivity::class.java
                    )
                    setResult(103, intent)
                    finish()
                    Toast.makeText(
                        this@GstValidationActivity,
                        "Unable to validate OTP. Please try again some time.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<GstReturnResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                val intent =
                    Intent(this@GstValidationActivity, UploadBusinessDetailsActivity::class.java)
                setResult(103, intent)
                finish()
                Toast.makeText(
                    this@GstValidationActivity,
                    "Service Failure, unable to validate OTP. Please try again after some time.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun generateOTP(userName: String, gstIn: String) {

        val jsonObject = JsonObject()
        showProgressDialog()

        jsonObject.addProperty("username", userName)
        jsonObject.addProperty("gstin", gstIn)
        jsonObject.addProperty("consent", "Y")
        jsonObject.addProperty("extendedPeriod", true)

        val clientId = "8jn2bAml0S4xcorY5EQM"

        ApiClient().getKarzaApiService(this).gstReturnStatus(clientId, jsonObject).enqueue(object :
            Callback<GstReturnResponse> {
            override fun onResponse(
                call: Call<GstReturnResponse>,
                response: Response<GstReturnResponse>
            ) {
                val gstReturnResponse = response.body()

                if (gstReturnResponse != null) {
                    val statusCode = gstReturnResponse.statusCode
                    requestId = gstReturnResponse.requestId.toString()
                    saveGstRequestId1(requestId!!)
                    if (statusCode == 101) {
                        activityGstValidationBinding.llGst.visibility = View.GONE
                        activityGstValidationBinding.llOtp.visibility = View.VISIBLE
                        hideProgressDialog()
                    } else {
                        hideProgressDialog()
                        Toast.makeText(
                            this@GstValidationActivity,
                            "Unable to validate GSTIN. Please try again some time.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    hideProgressDialog()
                    Toast.makeText(
                        this@GstValidationActivity,
                        "Unable to validate GSTIN. Please try again some time.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<GstReturnResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@GstValidationActivity,
                    "Service Failure, unable to validate GSTIN. Please try again after some time.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun saveGstRequestId1(requestId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", intent.getStringExtra("customerId"))
        jsonObject.addProperty("requestId", requestId)
        showProgressDialog()
        ApiClient().getAuthApiService(this).saveGstRequestId1(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@GstValidationActivity, "GST1 Failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()

            }
        })
    }

    private fun saveGstRequestId2(requestId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", intent.getStringExtra("customerId"))
        jsonObject.addProperty("requestId", requestId)
        showProgressDialog()
        ApiClient().getAuthApiService(this).saveGstRequestId2(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@GstValidationActivity, "GST1 Failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()

            }
        })
    }
}