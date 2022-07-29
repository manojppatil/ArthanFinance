package com.av.arthanfinance.applyLoan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.InitiateApplyLoanActivity2
import com.av.arthanfinance.R

class ApplyCoupon : AppCompatActivity() {
    private lateinit var btnDone: Button
    private var loanResponse: LoanProcessResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_coupon)

        if (supportActionBar != null)
            supportActionBar?.hide()
        btnDone = findViewById(R.id.btn_done)

        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
        }

        val loanId = loanResponse?.loanId

        btnDone.setOnClickListener {
            val intent = Intent(this, LoanApplicationSubmittedActivity::class.java)
            intent.putExtra("loanResponse", loanResponse)
            intent.putExtra("loanId", loanId)
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