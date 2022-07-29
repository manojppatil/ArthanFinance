package com.av.arthanfinance.serviceRequest

import android.app.Activity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.DigioPanResponse
import com.av.arthanfinance.databinding.ActivityPaymentDuesBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.JsonObject

import kotlinx.android.synthetic.main.activity_loan_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
//, PaymentResultWithDataListener
class PaymentDuesActivity : AppCompatActivity() {
    private lateinit var activityPaymentDuesBinding: ActivityPaymentDuesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPaymentDuesBinding = ActivityPaymentDuesBinding.inflate(layoutInflater)
        val view = activityPaymentDuesBinding.root
        setContentView(view)

        setSupportActionBar(activityPaymentDuesBinding.tbPaymentDues)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "Payment Of Dues"

        activityPaymentDuesBinding.tvOutStandingAmount.text =  "${applicationContext.getString(R.string.Rs)} ${"5000"}"

        activityPaymentDuesBinding.tbPaymentDues.setNavigationOnClickListener { v ->
            onBackPressed()
            finish()
        }

        activityPaymentDuesBinding.btnPayOutStanding.setOnClickListener {
            createOrder()
        }
    }

    private fun createOrder() {
        var amount: Float = "5000".toFloat()
        amount *= 100
        val jsonObject = JsonObject()
        jsonObject.addProperty("currency", "INR")
        jsonObject.addProperty("receipt", "Afpl123456")
        jsonObject.addProperty("amount", amount)

        val key = ArthanFinConstants.razorPayKey
        val secret = ArthanFinConstants.razorPaySecret

        val base = "$key:$secret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getRazorPayApiService(this).order(authHeader, jsonObject).enqueue(object :
            Callback<DigioPanResponse> {
            override fun onResponse(
                call: Call<DigioPanResponse>,
                response: Response<DigioPanResponse>
            ) {
                val orderResponse = response.body()
                val orderId = orderResponse?.id
                startPayment(amount, orderId)
            }

            override fun onFailure(call: Call<DigioPanResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@PaymentDuesActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startPayment(amount: Float, orderId: String?) {
        /*val checkout = Checkout()
        checkout.setKeyID(ArthanFinConstants.razorPayKey)

        checkout.setImage(R.mipmap.ic_launcher)

        // val context = activity?.applicationContext!!
        val activity: Activity = this

        try {
            val options = JSONObject()
            options.put("name", "Arthan Finance")
            options.put("description", "Loan statement download fee")
            options.put("currency", "INR")
            options.put("amount", amount) //pass amount in currency subunits
            options.put("order_id", orderId)
            val preFill = JSONObject()
            preFill.put("email", "")
            preFill.put("contact", "7894310911")
            options.put("prefill", preFill)
            checkout.open(activity, options)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/
    }

    /*override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
    }*/
}