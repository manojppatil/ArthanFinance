package com.av.arthanfinance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.models.CustomerHomeTabResponse

class RewardsActivity : AppCompatActivity() {
    var customerData: CustomerHomeTabResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)
    }
}