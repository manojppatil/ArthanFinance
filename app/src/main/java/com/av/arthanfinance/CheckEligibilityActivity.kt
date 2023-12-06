package com.av.arthanfinance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.user_kyc.*
import kotlinx.android.synthetic.main.activity_check_eligibility.*

class CheckEligibilityActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_eligibility)

        checkEligibility.setOnClickListener {
            val intent =
                Intent(this@CheckEligibilityActivity, UploadPanActivity::class.java)
            startActivity(intent)
        }
    }
}