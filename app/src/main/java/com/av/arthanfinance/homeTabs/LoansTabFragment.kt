package com.av.arthanfinance.homeTabs

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.example.awesomedialog.*
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
    private lateinit var iv_logout: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        mpinStatus = mPrefs?.getString("mpinStatus", null)
        loansList = ArrayList<LoanDetails>()
        getListOfLoans()
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
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loans_tab, container, false)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        iv_logout = view.findViewById(R.id.iv_logout)
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
                    response: Response<LoansResponse>
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    try {
                        val loansResponse = response.body()
                        loansList = loansResponse?.loans
                        val adapter = LoanDetailsAdapter(loansList!!, this)
                        adapter.notifyDataSetChanged()
                        loansRecyclerview.adapter = adapter

                        val loansCount = loansList?.count()
                        activeLoansText.text = "You have $loansCount active loans"
                        availedtext.text = "Used (${getString(R.string.Rs)})"
                        totalAvailedAmount.text = response.body()!!.usedLimit
                        tv_available.text = "Available Balance (${getString(R.string.Rs)})"
                        totalAvailableAmount.text = response.body()!!.availableAmount
                        limitText.text = "Limit (${getString(R.string.Rs)})"
                        totalLimitValue.text = response.body()!!.totalLimit
                        totalDueAmount.text = response.body()!!.usedLimit

                        val percentCompleted =
                            response.body()!!.limitPercent //to be achieved from BE
                        totalLoanlimitProgressBar.max = 100
                        ObjectAnimator.ofInt(
                            totalLoanlimitProgressBar,
                            "progress",
                            percentCompleted!!
                        )
                            .setDuration(1000).start()

                        if (response.body()!!.applyStatus == "N") {
                            applyforNewLoan.isEnabled = false
                        }
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
                    startPayment(loanDetails.loanAmount.toString().trim().toInt())
                }
            })
        }
    }

    private fun startPayment(amount: Int?) {
        val co = Checkout()
        try {
            val options = JSONObject()
            options.put("name", "Arthan Finance")
            options.put("description", "Repayment of Loan")
            //You can omit the image option to fetch the image from the dashboard
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", amount!! * 100)//pass amount in currency subunits

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

//            val prefill = JSONObject()
//            prefill.put("email", "gaurav.kumar@example.com")
//            prefill.put("contact", "9876543210")
//
//            options.put("prefill", prefill)
            co.open(activity as HomeDashboardActivity, options)
        } catch (e: Exception) {
            Toast.makeText(
                activity as HomeDashboardActivity,
                "Error in payment: " + e.message,
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
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
                    response: Response<AuthenticationResponse>
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