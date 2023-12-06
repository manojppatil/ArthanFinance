package com.av.arthanfinance.serviceRequest

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.databinding.ActivityConfirmPaymentBinding
import com.av.arthanfinance.networkService.ApiClient
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmPayment : BaseActivity(), PaymentResultWithDataListener, ExternalWalletListener {
    private lateinit var activityConfirmPaymentBinding: ActivityConfirmPaymentBinding
    private var mobileNo: String? = null
    private var mCustomerId: String? = null
    private var usedLimit: Double? = 0.0
    var customerData: UserDetailsResponse? = null
    var clevertapDefaultInstance: CleverTapAPI? = null
    private lateinit var btn_confimpay: Button
    private lateinit var amount: TextInputEditText
    private lateinit var payable_amount: TextView
    var payAmt: Double = 0.0
    override val layoutId: Int
        get() = R.layout.activity_confirm_payment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityConfirmPaymentBinding =
            ActivityConfirmPaymentBinding.inflate(layoutInflater)
        setContentView(activityConfirmPaymentBinding.root)

        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        Log.e("json", json.toString())
        if (json != null) {
            val obj: UserDetailsResponse =
                gson.fromJson(json, UserDetailsResponse::class.java)
            customerData = obj
            mobileNo = customerData?.mobileNo.toString()
        }

        Checkout.preload(this@ConfirmPayment)
        val co = Checkout()
        co.setKeyID("rzp_live_06qz6CizJP1S0D")
        Checkout.sdkCheckIntegration(this@ConfirmPayment)

        payable_amount = findViewById(R.id.payable_amount)
        payable_amount.text = intent.getStringExtra("amount")
        amount = findViewById(R.id.edt_payable_amount)
        btn_confimpay = findViewById(R.id.btn_confirmpay)
        btn_confimpay.setOnClickListener {
            startPayment()
        }
    }

    private fun startPayment() {
        payAmt = amount.text.toString().toDouble()
        try {
            val activity: Activity = this
            val co = Checkout()
            val options = JSONObject()
            options.put("name", "Arthan Finance")
            options.put("description", "Repayment of Loan")
            //You can omit the image option to fetch the image from the dashboard
            options.put("image", "https://arthan.finance/assets/images/logo-blue.png")
            options.put("theme.color", "#003FB2")
            options.put("currency", "INR")
            options.put("amount", payAmt * 100)//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", "")
            prefill.put("contact", mobileNo)

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(this@ConfirmPayment, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        clevertapDefaultInstance?.pushEvent("Razorpay payment success")//added by CleverTap Assistant

        Log.e("SUCCESS1RXP", p0.toString())
        Log.e("SUCCESSp0", p1!!.userContact.toString())
//        Log.e("SUCCESSp1", p1.userEmail.toString())
        Log.e("SUCCESSp2", p1.paymentId.toString())
//        Log.e("SUCCESSp4", p1.signature.toString())
//        Log.e("SUCCESSp5", p1.data.getString("").toString())
        customerRepayment(p1.paymentId,
            p1.userContact,
            "success")
//        Checkout.clearUserData(this@ConfirmPayment)
    }

    override fun onPaymentError(p0: Int, p2: String?, p1: PaymentData?) {
        clevertapDefaultInstance?.pushEvent("Razorpay payment failure")//added by CleverTap Assistant
        Log.e("FAIL1RXP", p0.toString())
        Log.e("FAIL3RXP", p2.toString())
//        Log.e("SUCCESSp0", p1?.paymentId.toString())
//        Log.e("SUCCESSp1", p1?.userEmail.toString())
        Log.e("SUCCESSp2", p1!!.userContact.toString())
//        Log.e("SUCCESSp4", p1.signature.toString())
//        Log.e("SUCCESSp5", p1.data.getString("").toString())
        customerRepayment(p1.paymentId,
            p1.userContact,
            "failure")
    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
        Log.e("WALLETRES1", p0.toString())
        Log.e("WALLETDT", p1?.paymentId.toString())
    }


    private fun customerRepayment(
        paymentId: String,
        userContact: String,
        paymentStatus: String,
    ) {
        Log.e("repay", paymentId)
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("amount", payAmt)
        jsonObject.addProperty("userContact", userContact)
        jsonObject.addProperty("userEmail", "")
        jsonObject.addProperty("paymentStatus", paymentStatus)
        jsonObject.addProperty("razorpay_payment_id", paymentId)
        jsonObject.addProperty("razorpay_order_id", "")
        jsonObject.addProperty("razorpay_signature", "")

        Log.e("TAG", jsonObject.toString())
        ApiClient().getAuthApiService(this).customerRepayment(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    if (paymentStatus == "success") {
                        clevertapDefaultInstance?.pushEvent("Repayment entry success")//added by CleverTap Assistant
                        Toast.makeText(
                            this@ConfirmPayment,
                            "Your payment has been successfully processed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    finish()
                } else {
                    clevertapDefaultInstance?.pushEvent("Repayment entry failure")//added by CleverTap Assistant
                    Toast.makeText(
                        this@ConfirmPayment,
                        "Something went wrong, Please try again later!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Repayment entry service failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@ConfirmPayment,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}