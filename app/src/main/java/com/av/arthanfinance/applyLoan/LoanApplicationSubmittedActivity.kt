package com.av.arthanfinance.applyLoan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.clevertap.android.sdk.CleverTapAPI

class LoanApplicationSubmittedActivity : AppCompatActivity() {
    private lateinit var btnDone: Button
    private lateinit var loanIdText: AppCompatTextView
    private var loanResponse: LoanProcessResponse? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_app_aubmitted)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        if (supportActionBar != null)
            supportActionBar?.hide()
        loanIdText = findViewById(R.id.txtLoanId)
        btnDone = findViewById(R.id.btn_done)

//        val loanId = intent.extras?.get("loanId").toString()
//        if (intent.hasExtra("loanResponse")) {
//            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
//        }
        clevertapDefaultInstance?.pushEvent("Loan applied successfully")//added by CleverTap Assistant

//        loanIdText.text = loanId
        btnDone.setOnClickListener {
            val intent = Intent(this, HomeDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
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