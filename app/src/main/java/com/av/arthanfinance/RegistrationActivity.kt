package com.av.arthanfinance

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.annotation.Nullable
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.networkService.Customer
import com.av.arthanfinance.networkService.DatabaseHandler
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import java.util.*
import java.util.Calendar.YEAR
import java.util.Calendar.getInstance
import java.util.regex.Pattern


class RegistrationActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private lateinit var btnSubmit: Button
    private lateinit var nameText: EditText
    private lateinit var mobileNoText: EditText

    //    private lateinit var dobText: EditText
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
//        dobText = findViewById(R.id.edt_dob)
        emailText = findViewById(R.id.edt_email)
        btnTC = findViewById(R.id.accept_tc)


//        dobText.setOnClickListener {
//            val calendar: Calendar = getInstance()
//            val datePickerDialog = DatePickerDialog(this@RegistrationActivity, this@RegistrationActivity, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
//            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
//            datePickerDialog.show()
//        }

        val mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect()
        }

        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, 1008, null, 0, 0, 0, null)
        } catch (e: SendIntentException) {
            Log.e("", "Could not start hint picker Intent", e)
        }

        btnSubmit = findViewById(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            if (mobileNoText.text.isNotEmpty()) {

                if (!isValidPhoneNumber(mobileNoText.text)) {
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Please enter VALID Mobile Number.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

//                if(!isValidMail(emailText.text)) {
//                    Toast.makeText(
//                        this@RegistrationActivity,
//                        "Please enter VALID Email Id.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }

                if (!btnTC.isChecked) {
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Please accept terms and Conditions.",
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun isValidMail(email: CharSequence): Boolean {
        val EMAIL_STRING = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(EMAIL_STRING).matcher(email).matches()
    }

    private fun registerCustomerData() {
        val intent = Intent(this@RegistrationActivity, OTPActivity::class.java)
        intent.putExtra("name", nameText.text.toString())
        intent.putExtra("mobNo", mobileNoText.text.toString())
        intent.putExtra("email", emailText.text.toString())
        startActivity(intent)
        /*custData.customerId?.let {
            saveCustomerData(name, email, mobile, dob, it)
        }*/

    }

    //method for saving records in database
    private fun saveCustomerData(
        name: String,
        email: String,
        mobile: String,
        dob: String,
        custId: String
    ) {
        val databaseHandler = DatabaseHandler(this)
        if (mobile.trim() != "") {
            val status =
                databaseHandler.saveCustomer(Customer(name, email, mobile, dob, custId, null))
            if (status > -1) {
                Toast.makeText(
                    applicationContext,
                    "Record saved to DB.",
                    Toast.LENGTH_LONG
                ).show()

            } else if (status == (-2).toLong()) {
                Toast.makeText(
                    applicationContext,
                    "Record already exists.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                applicationContext,
                "mobile no cannot be blank",
                Toast.LENGTH_LONG
            ).show()
        }
    }

//    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
//        val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
//        val currentDate = sdf.format(Date())
//        val selectedDate = "${dayOfMonth}/${month + 1}/${year}"
//
//        val date1: Date? = sdf.parse(currentDate)
//        val date2: Date? = sdf.parse(selectedDate)
//        val yearDifference = getDiffYears(date1!!, date2!!)
//
//        when {
//            yearDifference < 21 -> {
//                Toast.makeText(this, "You have to be of 21 years to apply for loan", Toast.LENGTH_SHORT).show()
//            }
//            yearDifference > 60 -> {
//                Toast.makeText(this, "You have to be less than 60 years to apply loan", Toast.LENGTH_SHORT).show()
//            }
//            else -> {
////                dobText.setText("${dayOfMonth}/${month + 1}/${year}")
//            }
//        }
//
//    }

    fun getDiffYears(first: Date, last: Date): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        val yearsInBetween: Int = (a.get(YEAR) - b.get(YEAR))
        return yearsInBetween
    }

    private fun getCalendar(date: Date): Calendar {
        val cal = getInstance(Locale.getDefault())
        cal.time = date
        return cal
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1008) {
            if (resultCode == RESULT_OK) {
                val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                credential?.apply {
                    Log.e("cred.getId", credential.id)
                    val userMob = credential.id
                    val mobNo = userMob.replace("+91", "")
                    mobileNoText.setText(mobNo)
                }
            }
        }
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}