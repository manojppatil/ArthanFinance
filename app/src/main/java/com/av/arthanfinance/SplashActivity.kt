package com.av.arthanfinance

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
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.introductionPager.IntroductionPagerActivity
import com.google.gson.Gson

class SplashActivity : AppCompatActivity() {
    private var customerData: CustomerHomeTabResponse? = null
    private var customerID:String = ""
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
            val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if(json != null) {
                val obj: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                customerData = obj
                customerID = customerData?.customerId.toString()
            }

            when {
                sharedPref.getBoolean("isFirstTime", true) -> {
                    /*val editor = sharedPref.edit()
                        editor.putBoolean("isFirstTime", false)
                        editor.apply()*/

                    val intent = Intent(this@SplashActivity, PermissionsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                customerID == "" -> {
        //                val intent = Intent(this@SplashActivity, FingerPrintLoginActivity::class.java)//
                    val intent = Intent(this@SplashActivity, MPINLoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    val intent = Intent(this@SplashActivity, MPINLoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            // Your Code
        }, 2000) // 3000 is the delayed time in milliseconds.
    }
}
//MAPS_API_KEY=AIzaSyCe9epHZuDTREfqN_WL9U1wOPq0w6m4BE0