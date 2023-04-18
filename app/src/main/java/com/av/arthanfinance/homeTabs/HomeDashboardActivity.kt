package com.av.arthanfinance.homeTabs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.profile.ProfileFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.razorpay.PaymentResultListener
import kotlinx.android.synthetic.main.activity_no_loan_dashboard.*

class HomeDashboardActivity : BaseActivity(), PaymentResultListener {

    private val noLoanHomeFragment = NoLoanHomeFragment()
    private val loansTabFragment = LoansTabFragment()
    private val businessTabFragment = BusinessTabFragment()
    private val profileTabFragment = ProfileFragment()
    private val fragmentManager = supportFragmentManager
    private var activeFragment: Fragment = noLoanHomeFragment
    private var isLoansAvailable: Boolean = false
    var customerData: UserDetailsResponse? = null

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

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: UserDetailsResponse =
                gson.fromJson(json, UserDetailsResponse::class.java)
            customerData = obj

            noLoanHomeFragment.customerData = customerData
            loansTabFragment.customerData = customerData
            profileTabFragment.customerData = customerData
            businessTabFragment.customerData = customerData

            fragmentManager.beginTransaction().apply {
                add(R.id.container, profileTabFragment, "profile").hide(profileTabFragment)
                add(R.id.container, businessTabFragment, "business").hide(businessTabFragment)
                add(R.id.container, loansTabFragment, "loans").hide(loansTabFragment)
                add(R.id.container, noLoanHomeFragment, "Home")
            }.commit()

        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeTab -> {
                    fragmentManager.beginTransaction().hide(activeFragment).show(
                        noLoanHomeFragment
                    ).commit()
                    activeFragment = noLoanHomeFragment
                    true
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

    override fun onBackPressed() {
        finish()
    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(
            this@HomeDashboardActivity,
            "Success-$p0",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(
            this@HomeDashboardActivity,
            "Failure-$p0$p1",
            Toast.LENGTH_SHORT
        ).show()
    }
}