package com.av.arthanfinance.serviceRequest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.adapter.ViewLoansListAdapter
import com.av.arthanfinance.applyLoan.model.LoanDetails
import com.av.arthanfinance.applyLoan.model.LoansResponse
import com.av.arthanfinance.databinding.ActivityViewLoansBinding
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_view_loans.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ViewLoansActivity : AppCompatActivity() {
    private var loansList: ArrayList<LoanDetails>? = null
    private var customerData: CustomerHomeTabResponse? = null
    private var status = ""
    private lateinit var activityViewLoansBinding : ActivityViewLoansBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewLoansBinding = ActivityViewLoansBinding.inflate(layoutInflater)
        setContentView(activityViewLoansBinding.root)

        status = intent.getStringExtra("status").toString()

        setSupportActionBar(tbLoanList)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "View Loans"

        activityViewLoansBinding.tbLoanList.setNavigationOnClickListener { v ->
            onBackPressed()
            finish()
        }

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if(json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
        }

        rvLoans.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        getListOfLoans()
    }

    private fun getListOfLoans() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData?.customerId)

            ApiClient().getAuthApiService(this).getCustomerApplications(jsonObject).enqueue(object :
                Callback<LoansResponse>{
                override fun onResponse(call: Call<LoansResponse>, response: Response<LoansResponse>) {
                    val loansResponse = response.body()
                    loansList = loansResponse?.loans
                    if (loansList!!.size == 0){
                        activityViewLoansBinding.nsvLoanList.visibility = View.GONE
                        activityViewLoansBinding.tvNoDataFound.visibility = View.VISIBLE
                    }else{
                        val adapter = ViewLoansListAdapter(loansList, this@ViewLoansActivity, status)
                        rvLoans.adapter = adapter
                    }
                }

                override fun onFailure(call: Call<LoansResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@ViewLoansActivity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                    activityViewLoansBinding.nsvLoanList.visibility = View.GONE
                    activityViewLoansBinding.tvNoDataFound.visibility = View.VISIBLE
                }
            })
    }
}