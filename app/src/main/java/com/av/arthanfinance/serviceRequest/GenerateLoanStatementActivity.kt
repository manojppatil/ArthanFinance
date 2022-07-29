package com.av.arthanfinance.serviceRequest

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.ApplyForLoanActivity
import com.av.arthanfinance.applyLoan.model.DigioPanResponse
import com.av.arthanfinance.databinding.ActivityGenerateLoanStatementBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.JsonObject

import kotlinx.android.synthetic.main.activity_loan_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
//, PaymentResultWithDataListener
class GenerateLoanStatementActivity : AppCompatActivity() {
    private lateinit var activityGenerateLoanStatementBinding: ActivityGenerateLoanStatementBinding
    private var periodList = arrayOf("Last 1 Year","Last 2 Year", "Last 3 Year","Last 4 Year")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityGenerateLoanStatementBinding = ActivityGenerateLoanStatementBinding.inflate(layoutInflater)
        val view = activityGenerateLoanStatementBinding.root
        setContentView(view)

        setSupportActionBar(activityGenerateLoanStatementBinding.tbGenerateLs)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "Generate Loan Statement"

        activityGenerateLoanStatementBinding.tbGenerateLs.setNavigationOnClickListener { v ->
            onBackPressed()
            finish()
        }

        activityGenerateLoanStatementBinding.clDownloadPdfLs.setOnClickListener {
            activityGenerateLoanStatementBinding.clEmailLs.setBackgroundResource(R.drawable.bg_download_statement_unselected)
            activityGenerateLoanStatementBinding.clDownloadPdfLs.setBackgroundResource(R.drawable.bg_download_statement_selected)
            activityGenerateLoanStatementBinding.tvDownloadPdf.setTextColor(ContextCompat.getColor(this, R.color.white))
            activityGenerateLoanStatementBinding.tvEmailMe.setTextColor(ContextCompat.getColor(this, R.color.indigo))
        }

        activityGenerateLoanStatementBinding.clEmailLs.setOnClickListener {
            activityGenerateLoanStatementBinding.clEmailLs.setBackgroundResource(R.drawable.bg_download_statement_selected)
            activityGenerateLoanStatementBinding.clDownloadPdfLs.setBackgroundResource(R.drawable.bg_download_statement_unselected)
            activityGenerateLoanStatementBinding.tvDownloadPdf.setTextColor(ContextCompat.getColor(this, R.color.indigo))
            activityGenerateLoanStatementBinding.tvEmailMe.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        val bankNamesAdapter = ArrayAdapter(this, R.layout.emi_options, periodList)
        activityGenerateLoanStatementBinding.periodDropDown.setText(bankNamesAdapter.getItem(0).toString(), false)
        activityGenerateLoanStatementBinding.periodDropDown.setAdapter(bankNamesAdapter)

        activityGenerateLoanStatementBinding.periodDropDown.setOnClickListener {
            activityGenerateLoanStatementBinding.periodDropDown.showDropDown()
        }

        activityGenerateLoanStatementBinding.btnPayLsFee.setOnClickListener {
            createOrder()
            //startPayment()
        }
    }

    private fun createOrder() {
        var amount: Float = activityGenerateLoanStatementBinding.tvLsFee.text.toString().toFloat()
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
                    this@GenerateLoanStatementActivity,
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