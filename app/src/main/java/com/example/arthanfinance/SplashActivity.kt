package com.example.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import com.example.arthanfinance.introductionPager.IntroductionPagerActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash)

        if (supportActionBar != null)
            supportActionBar?.hide()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler(Looper.getMainLooper()).postDelayed({

            val sharedPref: SharedPreferences = getSharedPreferences("isFirstTime", Context.MODE_PRIVATE)
            if (sharedPref.getBoolean("isFirstTime", true)) {
                val editor = sharedPref.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()
                val intent = Intent(this@SplashActivity, IntroductionPagerActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@SplashActivity, FingerPrintLoginActivity::class.java)
                startActivity(intent)
            }
            // Your Code
        }, 3000) // 3000 is the delayed time in milliseconds.
    }
}