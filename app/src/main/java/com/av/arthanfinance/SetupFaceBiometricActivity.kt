package com.av.arthanfinance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.av.arthanfinance.homeTabs.HomeDashboardActivity

class SetupFaceBiometricActivity : AppCompatActivity() {
    private lateinit var btnNotNow: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageButton
    private var isLoansAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_set_up_bio_face_id)
        btnNotNow = findViewById(R.id.btn_not_now)

        if (supportActionBar != null)
            supportActionBar?.hide()

        btnSubmit = findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener{
            val intent = Intent(this@SetupFaceBiometricActivity, HomeDashboardActivity::class.java)
            startActivity(intent)
        }

        btnBack = findViewById(R.id.img_back_biometricface)
        btnBack.setOnClickListener {
            this.finish()
        }

    }
}