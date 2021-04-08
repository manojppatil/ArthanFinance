package com.example.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.arthanfinance.CustomerHomeTabResponse
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_initiate_apply_loan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*


class InitiateApplyLoanActivity : AppCompatActivity() {

    private lateinit var tenureDropDown: AutoCompleteTextView
    private lateinit var loanAmountSeekBar: SeekBar
    private lateinit var loanAmountEditText: EditText
    private lateinit var inLakhs: TextView
    private lateinit var monthlyEMI: TextView
    private lateinit var loanPurpose: EditText
    private lateinit var apiClient: ApiClient
    private lateinit var customerData: CustomerHomeTabResponse
    private var tenureList = arrayOf("Select Tenure", "12 Months", "18 Months","24 Months")

    var MIN = 100000
    var MAX = 1000000
    var STEP = 10000

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initiate_apply_loan)

        if (supportActionBar != null)
            supportActionBar?.hide()

        customerData = intent.extras?.get("customerData") as CustomerHomeTabResponse
        apiClient = ApiClient()

        loanAmountSeekBar = findViewById(R.id.loanAmountSeekbar)
        loanAmountEditText = findViewById(R.id.loanAmountEditText)
        loanPurpose = findViewById(R.id.txtLoanPurpose)
        monthlyEMI = findViewById(R.id.monthlyEMI)

        loanAmountEditText.isCursorVisible = true
        loanAmountEditText.isFocusableInTouchMode = true
        loanAmountEditText.isEnabled = true

        inLakhs = findViewById(R.id.inLakhs)

//        loanAmountEditText.addTextChangedListener(object: TextWatcher{
//            override fun afterTextChanged(s: Editable?) {
//                val formatter: NumberFormat = NumberFormat.getCurrencyInstance()
//                formatter.currency = Currency.getInstance(Locale("en", "in"))
//                val totalAmlunt = (loanAmountEditText.text).toString().replace(",","")
//                val finalFormat = formatter.format(totalAmlunt).replace(formatter.getCurrency().symbol, "");
//                loanAmountEditText.setText(finalFormat.toString())
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//        } )


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
        val amountTextwidth = amountTextPaint.measureText("Loan Application")

        val amountTextshader = LinearGradient(0f, 0f, amountTextwidth, loan_amount_text.textSize, resources.getColor(
            R.color.dark_orange2, theme), resources.getColor(
            R.color.indigoBlue, theme), Shader.TileMode.CLAMP)
        loan_amount_text.paint.shader = amountTextshader
        emiMonthly.paint.shader = amountTextshader

        tenureDropDown = findViewById(R.id.tenureDropDown)
        val bankNamesAdapter = ArrayAdapter(this,
            R.layout.emi_options, tenureList)
        tenureDropDown.setText(bankNamesAdapter.getItem(0).toString(), false)
        tenureDropDown.setAdapter(bankNamesAdapter)

        tenureDropDown.setOnItemClickListener { parent, view, position, id ->
            val tenure = tenureList[position]
            if(tenure == "Select Tenure"){
                Toast.makeText(
                    this@InitiateApplyLoanActivity,
                    "Please select proper tenure of loan",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //make API Call
                getEmiRoi()
            }
        }
        loanAmountSeekBar.max = (MAX - MIN) / STEP

        loanAmountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val progress_custom = MIN + ( i * STEP )
                val formatter: NumberFormat = NumberFormat.getCurrencyInstance()
                formatter.currency = Currency.getInstance(Locale("en", "in"))

                val finalFormat = formatter.format(progress_custom).replace(formatter.getCurrency().symbol, "");
                loanAmountEditText.setText(finalFormat.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"start tracking",Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext,"stop tracking",Toast.LENGTH_SHORT).show()
            }
        })

        btn_next.setOnClickListener {
            sendLoanRequest()
        }

        img_back.setOnClickListener{
            finish()
        }
    }

    private fun getEmiRoi() {
        val jsonObject = JsonObject()

        val tenure = tenureDropDown.text.split(" ").first()
        val loanAmount = loanAmountEditText.text.toString().replace(",","")
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

    private fun sendLoanRequest() {
        if(tenureDropDown.text.toString() == "Select Tenure"){
            Toast.makeText(
                this@InitiateApplyLoanActivity,
                "Please select the tenure of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val loanAmount = loanAmountEditText.text.toString().replace(",","")
        val tenure = tenureDropDown.text.split(" ").first()
        val purpose = loanPurpose.text.toString()
        if(purpose.isEmpty()) {
            Toast.makeText(
                this@InitiateApplyLoanActivity,
                "Please enter the purpose of loan",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        jsonObject.addProperty("loanAmount", loanAmount)
        jsonObject.addProperty("tenure", tenure)
        jsonObject.addProperty("purpose", purpose)

        apiClient.getApiService(this).applyForLoan(jsonObject).enqueue(object : Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val loanResponse = response.body()

                val intent = Intent(this@InitiateApplyLoanActivity, UploadKycDetailsActivity::class.java)
                intent.putExtra("loanResponse", loanResponse)
                startActivity(intent)
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@InitiateApplyLoanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}