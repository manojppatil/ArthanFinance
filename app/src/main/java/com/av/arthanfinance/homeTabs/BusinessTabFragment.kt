package com.av.arthanfinance.homeTabs

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.serviceRequest.LoanPreClosureActivity
import com.av.arthanfinance.serviceRequest.ViewLoansActivity
import kotlinx.android.synthetic.main.fragment_business_tab.*

class BusinessTabFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_business_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loanDetailsLayout.setOnClickListener {
            val intent = Intent(context, ViewLoansActivity::class.java)
            intent.putExtra("status", "1")
            startActivity(intent)
        }

        generateLoanStatementLayout.setOnClickListener {
            val intent = Intent(context, ViewLoansActivity::class.java)
            intent.putExtra("status", "2")
            startActivity(intent)
        }

        paymentOfDuesLayout.setOnClickListener {
            val intent = Intent(context, ViewLoansActivity::class.java)
            intent.putExtra("status", "3")
            startActivity(intent)
        }

        loanPreClosureLayout.setOnClickListener {
            val intent = Intent(context, LoanPreClosureActivity::class.java)
            startActivity(intent)
        }
    }
}