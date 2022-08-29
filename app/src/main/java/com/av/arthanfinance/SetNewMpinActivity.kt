package com.av.arthanfinance


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.GenericResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadBankDetailsActivity
import com.av.arthanfinance.user_kyc.UploadPanActivity
import com.chaos.view.PinView
import com.google.gson.Gson
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null)
            supportActionBar?.hide()
        apiClient = ApiClient()
        btnBack = findViewById(R.id.img_back_mpin)

        mobileNum = intent.extras?.get("mob") as String
        fbtoken = intent.extras?.get("fbtoken") as String

        btnBack.setOnClickListener {
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
                resetPin()
            } else {
                setMPIN()
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
        apiClient.getAuthApiService(this).resetMpin(jsonObject).enqueue(object :
            Callback<GenericResponse> {
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@SetNewMpinActivity, "Please enter the valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<GenericResponse>,
                response: Response<GenericResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
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

        if (pin1 == pin2) {
            return true
        } else {
            pinView.setText("")
            pinView2.setText("")
            Toast.makeText(
                this@SetNewMpinActivity, "Please enter the valid MPIN",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return false
    }


    private fun setMPIN() {
        if (!validateMPIN()) {
            return
        }
        val mpin = pin1
        showProgressDialog()
        val customerId = intent.extras?.get("customerId") as String
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
                Toast.makeText(
                    this@SetNewMpinActivity, "Please enter the valid MPIN",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<CustomerHomeTabResponse>,
                response: Response<CustomerHomeTabResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()

                if (custData != null && custData.errCode == 200.toString()) {
                    val sharedPref: SharedPreferences? =
                        getSharedPreferences("customerData", Context.MODE_PRIVATE)
                    val prefsEditor = sharedPref?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(custData)
                    prefsEditor?.putString("customerData", json)
                    prefsEditor?.apply()

                    val intent = Intent(this@SetNewMpinActivity, CheckEligibilityActivity::class.java)
                    intent.putExtra("customerData", custData)
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

