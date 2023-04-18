package com.av.arthanfinance.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.SetNewMpinActivity
import com.av.arthanfinance.databinding.ActivitySettingsBinding
import com.av.arthanfinance.util.ArthanFinConstants

class SettingsActivity : BaseActivity() {
    private lateinit var activitySettingsBinding: ActivitySettingsBinding
    override val layoutId: Int
        get() = R.layout.activity_settings
    private var valMpin: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySettingsBinding =
            ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(activitySettingsBinding.root)

        val mPref: SharedPreferences =
            getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val finger_data = mPref.getBoolean(ArthanFinConstants.isFingerPrintSet, true)
        val mpin_data = mPref.getBoolean(ArthanFinConstants.isMpinSet, true)
        if (finger_data) {
            activitySettingsBinding.switchFinger.isChecked = true
        }



        activitySettingsBinding.lytMpin.setOnClickListener {
            val intent = Intent(this@SettingsActivity, SetNewMpinActivity::class.java)
            startActivity(intent)
            finish()
        }

        activitySettingsBinding.switchFinger.setOnCheckedChangeListener { _, isChecked ->
            if (mpin_data) {
                if (isChecked) {
                    // The switch is enabled/checked
                    val sharedPref: SharedPreferences =
                        getSharedPreferences("customerData", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putBoolean(ArthanFinConstants.isFingerPrintSet, true)
                    editor.apply()
                } else {
                    // The switch is disabled
                    val sharedPref: SharedPreferences =
                        getSharedPreferences("customerData", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putBoolean(ArthanFinConstants.isFingerPrintSet, false)
                    editor.apply()
                }
            } else {
                Toast.makeText(
                    this@SettingsActivity,
                    "Please set your MPIN first",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        activitySettingsBinding.imgBack.setOnClickListener {
            finish()
        }
    }
}
