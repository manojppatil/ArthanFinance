package com.av.arthanfinance.homeTabs

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.applyLoan.InitiateApplyLoanActivity2
import com.av.arthanfinance.R
import com.av.arthanfinance.SetNewMpinActivity
import com.av.arthanfinance.adapter.LoanDetailsAdapter
import com.av.arthanfinance.adapter.LoanItemClickListener
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanDetails
import com.av.arthanfinance.applyLoan.model.LoansResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.serviceRequest.ConfirmPayment
import com.av.arthanfinance.serviceRequest.TransactionDetails
import com.av.arthanfinance.util.ArthanFinConstants
import com.example.awesomedialog.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.razorpay.Checkout
import kotlinx.android.synthetic.main.fragment_loans_tab.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class LoansTabFragment : Fragment() {
    private var loansList: ArrayList<LoanDetails>? = null
    var customerData: UserDetailsResponse? = null
    lateinit var adapter: LoanDetailsAdapter
    private var mCustomerId: String? = null
    private var mpinStatus: String? = null
    private var usedLimit: String? = null
    private var availableLimit: String? = null
    private var totalLimit: String? = null
    private lateinit var iv_logout: ImageButton
    private lateinit var pay_btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        Log.e("json", json.toString())
        if (json != null) {
            val obj: UserDetailsResponse =
                gson.fromJson(json, UserDetailsResponse::class.java)
            customerData = obj
            usedLimit = customerData?.borrowedAmount
            availableLimit = customerData!!.availableAmount
            totalLimit = customerData!!.eligibilityAmount

        }
        mpinStatus = mPrefs?.getString("mpinStatus", null)
        loansList = ArrayList<LoanDetails>()
        if (mCustomerId != null) {
            getListOfLoans()
        } else {
            Log.e("ERROR", "Missing customer Id at Loan")
        }
        Checkout.preload(activity as HomeDashboardActivity)
        val co = Checkout()
        co.setKeyID("rzp_live_06qz6CizJP1S0D")
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val percentCompleted = 10 //to be achieved from BE
        totalLoanlimitProgressBar.max = 100
        ObjectAnimator.ofInt(totalLoanlimitProgressBar, "progress", percentCompleted)
            .setDuration(1000).start()

        loansRecyclerview.layoutManager =
            LinearLayoutManager(activity?.applicationContext, RecyclerView.VERTICAL, false)
        applyforNewLoan.setOnClickListener {
            val intent =
                Intent(activity?.applicationContext, InitiateApplyLoanActivity2::class.java)
            intent.putExtra(ArthanFinConstants.IS_CREATE_FLOW, true)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loans_tab, container, false)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        iv_logout = view.findViewById(R.id.iv_logout)
        pay_btn = view.findViewById(R.id.payBtn)
        iv_logout.setOnClickListener {
            if (mpinStatus == "Complete") {
                logOut()
            } else {
                AwesomeDialog.build(activity as HomeDashboardActivity)
                    .title("Warning")
                    .body("Please set your MPIN before Logging out")
                    .icon(R.drawable.ic_info_icon)
                    .onPositive("Set MPIN now") {
                        val intent = Intent(
                            activity,
                            SetNewMpinActivity::class.java
                        )
                        startActivity(intent)
                    }
            }
        }
        pay_btn.setOnClickListener {
            val intent = Intent(
                context,
                ConfirmPayment::class.java
            )
            intent.putExtra("amount", totalDueAmount.text)
            startActivity(intent)
        }

        return view
    }

    private fun getListOfLoans() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        activity?.applicationContext?.let {
            ApiClient().getAuthApiService(it).getCustomerApplications(jsonObject).enqueue(object :
                Callback<LoansResponse>, LoanItemClickListener {
                @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
                override fun onResponse(
                    call: Call<LoansResponse>,
                    response: Response<LoansResponse>,
                ) {
                    try {
                        (activity as HomeDashboardActivity).hideProgressDialog()
                        val loansResponse = response.body()
                        loansList = loansResponse?.loans
                        val adapter = LoanDetailsAdapter(loansList!!, this)
                        adapter.notifyDataSetChanged()
                        loansRecyclerview.adapter = adapter

//                        val loansCount = loansList?.count()
//                        availedtext.text = "Used (${getString(R.string.Rs)})"
                        Log.e("Logused", usedLimit + "")
//                        totalAvailedAmount.text = usedLimit
//                        tv_available.text = "Available Balance (${getString(R.string.Rs)})"
//                        totalAvailableAmount.text = availableLimit
//                        limitText.text = "Limit (${getString(R.string.Rs)})"
//                        totalLimitValue.text = totalLimit
                        totalDueAmount.text = loansResponse!!.totalDebitAmount.toString()

//                        val percentCompleted =
//                            response.body()!!.limitPercent //to be achieved from BE
//                        totalLoanlimitProgressBar.max = 100
//                        ObjectAnimator.ofInt(
//                            totalLoanlimitProgressBar,
//                            "progress",
//                            percentCompleted!!
//                        )
//                            .setDuration(1000).start()

//                        if (response.body()!!.applyStatus == "N") {
//                            applyforNewLoan.isEnabled = false
//                        }
                    } catch (ex: NullPointerException) {
                        ex.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<LoansResponse>, t: Throwable) {
                    t.printStackTrace()
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    Toast.makeText(
                        it,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onLoanItemClicked(loanDetails: LoanDetails) {
                    val intent = Intent(
                        context,
                        TransactionDetails::class.java
                    )
                    intent.putExtra("accountId", loanDetails.accountId)
                    intent.putExtra("transactionName", loanDetails.transactionName)
                    intent.putExtra("transactionDateStr", loanDetails.transactionDateStr)
                    intent.putExtra("accountEntryType", loanDetails.accountEntryType)
                    intent.putExtra("amount", loanDetails.amount)
                    intent.putExtra("description", loanDetails.description)
                    intent.putExtra("transactionId", loanDetails.transactionId)
                    startActivity(intent)
                }
            })
        }
    }

    private fun logOut() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        (activity as HomeDashboardActivity).showProgressDialog("Logging out...")
        if (context != null) {
            ApiClient().getAuthApiService(requireContext()).logOut(jsonObject).enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>,
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    val custData = response.body()
                    val sharedPref: SharedPreferences =
                        (activity as HomeDashboardActivity).getSharedPreferences(
                            "customerData",
                            Context.MODE_PRIVATE
                        )
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    if (custData != null && custData.message.trim() == "Success") {
                        val intent =
                            Intent(activity, MPINLoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        (activity as HomeDashboardActivity).finish()
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(activity, "LogOut Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}