package com.av.arthanfinance

import `in`.aabhasjindal.otptextview.OtpTextView
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.Nullable
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.OtpResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.networkService.Customer
import com.av.arthanfinance.networkService.DatabaseHandler
import com.av.arthanfinance.util.SmsBroadcastReceiver
import com.av.arthanfinance.util.SmsBroadcastReceiver.SmsBroadcastReceiverListener
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Matcher
import java.util.regex.Pattern


class OTPActivity : BaseActivity() {
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageButton

    private lateinit var otpView: OtpTextView
    private lateinit var mobileTextView: TextView
    private lateinit var apiClient: ApiClient
    private var customerId: String = ""
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var mobileNo: String
    override val layoutId: Int
        get() = R.layout.layout_otp
    private val REQ_USER_CONSENT = 200
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null)
            supportActionBar?.hide()

        mobileNo = intent.getStringExtra("mobNo").toString()
        name = intent.getStringExtra("name").toString()
        email = intent.getStringExtra("email").toString()
//        customerId = intent.extras?.get("customerId") as String
        val maskedMobileNo = mobileNo.substring(mobileNo.length - 2, mobileNo.length)
        //mobileTextView.text = "Enter the 6 digit OTP sent on mobile number XXXXXXXX${maskedMobileNo}"

        btnBack = findViewById(R.id.img_back_otp)

        apiClient = ApiClient()
        otpView = findViewById(R.id.otp_view)
        mobileTextView = findViewById(R.id.tv_enter_6)
        btnSubmit = findViewById(R.id.btn_submit)

        sendOtp(mobileNo)

        val maskedMobileNum = maskString(mobileNo, 0, 8, 'X')
        mobileTextView.text = "Enter the 6 digit OTP sent on mobile number $maskedMobileNum"

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

        startSmsUserConsent()
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(this)
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener {
            Log.e("TAG", "On Success")
        }.addOnFailureListener {
            Log.e("Error", "On OnFailure")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message!!)
            }
        }
    }

    private fun getOtpFromMessage(message: String) {
        // This will match any 6 digit number in the message
        val pattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            otpView.otp = matcher.group(0)
            verifyOTP(mobileNo, otpView.otp)
        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    @Throws(Exception::class)
    private fun maskString(strText: String?, start: Int, end: Int, maskChar: Char): String {
        var start = start
        var end = end
        if (strText == null || strText == "") return ""
        if (start < 0) start = 0
        if (end > strText.length) end = strText.length
        if (start > end) throw Exception("End index cannot be greater than start index")
        val maskLength = end - start
        if (maskLength == 0) return strText
        val sbMaskString = StringBuilder(maskLength)
        for (i in 0 until maskLength) {
            sbMaskString.append(maskChar)
        }
        return (strText.substring(0, start)
                + sbMaskString.toString()
                + strText.substring(start + maskLength))
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
                        registerCustomerData()
                    } else {
                        Toast.makeText(
                            this@OTPActivity, "Please enter the valid OTP",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {

                }
            }
        })
    }

    private fun registerCustomerData() {
        val name = name
        val email = email
        val mobile = mobileNo

        showProgressDialog()
        //Need to create GSON JsonObject rather than creating a new class object and sending the data in the API Calls
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("mobNo", mobile)
//        jsonObject.addProperty("dob", dob)
        jsonObject.addProperty("emailId", email)
        jsonObject.addProperty("consent", "Y")
        jsonObject.addProperty("userLanguage", "English")
        ApiClient().getAuthApiService(this).registerCustomer(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@OTPActivity,
                    "Registration failure. Try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
//                        registerFinbox(custData.customerId!!)
                        custData.customerId?.let {
                            saveCustomerData(name, email, mobile, it)
                        }
                    } else {
                        Log.e("Error", custData.message + "")
                    }
                }
            }
        })
    }

    //method for saving records in database
    private fun saveCustomerData(
        name: String,
        email: String,
        mobile: String,
        custId: String
    ) {
        val databaseHandler = DatabaseHandler(this)
        if (mobile.trim() != "") {
            val status =
                databaseHandler.saveCustomer(Customer(name, email, mobile, custId, null))
            val intent = Intent(this@OTPActivity, SetNewMpinActivity::class.java)
            intent.putExtra("customerId", custId)
            intent.putExtra("mob", mobileNo)
            intent.putExtra("fbtoken", "")
            startActivity(intent)
            finish()
            if (status > -1) {
                Log.e("Success", "Record saved to DB")
            } else if (status == (-2).toLong()) {
                Log.e("Warning", "Record already exists.")
            }
        } else {
            Log.e("Error", "mobile no cannot be blank")
        }
    }


//    private fun registerFinbox(customerId: String) {
//        FinBox.createUser("FLMtON74y17xzANePjLp7aMtP98Za0Iv3ig2AmPr", customerId,
//            object : FinBox.FinBoxAuthCallback {
//                override fun onSuccess(accessToken: String) {
//                    val finBox = FinBox()
//                    finBox.startPeriodicSync()
////                    Toast.makeText(
////                        this@OTPActivity,
////                        "FinBox Registration success",
////                        Toast.LENGTH_SHORT
////                    ).show()
//                    val intent = Intent(this@OTPActivity, SetNewMpinActivity::class.java)
//                    intent.putExtra("customerId", customerId)
//                    intent.putExtra("mob", mobileNo)
//                    intent.putExtra("fbtoken", accessToken)
//                    startActivity(intent)
//                    finish()
//                }
//
//                override fun onError(@FinBoxErrorCode errorCode: Int) {
//                    Log.e("Error", "" + errorCode)
//                }
//            })
//    }
}
