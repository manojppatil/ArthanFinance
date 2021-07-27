package com.av.arthanfinance.homeTabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R

class WithLoanHomeFragment : Fragment() {
    var customerData: CustomerHomeTabResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_with_loan_home, container, false)
    }
}