package com.av.arthanfinance.profile

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.R
import ir.mahozad.android.PieChart

class Credit_score : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_score)

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        pieChart.slices = listOf(
            PieChart.Slice(0.2f, Color.BLUE),
            PieChart.Slice(0.3f, Color.YELLOW),
        )
    }
}