package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.av.arthanfinance.InitiateApplyLoanActivity2
import com.av.arthanfinance.R
import com.av.arthanfinance.homeTabs.HomeDashboardActivity

class LoanEligibilitySubmittedActivity : AppCompatActivity() {
    private lateinit var btnDone: Button
    private lateinit var loanIdText: AppCompatTextView
    private var loanResponse: LoanProcessResponse? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_eligibility_submitted)

        if (supportActionBar != null)
            supportActionBar?.hide()
        loanIdText = findViewById(R.id.txtElLoanAmount)
        btnDone = findViewById(R.id.btn_done)

        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
        }

        val loanId = loanResponse?.loanId
        val eligilityAmount = loanResponse?.eligilityAmount
//        loanIdText.text = loanId + eligilityAmount

        btnDone.setOnClickListener {
            val intent = Intent(this, HomeDashboardActivity::class.java)
            intent.putExtra("loanResponse", loanResponse)
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