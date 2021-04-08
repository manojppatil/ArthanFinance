package com.example.arthanfinance

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.arthanfinance.applyLoan.AuthenticationResponse
import com.example.arthanfinance.homeTabs.HomeDashboardActivity
import com.example.arthanfinance.networkService.ApiClient
import com.example.arthanfinance.networkService.DatabaseHandler
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MPINLoginActivity : AppCompatActivity() {
    private lateinit var tvForgotMpin: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvDontHaveAccount: TextView
    private lateinit var mpintext: EditText
    private lateinit var mobileText: EditText
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_mpin_login)

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
            val customerData: CustomerHomeTabResponse = gson.fromJson(json, CustomerHomeTabResponse::class.java)
            mobileText.setText(customerData.mobNo)
        }

        tvForgotMpin.setOnClickListener{
            val intent = Intent(this@MPINLoginActivity, SetNewMpinActivity::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener{
            verifyCustomerPin()
        }
        tvDontHaveAccount.setOnClickListener{
            val intent = Intent(this@MPINLoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verifyCustomerPin() {
        //val customerId = intent.extras?.get("customerId") as String
        val jsonObject = JsonObject()
//        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//        val custId = databaseHandler.getCustomerByMobileNo(mobileText.text.toString())
//        val customerId = custId?.custId
        jsonObject.addProperty("mobNo", mobileText.text.toString())
        jsonObject.addProperty("pin", mpintext.text.toString())
        ApiClient().getApiService(this).verifyCustomerPin(jsonObject).enqueue(object :
            Callback<CustomerHomeTabResponse> {
            override fun onFailure(call: Call<CustomerHomeTabResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MPINLoginActivity,"Login Failed. Please enter Valid MPIN",
                    Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<CustomerHomeTabResponse>,
                response: Response<CustomerHomeTabResponse>
            ) {
                val custData = response.body()
                if (custData != null) {
                        if (custData.customerBiometric == "Y") {
                            val intent = Intent(this@MPINLoginActivity, HomeDashboardActivity::class.java)
                            intent.putExtra("customerData",custData)
                            val sharedPref: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
                            val prefsEditor = sharedPref?.edit()
                            val gson = Gson()
                            val json: String = gson.toJson(custData)
                            prefsEditor?.putString("customerData", json)
                            prefsEditor?.apply()
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@MPINLoginActivity, SetupFingerBiometricActivity::class.java)
                            startActivity(intent)
                        }

                }
            }
        })
    }
}
