package com.av.arthanfinance.serviceRequest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.databinding.ActivityLoanPreClosureBinding

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

        activityLoanPreClosureBinding.callbutton.setOnClickListener {
            callPhone()
        }
    }

    private fun callPhone() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "8007339339"))
        startActivity(intent)
    }
}