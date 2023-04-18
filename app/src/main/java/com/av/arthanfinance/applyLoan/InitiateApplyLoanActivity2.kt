package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.arthanfinance.core.base.BaseActivity
import com.arthanfinance.core.util.FiniculeUtil
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityInitiateApplyLoan2Binding
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.av.arthanfinance.util.ArthanFinConstants.IS_CREATE_FLOW
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.util.*

class InitiateApplyLoanActivity2 : BaseActivity() {
    private lateinit var activity2: ActivityInitiateApplyLoan2Binding
    private var loanResponse: LoanProcessResponse? = null
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    var MIN = 1000
    var MAX = 30000
    var STEP = 1000
    private var mLoanAmount: String? = null
    private var dayDifference: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity2 = ActivityInitiateApplyLoan2Binding.inflate(layoutInflater)
        setContentView(activity2.root)

        val isCreateFlow = intent.getBooleanExtra(IS_CREATE_FLOW, false)

        if (supportActionBar != null)
            supportActionBar?.hide()

        if (intent.hasExtra("customerData")) {
            customerData = intent.extras?.get("customerData") as CustomerHomeTabResponse
        } else {
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if (json != null) {
                val obj: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                customerData = obj
            }
        }

        mLoanAmount = intent.getStringExtra("amount")
        apiClient = ApiClient()

        activity2.loanAmountEditText.setText(mLoanAmount)
        activity2.loanAmountEditText.isCursorVisible = true
        activity2.loanAmountEditText.isFocusableInTouchMode = true
        activity2.loanAmountEditText.isEnabled = true

//        activity2.loanAmountEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(amount: Editable) {
//                if (amount.isNotEmpty()) {
//                    val totalLoanAmount = amount.toString().toDouble()
//                    if (totalLoanAmount > 30000) {
//                        activity2.loanAmountEditText.setText(MIN.toString())
//                        activity2.loanAmountEditText.setSelection(MIN.toString().length)
//                    }
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
////                getEmiRoi("2")
//            }
//
//        })

        activity2.loanAmountSeekbar.max = 30

        activity2.loanAmountSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = MIN + (i * STEP)
                if (i == 30) {
                    progress_custom = 30000

                }
                activity2.loanAmountEditText.setText(progress_custom.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

//        val tenureAdapter = ArrayAdapter(this, R.layout.emi_options, tenureList)
//        activity2.tenureDropDown.setText(tenureAdapter.getItem(0).toString(), false)
//        activity2.tenureDropDown.setAdapter(tenureAdapter)
//
//        activity2.tenureDropDown.setOnClickListener {
//            activity2.tenureDropDown.showDropDown()
//        }

//        val cal = Calendar.getInstance()

//        val dateSetListener =
//            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
//                cal.set(Calendar.YEAR, year)
//                cal.set(Calendar.MONTH, monthOfYear)
//                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//
//                val myFormat = "dd-MM-yyyy" // mention the format you need
//                val sdf = SimpleDateFormat(myFormat, Locale.US)
//                val currentDate = sdf.format(Date())
//                val paymentDate = sdf.format(cal.time)
//                val date1: Date = sdf.parse(currentDate) as Date
//                val date2: Date = sdf.parse(paymentDate) as Date
//                val difference: Long = abs(date2.time - date1.time)
//                val differenceDates = difference / (24 * 60 * 60 * 1000)
//                dayDifference = differenceDates.toString()
//                Log.e("TDAYS", dayDifference!!)
//                if (dayDifference!!.toInt() >= 45) {
//                    Toast.makeText(
//                        this@InitiateApplyLoanActivity2,
//                        "Your Repayment days should not exceeds more than 45 days",
//                        Toast.LENGTH_LONG
//                    ).show()
//                    activity2.monthlyEMI.text = currentDate
//                } else {
//                    activity2.monthlyEMI.text = sdf.format(cal.time)
//                }
//            }

//        activity2.monthlyEMI.setOnClickListener {
//            DatePickerDialog(
//                this, dateSetListener,
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }


