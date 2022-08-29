package com.av.arthanfinance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.arthanfinance.core.util.FiniculeUtil
import com.av.arthanfinance.applyLoan.ApplyCoupon
import com.av.arthanfinance.applyLoan.LoanApplicationSubmittedActivity
import com.av.arthanfinance.applyLoan.LoanProcessResponse
import com.av.arthanfinance.applyLoan.model.BusinessTypesResponse
import com.av.arthanfinance.databinding.ActivityInitiateApplyLoan2Binding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadBusinessDetailsActivity
import com.av.arthanfinance.util.ArthanFinConstants
import com.av.arthanfinance.util.ArthanFinConstants.IS_CREATE_FLOW
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_initiate_apply_loan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InitiateApplyLoanActivity2 : BaseActivity() {
    private lateinit var activity2: ActivityInitiateApplyLoan2Binding
    private var loanResponse: LoanProcessResponse? = null
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    private var tenureList = arrayOf("6 Months", "12 Months", "18 Months", "24 Months")
    var MIN = 30000
    var MAX = 100000
    var STEP = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity2 = ActivityInitiateApplyLoan2Binding.inflate(layoutInflater)
        setContentView(activity2.root)
//        if (intent.hasExtra("loanResponse")) {
//            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
//        }

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
        apiClient = ApiClient()

        activity2.loanAmountEditText.isCursorVisible = true
        activity2.loanAmountEditText.isFocusableInTouchMode = true
        activity2.loanAmountEditText.isEnabled = true

        activity2.loanAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(amount: Editable) {
                if (amount.isNotEmpty()) {
                    val totalLoanAmount = amount.toString().toInt()
                    if (totalLoanAmount > 100000) {
                        activity2.loanAmountEditText.setText(MIN.toString())
                        activity2.loanAmountEditText.setSelection(MIN.toString().length)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getEmiRoi()
                //showProgressDialog()
            }

        })

        activity2.loanAmountSeekbar.max = 14

        activity2.loanAmountSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = MIN + (i * STEP)
                if (i == 14) {
                    progress_custom = 100000
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

        val tenureAdapter = ArrayAdapter(this, R.layout.emi_options, tenureList)
        activity2.tenureDropDown.setText(tenureAdapter.getItem(0).toString(), false)
        activity2.tenureDropDown.setAdapter(tenureAdapter)

        activity2.tenureDropDown.setOnClickListener {
            activity2.tenureDropDown.showDropDown()
        }

        activity2.tenureDropDown.setSelection(0)
        activity2.tenureDropDown.setOnItemClickListener { adapterView, view, i, l ->
            getEmiRoi()
        }

        activity2.btnNext.setOnClickListener {
            sendLoanRequest()
        }

        getEmiRoi()

        activity2.imgBack.setOnClickListener {
            finish()
        }

        getAllBusinessTypes()
    }

    private fun sendLoanRequest() {
        if (tenureDropDown.text.toString() == "Select Tenure") {
            Toast.makeText(
                this@InitiateApplyLoanActivity2,
                "Please select the tenure of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (loanPurposeDropDown.text.toString() == "Select Loan Purpose") {
            Toast.makeText(
                this@InitiateApplyLoanActivity2,
                "Please enter the purpose of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val loanAmount = loanAmountEditText.text.toString().replace(",", "")
        if (loanAmount.toInt() < 30000) {
            Toast.makeText(
                this@InitiateApplyLoanActivity2,
                "Loan Amount less than 30000 not allowed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (currentLocation == null) {
            FiniculeUtil.showGPSNotEnabledDialog(this)
            return
        }

        showProgressDialog()
        val tenure = tenureDropDown.text.split(" ").first()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmount", loanAmount)
        jsonObject.addProperty("tenure", tenure)
        jsonObject.addProperty("purpose", loanPurposeDropDown.text.toString())
        jsonObject.addProperty("applied", "Yes")
        currentLocation?.latitude?.let {
            jsonObject.addProperty("lat", it)
        }
        currentLocation?.longitude?.let {
            jsonObject.addProperty("lng", it)
        }

        apiClient.getAuthApiService(this).applyForLoan(jsonObject)
            .enqueue(object : Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>
                ) {
                    hideProgressDialog()
                    loanResponse = response.body()
                    if (loanResponse != null) {
                        val intent = Intent(
                            this@InitiateApplyLoanActivity2,
                            ApplyCoupon::class.java
                        )
                        val loanId = loanResponse!!.loanId
                        intent.putExtra("loanId", loanId)
                        intent.putExtra("loanResponse", loanResponse)
                        startActivity(intent)

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

    private fun navigateToBusinessDetailsActivity(
        loanResponse: LoanProcessResponse,
        navigateToBusinessDetails: Boolean
    ) {
        val intent =
            Intent(this@InitiateApplyLoanActivity2, UploadBusinessDetailsActivity::class.java)
        intent.putExtra(IS_CREATE_FLOW, true)
        intent.putExtra(ArthanFinConstants.MOVE_TO_BUSINESS_DETAILS, navigateToBusinessDetails)
        intent.putExtra("loanResponse", loanResponse)
        startActivity(intent)
    }

    private fun getAllBusinessTypes() {
        ApiClient().getAuthApiService(this).getBusinnessPurposeTypes().enqueue(object :
            Callback<BusinessTypesResponse> {
            override fun onFailure(call: Call<BusinessTypesResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<BusinessTypesResponse>,
                response: Response<BusinessTypesResponse>
            ) {
                val businessTypesResponse = response.body()
                val listOfBusiness = businessTypesResponse?.businessPurpose
                listOfBusiness?.let {
                    initateDropDownForBusinessPurposeField(it)
                }
            }
        })
    }

    private fun getEmiRoi() {
        val jsonObject = JsonObject()
        val tenure = activity2.tenureDropDown.text.split(" ").first()
        if (activity2.loanAmountEditText.text.toString().isEmpty()) {
            return
        }
        val loanAmount =
            activity2.loanAmountEditText.text.toString().replace(",", "").replace(".00", "")
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmt", loanAmount)
        jsonObject.addProperty("tenure", tenure)

        ApiClient().getAuthApiService(applicationContext).getEmiRoi(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                hideProgressDialog()
                val emiroiResponse = response.body() as LoanProcessResponse
                activity2.monthlyEMI.text = "${getString(R.string.Rs)} ${emiroiResponse.emi}"
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    applicationContext,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun initateDropDownForBusinessPurposeField(listOfBusiness: Array<String>?) {
        val bussinessPurposeAdapter = ArrayAdapter(
            this,
            R.layout.emi_options, listOfBusiness!!
        )
        if (intent.getBooleanExtra(IS_CREATE_FLOW, false)) {
            activity2.loanPurposeDropDown.setText(
                bussinessPurposeAdapter.getItem(0).toString(),
                false
            )
        }
        activity2.loanPurposeDropDown.setAdapter(bussinessPurposeAdapter)
        activity2.loanPurposeDropDown.setOnItemClickListener { parent, view, position, id ->
            val tenure = listOfBusiness[position]
            if (tenure == "Select Loan Purpose") {
                Toast.makeText(
                    this,
                    "Please enter the purpose of loan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!FiniculeUtil.isLocationEnabled(this)) {
            FiniculeUtil.requestAccessFineLocationPermission(
                this,
                FiniculeUtil.LOCATION_CODE
            )
        } else {
            setUpLocationListener()
        }
    }

    override val layoutId: Int
        get() = R.layout.activity_initiate_apply_loan2

}



