package com.example.arthanfinance.homeTabs

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arthanfinance.CustomerHomeTabResponse
import com.example.arthanfinance.R
import com.example.arthanfinance.applyLoan.*
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_loans_tab.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class LoansTabFragment : Fragment() {
    private var loansList: ArrayList<LoanDetails>? = null
    var customerData: CustomerHomeTabResponse? = null
    lateinit var adapter: LoanDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loansList = ArrayList<LoanDetails>()
        sendLoanRequest()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val percentCompleted = 40 //to be achieved from BE
        totalLoanlimitProgressBar.max = 100
        ObjectAnimator.ofInt(totalLoanlimitProgressBar, "progress", percentCompleted).setDuration(1000).start()

        availedtext.text = "Availed(${getString(R.string.Rs)})"
        totalAvailedAmount.text = "5,00,000.00"
        limitText.text = "Limit(${getString(R.string.Rs)})"
        totalLimitValue.text = "10,00,000.00"
        loansRecyclerview.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayout.VERTICAL, false)



        applyforNewLoan.setOnClickListener{
            val intent = Intent(activity?.applicationContext,
                InitiateApplyLoanActivity::class.java)
            intent.putExtra("customerData", customerData)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loans_tab, container, false)
    }

    private fun sendLoanRequest() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData?.customerId)

        activity?.applicationContext?.let {
            ApiClient().getApiService(it).getLoans(jsonObject).enqueue(object :
                Callback<LoansResponse> {
                override fun onResponse(call: Call<LoansResponse>, response: Response<LoansResponse>) {
                    val loansResponse = response.body()
                    loansList = loansResponse?.loans

                    val adapter = LoanDetailsAdapter(loansList!!)
                    adapter.notifyDataSetChanged()
                    loansRecyclerview.adapter = adapter

                    val loansCount = loansList?.count()
                    activeLoansText.setText("You have ${loansCount} active loans")
                }

                override fun onFailure(call: Call<LoansResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        it,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}