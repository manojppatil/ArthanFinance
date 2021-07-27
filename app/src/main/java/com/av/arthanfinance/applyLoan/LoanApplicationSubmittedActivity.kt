package com.av.arthanfinance.applyLoan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import com.av.arthanfinance.R
import com.av.arthanfinance.homeTabs.HomeDashboardActivity

class LoanApplicationSubmittedActivity : AppCompatActivity() {
    private lateinit var btnDone: Button
    private lateinit var loanIdText: AppCompatTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_app_aubmitted)

        if (supportActionBar != null)
            supportActionBar?.hide()
        loanIdText = findViewById(R.id.txtLoanId)
        btnDone = findViewById(R.id.btn_done)

        val loanId = intent.extras?.get("loanId").toString()
        loanIdText.setText(loanId)
        btnDone.setOnClickListener {
            val intent = Intent(this,HomeDashboardActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack();
        }else {
            super.onBackPressed();
        }
    }
}