package com.av.arthanfinance

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.networkService.Customer
import com.av.arthanfinance.networkService.DatabaseHandler
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern


class RegistrationActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var btnSubmit: Button
    private lateinit var nameText: EditText
    private lateinit var mobileNoText: EditText
    private lateinit var dobText: EditText
    private lateinit var emailText: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnTC: CheckBox
    private lateinit var apiClient: ApiClient
    override val layoutId: Int
        get() = R.layout.layout_register

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = ApiClient()

        if (supportActionBar != null)
            supportActionBar?.hide()

        nameText = findViewById(R.id.edt_name)
        mobileNoText = findViewById(R.id.edt_mob_num)
        dobText = findViewById(R.id.edt_dob)
        emailText = findViewById(R.id.edt_email)
        btnTC = findViewById(R.id.accept_tc)


        dobText.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(this@RegistrationActivity, this@RegistrationActivity, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        btnSubmit = findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener{
            if(nameText.text.isNotEmpty() && mobileNoText.text.isNotEmpty() && dobText.text.isNotEmpty()) {

                if(!isValidPhoneNumber(mobileNoText.text)) {
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Please enter VALID Mobile Number.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if(!btnTC.isChecked) {
                    Toast.makeText(this@RegistrationActivity,"Please accept terms and Conditions.",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                registerCustomerData()
            } else {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Mandatory fields missing please enter all data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnBack = findViewById(R.id.img_back)
        btnBack.setOnClickListener {
            this.finish()
        }
    }

    private fun isValidPhoneNumber(phoneNumber: CharSequence): Boolean {
        return if (!TextUtils.isEmpty(phoneNumber)) {
            Patterns.PHONE.matcher(phoneNumber).matches()
        } else false
    }
    private fun isValidMail(email: String): Boolean {
        val EMAIL_STRING = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(EMAIL_STRING).matcher(email).matches()
    }

    private fun registerCustomerData() {
        val name = nameText.text.toString()
        val email = emailText.text.trim().toString()
        val mobile = mobileNoText.text.toString()
        val dob = dobText.text.toString()
        showProgressDialog()
        //Need to create GSON JsonObject rather than creating a new class object and sending the data in the API Calls
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", name)
        jsonObject.addProperty("mobNo", mobile)
        jsonObject.addProperty("dob", dob)
        jsonObject.addProperty("emailId", email)
        jsonObject.addProperty("consent", "Y")
        jsonObject.addProperty("userLanguage", "English")
        apiClient.getAuthApiService(this).registerCustomer(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(this@RegistrationActivity,"Registration Failure Try after some time",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    if (custData.apiCode == "200") {
                        custData.customerId?.let {
                            saveCustomerData(name, email, mobile, dob, it)
                            Toast.makeText(this@RegistrationActivity,"Registration success",Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegistrationActivity, OTPActivity::class.java)
                            intent.putExtra("customerId",it)
                            intent.putExtra("mobNo",mobile)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@RegistrationActivity,custData.message ,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    //method for saving records in database
    private fun saveCustomerData(name: String, email: String, mobile: String, dob: String, custId: String) {
        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
        if (name.trim() != "" && email.trim() != "" && mobile.trim() != "") {
            val status = databaseHandler.saveCustomer(Customer(name, email, mobile,dob,custId, null))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    "Record saved to DB.",
                    Toast.LENGTH_LONG
                ).show()

            } else if (status == -2.toLong()) {
                Toast.makeText(
                    applicationContext,
                    "Record already exists.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "mobile or name or email cannot be blank",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        dobText.setText("${dayOfMonth}/${month + 1}/${year}")
    }
}