package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.RegistrationActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.databinding.ActivityLoanEligibilityFailedBinding
import com.av.arthanfinance.serviceRequest.Getintouch
import com.av.arthanfinance.user_kyc.UploadBankDetailsActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_loan_eligibility_failed.*
import kotlinx.android.synthetic.main.activity_upi_mandate_success.*

class LoanEligibilityFailed : BaseActivity() {
    private lateinit var activityLoanEligibilityFailedBinding: ActivityLoanEligibilityFailedBinding
    private lateinit var btnDone: Button
    private var customerData: AuthenticationResponse? = null
    private var customerId: String = ""
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_loan_eligibility_failed

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanEligibilityFailedBinding =
            ActivityLoanEligibilityFailedBinding.inflate(layoutInflater)
        setContentView(activityLoanEligibilityFailedBinding.root)

        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: AuthenticationResponse =
                gson.fromJson(json, AuthenticationResponse::class.java)
            customerData = obj
            customerId = customerData!!.customerId.toString()
        }

        if (supportActionBar != null)
            supportActionBar?.hide()
        btnDone = findViewById(R.id.btn_retry)

        if (intent.getStringExtra("from").equals("banking")) {
            clevertapDefaultInstance?.pushEvent("Banking not completed")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.retryTitle.text = "We're Sorry!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to notify you that we are unable to move forward with your loan application due to poor banking parameters."
        } else if (intent.getStringExtra("rejectCode").equals("401")) {
            clevertapDefaultInstance?.pushEvent("Reject by bureau")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.btnRetry.visibility = View.GONE
            activityLoanEligibilityFailedBinding.retryTitle.text = "We're Sorry!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to notify you that we have processed your application and are unable to meet the desired bureau score for further processing in compliance with our internal criteria."
        } else if (intent.getStringExtra("rejectCode").equals("402")) {
            clevertapDefaultInstance?.pushEvent("Reject by banking")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.retryTitle.text = "We're Sorry!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to notify you that we are unable to move forward with your loan transaction due to poor banking parameters."
        } else if (intent.getStringExtra("rejectCode").equals("403")) {
            clevertapDefaultInstance?.pushEvent("Eligibility not calculated")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.btnRetry.visibility = View.GONE
            activityLoanEligibilityFailedBinding.retryTitle.text = "Try After some time!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to inform you that we are unable to proceed with your fund disbursement due to a server issue at your bank. Please refresh and try again later."
        } else if (intent.getStringExtra("rejectCode").equals("404")) {
            activityLoanEligibilityFailedBinding.btnRetry.visibility = View.GONE
            clevertapDefaultInstance?.pushEvent("Reject by banking and bureau")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.retryTitle.text = "Not Eligible!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to inform you that we are unable to proceed with your loan application."
        } else if (intent.getStringExtra("rejectCode").equals("200")) {
            clevertapDefaultInstance?.pushEvent("Reject by internal criteria")//added by CleverTap Assistant
            activityLoanEligibilityFailedBinding.retryTitle.text = "Not Eligible!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to notify you that we have processed your application and are unable to meet the desired banking eligibility for further processing in compliance with our internal criteria."
        }

        btnDone.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("User retry with different bank")//added by CleverTap Assistant
            val intent =
                Intent(this@LoanEligibilityFailed, UploadBankDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            clevertapDefaultInstance?.pushEvent("Back from eligibility failed")//added by CleverTap Assistant

        }
    }
}