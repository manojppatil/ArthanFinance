package com.av.arthanfinance.serviceRequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.R
import com.av.arthanfinance.databinding.ActivityLoanDetailsBinding
import kotlinx.android.synthetic.main.activity_loan_details.*
import kotlinx.android.synthetic.main.activity_view_loans.*
import java.lang.Exception
import java.lang.StringBuilder

class LoanDetailsActivity : AppCompatActivity() {
    private lateinit var activityLoanDetailsBinding: ActivityLoanDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanDetailsBinding = ActivityLoanDetailsBinding.inflate(layoutInflater)
        setContentView(activityLoanDetailsBinding.root)

        setSupportActionBar(activityLoanDetailsBinding.tbLoanDetails)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(true)
        (this as AppCompatActivity).supportActionBar!!.title = "View Loan Details"

        activityLoanDetailsBinding.tbLoanDetails.setNavigationOnClickListener { v ->
            onBackPressed()
            finish()
        }

        val loanAccountNumber = "123456789"
        tvLoanAmount.text =  "${applicationContext.getString(R.string.Rs)} ${"500,000.00"}"
        tvMonthlyEmi.text =  "${applicationContext.getString(R.string.Rs)} ${"14,685.00"}"
        tvInterestRate.text =  "${"28.5"} ${applicationContext.getString(R.string.percent)}"
        tvTotalAmountPayable.text =  "${applicationContext.getString(R.string.Rs)} ${"5,00,000.00"}"
        tvTotalInterest.text =  "${applicationContext.getString(R.string.Rs)} ${"35,654.00"}"
        tvTotalAmountPaid.text =  "${applicationContext.getString(R.string.Rs)} ${"2,58,980.00"}"
        tvTotalAmountDue.text =  "${applicationContext.getString(R.string.Rs)} ${"2,76,674.00"}"
        tvLoanAccNum.text =  maskString(loanAccountNumber, 0, 4, 'X')

    }


    private fun maskString(strText: String?, start: Int, end: Int, maskChar: Char): String? {
        var start = start
        var end = end
        if (strText == null || strText == "") return ""
        if (start < 0) start = 0
        if (end > strText.length) end = strText.length
        if (start > end) throw Exception("End index cannot be greater than start index")
        val maskLength = end - start
        if (maskLength == 0) return strText
        val sbMaskString = StringBuilder(maskLength)
        for (i in 0 until maskLength) {
            sbMaskString.append(maskChar)
        }
        return (strText.substring(0, start)
                + sbMaskString.toString()
                + strText.substring(start + maskLength))
    }
}