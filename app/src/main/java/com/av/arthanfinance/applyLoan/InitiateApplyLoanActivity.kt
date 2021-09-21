package com.av.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.widget.*
import androidx.annotation.RequiresApi
import com.arthanfinance.core.base.BaseActivity
import com.arthanfinance.core.util.FiniculeUtil
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.BusinessTypesResponse
import com.av.arthanfinance.manager.DataManager
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.av.arthanfinance.util.ArthanFinConstants.IS_CREATE_FLOW
import com.av.arthanfinance.util.ArthanFinConstants.MOVE_TO_BUSINESS_DETAILS
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_initiate_apply_loan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class InitiateApplyLoanActivity : BaseActivity() {

    private lateinit var tenureDropDown: AutoCompleteTextView
    private lateinit var loanPurposeDropDown: AutoCompleteTextView

    private var loanProcessResponse:LoanProcessResponse? = null
    private lateinit var loanAmountSeekBar: SeekBar
    private lateinit var loanAmountEditText: EditText
    private lateinit var inLakhs: TextView
    private lateinit var monthlyEMI: TextView
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    private var tenureList = arrayOf("6 Months","12 Months", "18 Months","24 Months")

    var MIN = 30000
    var MAX = 100000
    var STEP = 5000
    override val layoutId: Int
        get() = R.layout.activity_initiate_apply_loan

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null)
            supportActionBar?.hide()
        if (intent.hasExtra("customerData")) {
            customerData = intent.extras?.get("customerData") as CustomerHomeTabResponse
        }
        apiClient = ApiClient()

        loanAmountSeekBar = findViewById(R.id.loanAmountSeekbar)
        loanAmountEditText = findViewById(R.id.loanAmountEditText)
        loanPurposeDropDown = findViewById(R.id.loanPurposeDropDown)
        monthlyEMI = findViewById(R.id.monthlyEMI)

        loanAmountEditText.isCursorVisible = true
        loanAmountEditText.isFocusableInTouchMode = true
        loanAmountEditText.isEnabled = true

        inLakhs = findViewById(R.id.inLakhs)

        loanAmountEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(amount: Editable) {
                if (amount.isNotEmpty()) {
                    val totalLoanAmount = amount.toString().toInt()
                    if (totalLoanAmount > 100000) {
                        loanAmountEditText.setText(MIN.toString())
                        loanAmountEditText.setSelection(MIN.toString().length)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getEmiRoi()
            }

        } )

        val isCreateFlow = intent.getBooleanExtra(IS_CREATE_FLOW,false)

        val percentCompleted = 23 //to be achieved from BE
        progress_loan.max = 100
        ObjectAnimator.ofInt(progress_loan, "progress", percentCompleted).setDuration(1000).start()
        tv_progresspercent.text = "${percentCompleted}%"

        val paint: TextPaint = tvLoanApplication.getPaint()
        val width = paint.measureText("Loan Application")
        val shader = LinearGradient(0f, 0f, width, tvLoanApplication.textSize, resources.getColor(
            R.color.dark_orange2, theme), resources.getColor(
            R.color.white, theme), Shader.TileMode.CLAMP)
        tvLoanApplication.paint.shader = shader
        tvLoanApplication.setTextColor(resources.getColor(R.color.dark_orange2, theme))

        val amountTextPaint: TextPaint = loan_amount_text.getPaint()
        val amountTextWidth = amountTextPaint.measureText("Loan Application")

        val amountTextShader = LinearGradient(0f, 0f, amountTextWidth, loan_amount_text.textSize, resources.getColor(
            R.color.dark_orange2, theme), resources.getColor(
            R.color.indigoBlue, theme), Shader.TileMode.CLAMP)
        loan_amount_text.paint.shader = amountTextShader
        emiMonthly.paint.shader = amountTextShader

        tenureDropDown = findViewById(R.id.tenureDropDown)
        val bankNamesAdapter = ArrayAdapter(this,
            R.layout.emi_options, tenureList)
        tenureDropDown.setText(bankNamesAdapter.getItem(0).toString(), false)
        tenureDropDown.setAdapter(bankNamesAdapter)

        tenureDropDown.setOnClickListener {
            tenureDropDown.showDropDown()
        }
        tenureDropDown.setOnItemClickListener { parent, view, position, id ->
            getEmiRoi()
        }
        loanAmountSeekBar.max = 14

        loanAmountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var progress_custom = MIN + ( i * STEP )
                if (i == 14){
                    progress_custom = 100000
                }
                loanAmountEditText.setText(progress_custom.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

        btn_next.setOnClickListener {
            if (isCreateFlow) {
                sendLoanRequest()
            }else{
                loanProcessResponse?.let {
                    val intent1 = Intent(this@InitiateApplyLoanActivity, UploadKycDetailsActivity::class.java)
                    intent1.putExtra("loanResponse",it)
                    intent1.putExtra(ArthanFinConstants.IS_CREATE_FLOW,false)
                    intent1.putExtra("customerData",intent.getSerializableExtra("customerData"))
                    startActivity(intent1)
                }
            }
        }

        // setting default to 6 months
        tenureDropDown.setSelection(0)
        getEmiRoi()

        img_back.setOnClickListener{
            finish()
        }

        if (!isCreateFlow)
            showLoanDetails()

        getAllBusinessTypes()
    }

    private fun showLoanDetails() {
        val loanDetails:LoanDetails = intent.getSerializableExtra("loanDetails") as LoanDetails
        loanDetails.loanId?.let {
            getPRLoanDetails(it)
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

    private fun getPRLoanDetails(loanId: String) {
        showProgressDialog()
        ApiClient().getAuthApiService(this).getPRLoanDetails(loanId,DataManager.applicantType).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                hideProgressDialog()
                val response = response.body() as LoanProcessResponse
                response.loanAmount?.let {
                    loanAmountEditText.setText(it)
                }
                response.purpose?.let {
                    loanPurposeDropDown.setText(it,false)
                }
                response.tenure?.let {
                    tenureDropDown.setText(it,false)
                }
                response.emi?.let {
                    monthlyEMI.text = it
                }

                loanProcessResponse = response
            }
        })
    }

    private fun getEmiRoi() {
        val jsonObject = JsonObject()
        val tenure = tenureDropDown.text.split(" ").first()
        if (loanAmountEditText.text.toString().isEmpty()){
            return
        }
        val loanAmount = loanAmountEditText.text.toString().replace(",","").replace(".00","")
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmt", loanAmount)
        jsonObject.addProperty("tenure", tenure)

        ApiClient().getApiService(applicationContext).getEmiRoi(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val emiroiResponse = response.body() as LoanProcessResponse
                monthlyEMI.text = "${getString(R.string.Rs)} ${emiroiResponse.emi}"
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
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
                listOfBusiness?.let { initateDropDownForBusinessPurposeField(it)
                }
            }
        })
    }

    private fun initateDropDownForBusinessPurposeField(listOfBusiness: Array<String>?) {
        val bussinessPurposeAdapter = ArrayAdapter(this,
            R.layout.emi_options, listOfBusiness!!)
        if (intent.getBooleanExtra(IS_CREATE_FLOW,false)){
            loanPurposeDropDown.setText(bussinessPurposeAdapter.getItem(0).toString(), false)
        }
        loanPurposeDropDown.setAdapter(bussinessPurposeAdapter)
        loanPurposeDropDown.setOnItemClickListener { parent, view, position, id ->
            val tenure = listOfBusiness[position]
            if(tenure == "Select Loan Purpose"){
                Toast.makeText(
                    this@InitiateApplyLoanActivity,
                    "Please enter the purpose of loan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    private fun sendLoanRequest() {
        if(tenureDropDown.text.toString() == "Select Tenure"){
            Toast.makeText(
                this@InitiateApplyLoanActivity,
                "Please select the tenure of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (loanPurposeDropDown.text.toString() == "Select Loan Purpose"){
            Toast.makeText(
                this@InitiateApplyLoanActivity,
                "Please enter the purpose of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val loanAmount = loanAmountEditText.text.toString().replace(",","")
        if (loanAmount.toInt() < 30000)
        {
            Toast.makeText(
                this@InitiateApplyLoanActivity,
                "Loan Amount less than 30000 not allowed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (currentLocation == null){
            FiniculeUtil.showGPSNotEnabledDialog(this)
            return
        }

        showProgressDialog()
        val tenure = tenureDropDown.text.split(" ").first()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData?.customerId)
        jsonObject.addProperty("loanAmount", loanAmount)
        jsonObject.addProperty("tenure", tenure)
        jsonObject.addProperty("purpose", loanPurposeDropDown.text.toString())
        currentLocation?.latitude?.let {
            jsonObject.addProperty("lat",it )
        }
        currentLocation?.longitude?.let {
            jsonObject.addProperty("lng",it)
        }
        
        apiClient.getAuthApiService(this).applyForLoan(jsonObject).enqueue(object : Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                hideProgressDialog()
                val loanResponse = response.body()
                if (loanResponse?.loanCount!! > 1){
                    showCoApplicantDialog(loanResponse)
                }else{
                    navigateToKycDetailsActivity(loanResponse,true)
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@InitiateApplyLoanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showCoApplicantDialog(loanResponse: LoanProcessResponse) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Do you want to add Co-Applicant")
            // if the dialog is cancelable
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                dialog.dismiss()
                navigateToKycDetailsActivity(loanResponse, false)
            }.setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                navigateToKycDetailsActivity(loanResponse, true)
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Co-Applicant Data")
        alert.show()
    }

    private fun navigateToKycDetailsActivity(loanResponse: LoanProcessResponse,navigateToBusinessDetails:Boolean) {
        val intent = Intent(this@InitiateApplyLoanActivity, UploadKycDetailsActivity::class.java)
        intent.putExtra(IS_CREATE_FLOW,true)
        intent.putExtra(MOVE_TO_BUSINESS_DETAILS,navigateToBusinessDetails)
        if (!navigateToBusinessDetails){
            loanResponse.applicantType = "CA"
        }
        intent.putExtra("loanResponse", loanResponse)
        startActivity(intent)
    }
}