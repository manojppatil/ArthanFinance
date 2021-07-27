package com.av.arthanfinance

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MPINLoginActivity : BaseActivity() {
    private lateinit var tvForgotMpin: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvDontHaveAccount: TextView
    private lateinit var mpintext: EditText
    private lateinit var mobileText: EditText
    private lateinit var apiClient: ApiClient
    override val layoutId: Int
        get() = R.layout.layout_mpin_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()

        apiClient = ApiClient()
        tvForgotMpin = findViewById(R.id.tv_forgot_mpin)
        btnLogin = findViewById(R.id.btn_login)
        mpintext = findViewById(R.id.edt_mpin)
        mobileText = findViewById(R.id.edt_mobile)

        tvDontHaveAccount = findViewById(R.id.tv_dnt_hav_account)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if(json != null) {
            val customerData: CustomerHomeTabResponse = gson.fromJson(
                json,
                CustomerHomeTabResponse::class.java
            )
            mobileText.setText(customerData.mobNo)
        }

        tvForgotMpin.setOnClickListener{
            val intent = Intent(this@MPINLoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener{
            verifyCustomerPin()
        }
        tvDontHaveAccount.setOnClickListener{
            val intent = Intent(this@MPINLoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        mpintext.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCustomerPin()
            }
            false
        }
//        btnLogin.performClick()
    }

    private fun verifyCustomerPin() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobNo", mobileText.text.toString())
        jsonObject.addProperty("pin", mpintext.text.toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).verifyCustomerPin(jsonObject).enqueue(object :
            Callback<CustomerHomeTabResponse> {
            override fun onFailure(call: Call<CustomerHomeTabResponse>, t: Throwable) {

                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@MPINLoginActivity, "Login Failed. Please enter Valid MPIN",
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
                    val intent = Intent(
                        this@MPINLoginActivity,
                        HomeDashboardActivity::class.java
                    )
                    intent.putExtra("customerData", custData)
                    val sharedPref: SharedPreferences? = getSharedPreferences(
                        "customerData",
                        Context.MODE_PRIVATE
                    )
                    val prefsEditor = sharedPref?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(custData)
                    prefsEditor?.putString("customerData", json)
                    prefsEditor?.apply()
                    startActivity(intent)
                    finish()
                }else{
                    custData?.let {
                        Toast.makeText(
                            this@MPINLoginActivity, it.errDesc,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}
