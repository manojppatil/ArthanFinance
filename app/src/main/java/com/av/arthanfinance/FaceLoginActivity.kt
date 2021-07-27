package com.av.arthanfinance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class FaceLoginActivity : AppCompatActivity() {
    private lateinit var btnMpin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login_face_id)

        if (supportActionBar != null)
            supportActionBar?.hide()

        btnMpin = findViewById(R.id.btn_login_mpin)
        btnMpin.setOnClickListener{
            val intent = Intent(this@FaceLoginActivity, MPINLoginActivity::class.java)
            startActivity(intent)
        }
    }
}