        activity2.btnNext.setOnClickListener {
            if (activity2.loanAmountEditText.text.isNotEmpty()) {
                sendLoanRequest()
            } else {
                Toast.makeText(
                    this@InitiateApplyLoanActivity2,
                    "Please enter valid amount.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }

//        getEmiRoi("2")

        activity2.imgBack.setOnClickListener {
            finish()
        }

//        getAllBusinessTypes()
    }

    private fun sendLoanRequest() {
        val loanAmount = activity2.loanAmountEditText.text.toString().replace(",", "")

        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmount", loanAmount)

        apiClient.getAuthApiService(this).applyForLoan(jsonObject)
            .enqueue(object : Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>,
                ) {
                    hideProgressDialog()
                    loanResponse = response.body()
                    Log.e("TAGR", loanResponse.toString())
                    if (loanResponse != null) {
                        if (loanResponse!!.errCode == "400") {
                            Toast.makeText(this@InitiateApplyLoanActivity2,
                                loanResponse!!.errDesc,
                                Toast.LENGTH_LONG).show()
                        } else {
                            updateStage(ArthanFinConstants.apply_loan)
                        }

                    }
                }

                override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                    t.printStackTrace()
                    hideProgressDialog()
                    Toast.makeText(
                        this@InitiateApplyLoanActivity2,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateStage(stage: String) {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("stage", stage)
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@InitiateApplyLoanActivity2, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    val intent = Intent(
                        this@InitiateApplyLoanActivity2,
                        LoanSummaryActivity::class.java
                    )
                    val loanId = loanResponse!!.applicationId
                    Log.e("TAGid", loanResponse!!.applicationId.toString())
                    intent.putExtra("loanId", loanId)
                    intent.putExtra("loanResponse", loanResponse)
                    intent.putExtra("loanAmount", loanResponse!!.loanAmount)
                    intent.putExtra("repaymentDate", loanResponse!!.repaymentDate)
                    intent.putExtra("tenure", loanResponse!!.tenure)
                    intent.putExtra("totalInterest", loanResponse!!.totalInterest)
                    intent.putExtra("payableAmt", loanResponse!!.payableAmt)
                    intent.putExtra("from", "applyLoan")
                    startActivity(intent)
                    finish()
                }

            }
        })
    }

//    private fun getAllBusinessTypes() {
//        ApiClient().getAuthApiService(this).getBusinnessPurposeTypes().enqueue(object :
//            Callback<BusinessTypesResponse> {
//            override fun onFailure(call: Call<BusinessTypesResponse>, t: Throwable) {
//                t.printStackTrace()
//            }
//
//            override fun onResponse(
//                call: Call<BusinessTypesResponse>,
//                response: Response<BusinessTypesResponse>
//            ) {
//                val businessTypesResponse = response.body()
//                val listOfBusiness = businessTypesResponse?.businessPurpose
//                listOfBusiness?.let {
//                    initateDropDownForBusinessPurposeField(it)
//                }
//            }
//        })
//    }
//
//    private fun getEmiRoi(tenure: String) {
//        val jsonObject = JsonObject()
////        val tenure = activity2.tenureDropDown.text.split(" ").first()
//        if (activity2.loanAmountEditText.text.toString().isEmpty()) {
//            return
//        }
//        val loanAmount =
//            activity2.loanAmountEditText.text.toString().replace(",", "").replace(".00", "")
//        jsonObject.addProperty("customerId", customerData.customerId)
//        jsonObject.addProperty("loanAmt", loanAmount)
//        jsonObject.addProperty("tenure", tenure)
//
//        ApiClient().getAuthApiService(applicationContext).getEmiRoi(jsonObject).enqueue(object :
//            Callback<LoanProcessResponse> {
//            @SuppressLint("SetTextI18n")
//            override fun onResponse(
//                call: Call<LoanProcessResponse>,
//                response: Response<LoanProcessResponse>
//            ) {
//                hideProgressDialog()
//                val emiroiResponse = response.body() as LoanProcessResponse
//                activity2.monthlyEMI.text = "${getString(R.string.Rs)} ${emiroiResponse.emi}"
//            }
//
//            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
//                t.printStackTrace()
//                hideProgressDialog()
//                Toast.makeText(
//                    applicationContext,
//                    "Service Failure, Once Network connection is stable, will try to resend again",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//    }

//    private fun initateDropDownForBusinessPurposeField(listOfBusiness: Array<String>?) {
//        val bussinessPurposeAdapter = ArrayAdapter(
//            this,
//            R.layout.emi_options, listOfBusiness!!
//        )
//        if (intent.getBooleanExtra(IS_CREATE_FLOW, false)) {
//            activity2.loanPurposeDropDown.setText(
//                bussinessPurposeAdapter.getItem(0).toString(),
//                false
//            )
//        }
//        activity2.loanPurposeDropDown.setAdapter(bussinessPurposeAdapter)
//        activity2.loanPurposeDropDown.setOnItemClickListener { parent, view, position, id ->
//            val tenure = listOfBusiness[position]
//            if (tenure == "Select Loan Purpose") {
//                Toast.makeText(
//                    this,
//                    "Please enter the purpose of loan",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//    }

    override val layoutId: Int
        get() = R.layout.activity_initiate_apply_loan2

}



