package com.av.arthanfinance.homeTabs

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.profile.ProfileFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_no_loan_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeDashboardActivity : BaseActivity() {

    private val noLoanHomeFragment = NoLoanHomeFragment()
    private val withLoanHomeFragment = WithLoanHomeFragment()
    private val loansTabFragment = LoansTabFragment()
    private val businessTabFragment = BusinessTabFragment()
    private val profileTabFragment = ProfileFragment()
    private val fragmentManager = supportFragmentManager
    private var activeFragment: Fragment = noLoanHomeFragment
    private var isLoansAvailable: Boolean = false
    var customerData: CustomerHomeTabResponse? = null

    override val layoutId: Int
        get() = R.layout.activity_no_loan_dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            //Toast.makeText(baseContext, ""+token, Toast.LENGTH_SHORT).show()
        })

//        FinBox.createUser("FLMtON74y17xzANePjLp7aMtP98Za0Iv3ig2AmPr", "PGC12345",
//            object : FinBox.FinBoxAuthCallback {
//                override fun onSuccess(accessToken: String) {
//                    val finBox = FinBox()
//                    finBox.startPeriodicSync()
//                }
//
//                override fun onError(@FinBoxErrorCode errorCode: Int) {
//                    Toast.makeText(this@HomeDashboardActivity, "" + errorCode, Toast.LENGTH_SHORT).show()
//                }
//            })

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj

            noLoanHomeFragment.customerData = customerData
            withLoanHomeFragment.customerData = customerData
            loansTabFragment.customerData = customerData
            profileTabFragment.customerData = customerData

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
                    if (isLoansAvailable) {
                        fragmentManager.beginTransaction().hide(activeFragment).show(
                            withLoanHomeFragment
                        ).commit()
                        activeFragment = withLoanHomeFragment
                        true
                    } else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(
                            noLoanHomeFragment
                        ).commit()
                        activeFragment = noLoanHomeFragment
                        true
                    }

                }
                R.id.loansTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(loansTabFragment)
                        .commit()
                    activeFragment = loansTabFragment
                    true
                }
                R.id.businessTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment)
                        .show(businessTabFragment).commit()
                    activeFragment = businessTabFragment
                    true
                }
                R.id.profileTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(profileTabFragment)
                        .commit()
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
        showProgressDialog("Logging out...")
        val context = applicationContext
        if (context != null) {
            ApiClient().getAuthApiService(context).logOut(jsonObject).enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>
                ) {
                    hideProgressDialog()
                    val custData = response.body()
                    if (custData != null && custData.message.trim() == "Success") {
                        val intent =
                            Intent(this@HomeDashboardActivity, MPINLoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(this@HomeDashboardActivity, "LogOut Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }

    override fun onBackPressed() {
        finish()
    }
}