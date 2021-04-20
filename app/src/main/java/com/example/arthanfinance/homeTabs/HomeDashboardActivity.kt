package com.example.arthanfinance.homeTabs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.arthanfinance.CustomerHomeTabResponse
import com.example.arthanfinance.FingerPrintLoginActivity
import com.example.arthanfinance.MPINLoginActivity
import com.example.arthanfinance.R
import com.example.arthanfinance.applyLoan.AuthenticationResponse
import com.example.arthanfinance.applyLoan.LoanDetails
import com.example.arthanfinance.applyLoan.LoanDetailsAdapter
import com.example.arthanfinance.applyLoan.LoansResponse
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_no_loan_dashboard.*
import kotlinx.android.synthetic.main.fragment_loans_tab.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeDashboardActivity : AppCompatActivity() {

    private val noLoanHomeFragment = NoLoanHomeFragment()
    private val withLoanHomeFragment = WithLoanHomeFragment()
    private val loansTabFragment = LoansTabFragment()
    private val businessTabFragment = BusinessTabFragment()
    private val profileTabFragment = ProfileTabFragment()
    private val fragmentManager = supportFragmentManager
    private var activeFragment: Fragment = noLoanHomeFragment
    private var isLoansAvailable: Boolean = false
    var customerData: CustomerHomeTabResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_loan_dashboard)
        this.supportActionBar?.hide()

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if(json != null) {
            val obj: CustomerHomeTabResponse = gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj

            noLoanHomeFragment.customerData = customerData
            withLoanHomeFragment.customerData = customerData
            loansTabFragment.customerData = customerData


            if (isLoansAvailable) { //redirect to loans dashboard
                fragmentManager.beginTransaction().apply {
                    add(R.id.container, profileTabFragment, "profile").hide(profileTabFragment)
                    add(R.id.container, businessTabFragment, "business").hide(businessTabFragment)
                    add(R.id.container, loansTabFragment, "loans").hide(loansTabFragment)
                    add(R.id.container, withLoanHomeFragment, "Home")
                }.commit()
            } else { //redirect to new dashboard with out loans
                fragmentManager.beginTransaction().apply {
                    add(R.id.container, profileTabFragment, "profile").hide(profileTabFragment)
                    add(R.id.container, businessTabFragment, "business").hide(businessTabFragment)
                    add(R.id.container, loansTabFragment, "loans").hide(loansTabFragment)
                    add(R.id.container, noLoanHomeFragment, "Home")
                }.commit()
            }
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeTab -> {
                    if(isLoansAvailable){
                        fragmentManager.beginTransaction().hide(activeFragment).show(withLoanHomeFragment).commit()
                        activeFragment = withLoanHomeFragment
                        true
                    } else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(noLoanHomeFragment).commit()
                        activeFragment = noLoanHomeFragment
                        true
                    }

                }
                R.id.loansTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(loansTabFragment).commit()
                    activeFragment = loansTabFragment
                    true
                }
                R.id.businessTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(businessTabFragment).commit()
                    activeFragment = businessTabFragment
                    true
                }
                R.id.profileTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(profileTabFragment).commit()
                    activeFragment = profileTabFragment
                    true
                }
                else -> false
            }
        }
    }

    fun logoutClicked(view: View) {
        logOut()
    }

    private fun logOut() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData?.customerId)
        val context = applicationContext
        if (context != null) {
            ApiClient().getApiService(context).logOut(jsonObject).enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>
                ) {
                    val custData = response.body()
                    if (custData != null && custData.message?.trim() == "Success") {
                        val intent = Intent(this@HomeDashboardActivity, MPINLoginActivity::class.java)
                        startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@HomeDashboardActivity, "LogOut Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

}