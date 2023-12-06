package com.av.arthanfinance


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.model.GenericResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.chaos.view.PinView
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_set_new_mpin.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SetNewMpinActivity : BaseActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var apiClient: ApiClient
    private var pin1 = ""
    private var pin2 = ""
    override val layoutId: Int
        get() = R.layout.layout_set_new_mpin
    private var mobileNum = ""
    private var fbtoken = ""
    private var mCustomerId: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)

        if (supportActionBar != null)
            supportActionBar?.hide()
        apiClient = ApiClient()
        btnBack = findViewById(R.id.img_back)

        when {
            intent.hasExtra("mob") -> {
                mobileNum = intent.extras?.get("mob") as String
            }
        }

//        fbtoken = intent.extras?.get("fbtoken") as String

        btnBack.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Back from MPIN")//added by CleverTap Assistant
            this.finish()
        }
        val mpin1 = findViewById<PinView>(R.id.pinView)
        val mpin2 = findViewById<PinView>(R.id.pinView2)
        mpin1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                pin1 = s.toString()
                if (count == 4) {
                    mpin2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mpin2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                pin2 = s.toString()
                if (count == 4) {
                    //hideKeyboard(this@SetNewMpinActivity, mpin2)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mpin2.isPasswordHidden = true
        findViewById<Button>(R.id.save_m_pin).setOnClickListener {
            if (intent.hasExtra("MOBILE")) {
                if (pin1.isNotEmpty()) {
                    resetPin()
                } else {
                    Toast.makeText(
                        this,
                        "Please enter valid MPIN",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                if (pin1.isNotEmpty()) {
                    setMPIN()
                } else {
                    Toast.makeText(
                        this,
                        "Please enter valid MPIN",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    private fun resetPin() {
        if (!validateMPIN()) {
            return
        }
        val mpin = pin1
        showProgressDialog()
        val jsonObject = JsonObject()
        if (intent.hasExtra("MOBILE")) {
            jsonObject.addProperty("mobileNo", intent.getStringExtra("MOBILE"))
        }
        jsonObject.addProperty("mpin", mpin)
        Log.e("REQ", jsonObject.toString())
        apiClient.getAuthApiService(this).resetMpin(jsonObject).enqueue(object :
            Callback<GenericResponse> {
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Reset Mpin failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@SetNewMpinActivity, "Please enter the valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
                        clevertapDefaultInstance?.pushEvent("Reset Mpin success")//added by CleverTap Assistant
                        val sharedPref: SharedPreferences =
                            getSharedPreferences("customerData", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean(ArthanFinConstants.isMpinSet, true)
                        editor.apply()
                        val intent = Intent(this@SetNewMpinActivity, MPINLoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@SetNewMpinActivity, "Please enter the valid MPIN",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun validateMPIN(): Boolean {
        return if (pin1 == pin2) {
            true
        } else {
            pinView.setText("")
            pinView2.setText("")
            Toast.makeText(
                this@SetNewMpinActivity, "Please enter the valid MPIN",
                Toast.LENGTH_SHORT
            ).show()
            false
        }
        return false
    }


    private fun setMPIN() {
        if (!validateMPIN()) {
            return
        }
        val mpin = pin1
        showProgressDialog()
        val customerId = mCustomerId
        val jsonObject = JsonObject()
        if (intent.hasExtra("MOBILE")) {
            jsonObject.addProperty("customerId", customerId)
        } else {
            jsonObject.addProperty("customerId", customerId)
        }
        jsonObject.addProperty("pin", mpin)
        jsonObject.addProperty("fbToken", fbtoken)
        ApiClient().getAuthApiService(this).setCustomerMPIN(jsonObject).enqueue(object :
            Callback<CustomerHomeTabResponse> {
            override fun onFailure(call: Call<CustomerHomeTabResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Set Mpin failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@SetNewMpinActivity, "Please enter the valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<CustomerHomeTabResponse>,
                response: Response<CustomerHomeTabResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()
                clevertapDefaultInstance?.pushEvent("Set Mpin success")//added by CleverTap Assistant
                if (custData != null && custData.errCode == 200.toString()) {
                    val sharedPref: SharedPreferences =
                        getSharedPreferences("customerData", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putBoolean(ArthanFinConstants.isMpinSet, true)
                    editor.apply()
                    val intent =
                        Intent(this@SetNewMpinActivity, HomeDashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    custData?.let {
                        Toast.makeText(
                            this@SetNewMpinActivity, it.errDesc,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}

