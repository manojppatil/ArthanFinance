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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_loan_eligibility_failed.*
import kotlinx.android.synthetic.main.activity_upi_mandate_success.*

class LoanEligibilityFailed : BaseActivity() {
    private lateinit var activityLoanEligibilityFailedBinding: ActivityLoanEligibilityFailedBinding
    private lateinit var btnDone: Button
    private var customerData: AuthenticationResponse? = null
    private var customerId: String = ""
    override val layoutId: Int
        get() = R.layout.activity_loan_eligibility_failed

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanEligibilityFailedBinding =
            ActivityLoanEligibilityFailedBinding.inflate(layoutInflater)
        setContentView(activityLoanEligibilityFailedBinding.root)

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
            activityLoanEligibilityFailedBinding.retryTitle.text = "We're Sorry!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to notify you that we are unable to move forward with your loan application due to poor banking parameters."
        } else if (intent.getStringExtra("from").equals("home")) {
            activityLoanEligibilityFailedBinding.retryTitle.text = "We're Sorry!"
            activityLoanEligibilityFailedBinding.retrySubtitle.text =
                "We regret to inform you that we are unable to proceed with your fund disbursement due to a server issue at your bank. Please refresh and try again later"
        }

        btnDone.setOnClickListener {
            val intent =
                Intent(this@LoanEligibilityFailed, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}