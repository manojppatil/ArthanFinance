package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import kotlinx.android.synthetic.main.activity_upi_mandate_success.*

class UpiMandateSuccess : AppCompatActivity() {
    private lateinit var btnDone: Button
    var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upi_mandate_success)

        val mPrefs: SharedPreferences? =
            getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)

        txtMobNo.text = mPrefs?.getString("mobNo", null)

        if (intent.getStringExtra("from").equals("agreement")) {
            txt_title.text = "E-Agreement Initiated Successfully"
            txt_subtitle.text = "Thank you for selecting E-Agreement registration"
        } else if (intent.getStringExtra("from").equals("nach")) {
            txt_title.text = "E-NACH Mandate Initiated"
            txt_subtitle.text = "Thank you for selecting E-NACH Mandate registration"
        }

        btnDone = findViewById(R.id.btn_done)

        btnDone.setOnClickListener {
            val intent = Intent(
                this@UpiMandateSuccess,
                HomeDashboardActivity::class.java
            )
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