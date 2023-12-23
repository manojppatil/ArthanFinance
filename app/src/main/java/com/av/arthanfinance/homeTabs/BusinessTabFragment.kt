package com.av.arthanfinance.homeTabs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.SetNewMpinActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.example.awesomedialog.*
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_business_tab.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BusinessTabFragment : Fragment() {
    var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null
    private lateinit var iv_logout: ImageButton
    private var mpinStatus: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_business_tab, container, false)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        mpinStatus = mPrefs?.getString("mpinStatus", null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loanDetailsLayout.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, ViewLoansActivity::class.java)
//            intent.putExtra("status", "1")
//            startActivity(intent)
        }

        generateLoanStatementLayout.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, ViewLoansActivity::class.java)
//            intent.putExtra("status", "2")
//            startActivity(intent)
        }

        paymentOfDuesLayout.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, ViewLoansActivity::class.java)
//            intent.putExtra("status", "3")
//            startActivity(intent)
        }

        loanPreClosureLayout.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, LoanPreClosureActivity::class.java)
//            startActivity(intent)
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