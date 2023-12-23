package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.databinding.ActivityUpiMandateBinding
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_upi_mandate.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpiMandate : BaseActivity() {
    private lateinit var activityUpiMandate: ActivityUpiMandateBinding
    private lateinit var btnSubmit: Button
    private lateinit var bankName: EditText
    private lateinit var upiId: TextInputEditText
    private lateinit var accountHolder: EditText
    private lateinit var accountType: EditText
    private lateinit var accountNo: TextInputEditText
    private lateinit var con_accountNo: TextInputEditText
    private lateinit var ifscCode: EditText
    private lateinit var btnTC: CheckBox
    private lateinit var apiClient: ApiClient
    private var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_upi_mandate

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUpiMandate =
            ActivityUpiMandateBinding.inflate(layoutInflater)
        setContentView(activityUpiMandate.root)
        apiClient = ApiClient()
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: UserDetailsResponse =
                gson.fromJson(json, UserDetailsResponse::class.java)
            customerData = obj
            mCustomerId = customerData!!.customerId
        }

        if (supportActionBar != null)
            supportActionBar?.hide()
        bankName = findViewById(R.id.edt_bank_name)
        upiId = findViewById(R.id.edt_upi_id)
        accountHolder = findViewById(R.id.edt_account_holder)
        accountType = findViewById(R.id.edt_acc_type)
        accountNo = findViewById(R.id.edt_account_no)
        con_accountNo = findViewById(R.id.edt_confirm_acno)
        ifscCode = findViewById(R.id.edt_ifsccode)
        btnSubmit = findViewById(R.id.btn_submit)
        btnTC = findViewById(R.id.accept_tc)

        bankName.setText(customerData!!.bankName)
        accountHolder.setText(customerData!!.accountName)
        accountType.setText(customerData!!.accountType)
        ifscCode.setText(customerData!!.ifscCode)

        activityUpiMandate.imgBack.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Back from Nach mandate")//added by CleverTap Assistant
            finish()
        }

        btnSubmit.setOnClickListener {
            if (bankName.text.isNotEmpty()) {

                if (bankName.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@UpiMandate,
                        "Please enter Bank Name.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (accountHolder.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@UpiMandate,
                        "Please enter Account Holder Name.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (accountType.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@UpiMandate,
                        "Please enter Account Type.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (accountNo.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@UpiMandate,
                        "Please enter Account Number.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (ifscCode.text.toString().isEmpty()) {
                    Toast.makeText(
                        this@UpiMandate,
                        "Please enter IFSC Code.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (accountNo.text.toString() != con_accountNo.text.toString()) {
                    Toast.makeText(
                        this,
                        "Confirm Account No. not matched with Account No.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (!btnTC.isChecked) {
                    Toast.makeText(
                        this,
                        "Please accept terms and Conditions.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                registerNachmandate()
            } else {
                Toast.makeText(
                    this@UpiMandate,
                    "Mandatory fields missing please enter all data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun registerNachmandate() {
        val bank_name = bankName.text.toString()
        val account_holder = accountHolder.text.toString()
        val account_type = accountType.text.toString()
        val account_no = accountNo.text.toString()
        val ifsc_code = ifscCode.text.toString()
        val upi_Id = upiId.text.toString()

        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("bankName", bank_name)
        jsonObject.addProperty("accountNo", account_no)
        jsonObject.addProperty("accountName", account_holder)
        jsonObject.addProperty("accountType", account_type)
        jsonObject.addProperty("ifscCode", ifsc_code)
        jsonObject.addProperty("vpaId", upi_Id)
        jsonObject.addProperty("source", "arthik")
        clevertapDefaultInstance?.pushEvent("Nach mandate started")//added by CleverTap Assistant
        ApiClient().getAuthApiService(this).registerNach(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Nach mandate failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@UpiMandate,
                    "Nach Mandate failure. Try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
                        clevertapDefaultInstance?.pushEvent("Nach mandate success")//added by CleverTap Assistant
                        updateStage(ArthanFinConstants.enach)
                    } else {
                        Log.e("Error", custData.message + "")
                    }
                }
            }
        })
    }

    private fun updateStage(stage: String) {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("stage", stage)
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UpiMandate, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    clevertapDefaultInstance?.pushEvent("Nach stage updated")//added by CleverTap Assistant
                    val intent = Intent(this@UpiMandate, UpiMandateSuccess::class.java)
                    intent.putExtra("from", "nach")
                    startActivity(intent)
                    finish()
                }

            }
        })
    }
}