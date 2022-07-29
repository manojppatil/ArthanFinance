package com.av.arthanfinance.serviceRequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.R
import com.av.arthanfinance.databinding.ActivityLoanPreClosureBinding
import com.av.arthanfinance.databinding.ActivityPaymentDuesBinding

class LoanPreClosureActivity : AppCompatActivity() {
    private lateinit var activityLoanPreClosureBinding: ActivityLoanPreClosureBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanPreClosureBinding = ActivityLoanPreClosureBinding.inflate(layoutInflater)
        val view = activityLoanPreClosureBinding.root
        setContentView(view)

        setSupportActionBar(activityLoanPreClosureBinding.tbLoanPreClosure)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "Pre-Closure Of Loan"

        activityLoanPreClosureBinding.tbLoanPreClosure.setNavigationOnClickListener { v ->
            onBackPressed()
            finish()
        }
    }
}