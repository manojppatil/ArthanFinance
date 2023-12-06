package com.av.arthanfinance.homeTabs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.profile.ProfileFragment
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_no_loan_dashboard.*


class HomeDashboardActivity : BaseActivity() {

    private val noLoanHomeFragment = NoLoanHomeFragment()
    private val loansTabFragment = LoansTabFragment()
    private val businessTabFragment = BusinessTabFragment()
    private val profileTabFragment = ProfileFragment()
    private val fragmentManager = supportFragmentManager
    private var activeFragment: Fragment = noLoanHomeFragment
    var customerData: UserDetailsResponse? = null
    private var appUpdate: AppUpdateManager? = null
    private val REQUEST_CODE = 100
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_no_loan_dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()

        appUpdate = AppUpdateManagerFactory.create(this@HomeDashboardActivity)
        checkUpdate()
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        clevertapDefaultInstance?.pushEvent("Home dashboard visited")//added by CleverTap Assistant

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
                add(R.id.container, noLoanHomeFragment, "Home")
                add(R.id.container, loansTabFragment, "loans").hide(loansTabFragment)
                add(R.id.container, businessTabFragment, "business").hide(businessTabFragment)
                add(R.id.container, profileTabFragment, "profile").hide(profileTabFragment)
            }.commit()

        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeTab -> {
                    clevertapDefaultInstance?.pushEvent("Home item clicked")//added by CleverTap Assistant
                    fragmentManager.beginTransaction().hide(activeFragment).show(
                        noLoanHomeFragment
                    ).commit()
                    activeFragment = noLoanHomeFragment
                    true
                }
                R.id.loansTab -> {
                    clevertapDefaultInstance?.pushEvent("Loans item clicked")//added by CleverTap Assistant
                    fragmentManager.beginTransaction().hide(activeFragment).show(loansTabFragment)
                        .commit()
                    activeFragment = loansTabFragment
                    true
                }
                R.id.businessTab -> {
                    clevertapDefaultInstance?.pushEvent("Business item clicked")//added by CleverTap Assistant
                    fragmentManager.beginTransaction().hide(activeFragment)
                        .show(businessTabFragment).commit()
                    activeFragment = businessTabFragment
                    true
                }
                R.id.profileTab -> {
                    clevertapDefaultInstance?.pushEvent("Profile item clicked")//added by CleverTap Assistant
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
        clevertapDefaultInstance?.pushEvent("Back from Homepage")//added by CleverTap Assistant
        finish()
    }

    fun checkUpdate() {
        appUpdate?.appUpdateInfo?.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdate!!.startUpdateFlowForResult(updateInfo,
                    AppUpdateType.IMMEDIATE,
                    this@HomeDashboardActivity,
                    REQUEST_CODE)

            }
        }
    }

    override fun onResume() {
        super.onResume()
        inProgressUpdate()
    }

    fun inProgressUpdate() {
        appUpdate?.appUpdateInfo?.addOnSuccessListener { updateInfo ->

            if (updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdate!!.startUpdateFlowForResult(updateInfo,
                    AppUpdateType.IMMEDIATE,
                    this@HomeDashboardActivity,
                    REQUEST_CODE)
            }
        }
    }
}