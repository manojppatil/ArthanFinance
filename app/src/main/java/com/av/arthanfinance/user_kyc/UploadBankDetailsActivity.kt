package com.av.arthanfinance.user_kyc

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.aa_sdk_integration.FiuAPIClient
import com.av.arthanfinance.aa_sdk_integration.FiuAPIInterface
import com.av.arthanfinance.aa_sdk_integration.InitiateConsentRequest
import com.av.arthanfinance.aa_sdk_integration.InitiateConsentResponse
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.databinding.ActivityUploadBankDetailsBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.copyFile
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sdk.pirimid_sdk.PirimidSDK
import com.sdk.pirimid_sdk.ResponseData
import kotlinx.android.synthetic.main.activity_upload_bank_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class UploadBankDetailsActivity : BaseActivity() {
    private lateinit var activityUploadBankDetailsBinding: ActivityUploadBankDetailsBinding
    private var kycCompleteStatus = "50"
    private var customerData: CustomerHomeTabResponse? = null
    private var REQ_CODE_BANK_STATEMENT = 444
    private var mLoanId: String? = null
    private var mCustomerId: String? = null
    private var mBankId: String? = null
    var banklistArray = arrayOf("")
    var branchlistArray = arrayOf("")
    val banks = arrayOf(
        "Bank of Baroda",
        "Bank of India",
        "ICICI Bank",
        "State bank of India",
        "Axis bank",
        "HDFC Bank",
        "Akola bank"
    )

    //    val baseUrl = "https://app-uat.onemoney.in/" // uat env
    private val baseUrl = "https://aa-app.onemoney.in/" // prod env
    private val orgId = "ARTHANFINANCE"
    private val clientId = "27d42d7fd01205a52baed74050d1db71efedb1eb"
    private val clientSecret = "ab2f77d17afd5d574b71d4e52d2a75f427f14a1b"
    private val fipId = "ICICI-FIP"
    private var fiuAPIInterface: FiuAPIInterface? = null
    var initiateConsentResponse: InitiateConsentResponse? = null

    override val layoutId: Int
        get() = R.layout.activity_upload_bank_details

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadBankDetailsBinding = ActivityUploadBankDetailsBinding.inflate(layoutInflater)
        setContentView(activityUploadBankDetailsBinding.root)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
        }

        mCustomerId = customerData!!.customerId

        fiuAPIInterface = FiuAPIClient.getClient().create(
            FiuAPIInterface::class.java
        )

//        createConsentMethod(mCustomerId!!)

        setSupportActionBar(activityUploadBankDetailsBinding.tbUploadBankDetails)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Provide Bank Account Details"

        activityUploadBankDetailsBinding.pbKycBank.max = 100
        ObjectAnimator.ofInt(activityUploadBankDetailsBinding.pbKycBank, "progress", 50)
            .setDuration(1000).start()
        activityUploadBankDetailsBinding.tvPercent.text = "${kycCompleteStatus}%"

        activityUploadBankDetailsBinding.textviewBankMobile.setText(customerData!!.mobNo)

        activityUploadBankDetailsBinding.btnBankDetails.setOnClickListener {

            if (activityUploadBankDetailsBinding.tieBankName.text.toString().isEmpty()) {
                Toast.makeText(this, "Bank Name is Empty", Toast.LENGTH_SHORT).show()
            } else if (activityUploadBankDetailsBinding.tieCAccountNumber.text.toString()
                    .isEmpty()
            ) {
                Toast.makeText(this, "Account Number is Empty", Toast.LENGTH_SHORT).show()
            } else if (activityUploadBankDetailsBinding.tieIfscCode.text.toString().isEmpty()) {
                Toast.makeText(this, "IFSC code is Empty", Toast.LENGTH_SHORT).show()
            } else if (!checkIFSCCode((activityUploadBankDetailsBinding.tieIfscCode.text.toString()))) {
                Toast.makeText(this, "IFSC code Invalid", Toast.LENGTH_SHORT).show()
            } else if (activityUploadBankDetailsBinding.tieAccountNumber.text.toString() != activityUploadBankDetailsBinding.tieCAccountNumber.text.toString()) {
                Toast.makeText(
                    this,
                    "Confirm Account No. not matched with Account No.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getBankDetailsFromDigio(
                    activityUploadBankDetailsBinding.tieIfscCode.text.toString(),
                    activityUploadBankDetailsBinding.tieCAccountNumber.text.toString(),
                    activityUploadBankDetailsBinding.tieBankName.text.toString()
                )
                showProgressDialog()
            }
        }

        activityUploadBankDetailsBinding.editBankMobile.setOnClickListener {
            activityUploadBankDetailsBinding.textviewBankMobile.isFocusable = true
            activityUploadBankDetailsBinding.textviewBankMobile.isEnabled = true
            activityUploadBankDetailsBinding.textviewBankMobile.isClickable = true
            activityUploadBankDetailsBinding.textviewBankMobile.isFocusableInTouchMode = true
        }

        activityUploadBankDetailsBinding.btnUploadbankstmt.setOnClickListener {
            val pdfPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            pdfPickerIntent.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(pdfPickerIntent, "Choose File"),
                REQ_CODE_BANK_STATEMENT
            )
        }

        activityUploadBankDetailsBinding.tieBankName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                fetchAllBanks()
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        activityUploadBankDetailsBinding.tieBankBranch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    fetchAllBranches(mBankId!!)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    Log.e("Error", e.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        activityUploadBankDetailsBinding.btnAggregator.setOnClickListener {
            createConsentMethod(mCustomerId!!)
        }

//        fetchAllBanks()
    }

    private fun createConsentMethod(requestId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("applicantId", requestId)
        jsonObject.addProperty("mobileNo", customerData!!.mobNo)
        showProgressDialog()
        ApiClient().getAuthApiService(this).getAAReferenceId(jsonObject).enqueue(object :
            Callback<AggregatorConsentResponse> {
            override fun onFailure(call: Call<AggregatorConsentResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBankDetailsActivity, "Consent Failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AggregatorConsentResponse>,
                response: Response<AggregatorConsentResponse>
            ) {
                hideProgressDialog()
                val res = response.body()
                val referenceId = res?.referenceId
                Log.e("TAGCONSENT", referenceId + "")
                initializeSDK(referenceId!!)
            }
        })
    }

    private fun initializeSDK(consentId: String) {
        val intent = Intent()
        intent.putExtra("ORGANISATION_ID", orgId)
        intent.putExtra("CLIENT_ID", clientId)
        intent.putExtra("CLIENT_SECRET", clientSecret)
        intent.putExtra("BASE_URL", baseUrl)
        intent.putExtra("consent_id", consentId)
        intent.putExtra("party_identifier", customerData!!.mobNo)
//        intent.putExtra("fipId", fipId)

        /// PIRIMID SDK Call
        PirimidSDK.initializePirimidSDK(this, intent, object : ResponseData {
            /// Success Response
            override fun onSuccess(responseDetails: Intent) {
                Log.d("main", responseDetails.getStringExtra("consent_action_type")!!)
                Log.d("main", responseDetails.getStringExtra("consent_status")!!)
                val consentActionType = responseDetails.getStringExtra("consent_action_type")
                Log.d("main", responseDetails.getStringExtra("CODE")!!)
                Log.d("main", responseDetails.getStringExtra("MESSAGE")!!)
                updateStage("BANK_PA")
            }

            /// Failure Response
            override fun onFailure(responseDetails: Intent) {
                Log.d("main", responseDetails.getStringExtra("CODE")!!)
                Log.d("main", responseDetails.getStringExtra("MESSAGE")!!)
                lyt_bankaggregator.visibility = View.GONE
                lyt_bankpage.visibility = View.VISIBLE
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_BANK_STATEMENT -> {
                    data?.apply {
                        if (this.data == null) return
                        val file = copyFile(this@UploadBankDetailsActivity, this.data!!)
                        if (file != null && file.absolutePath.isNotEmpty()) {
                            uploadStatement(file.absolutePath)
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadStatement(fileLocation: String) {
        if (mCustomerId == null) {
//            mLoanId = loanResponse!!.loanId
            mCustomerId = customerData!!.customerId
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(fileLocation)
                val requestBody: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                val multiPartBody =
                    MultipartBody.Part.createFormData("file", file.name, requestBody)
                val response =
                    ApiClient().getMasterApiService(this@UploadBankDetailsActivity)
                        .uploadStatement(multiPartBody, mCustomerId, mCustomerId)

                withContext(Dispatchers.Main) {
                    try {
                        Toast.makeText(
                            this@UploadBankDetailsActivity, "Statement Uploaded Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val mDocId = response?.docId ?: "DOC00053300"

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getBankDetailsFromDigio(ifsc: String, accNum: String, bankName: String) {
        val jsonObject = JsonObject()

        jsonObject.addProperty("beneficiary_account_no", accNum)
        jsonObject.addProperty("beneficiary_ifsc", ifsc)

        //SANDBOX CREDS
//        val clientId = "AI52KOUVC2PQTONW1ZKB92RU22UL8491"
//        val clientSecret = "B6DXG4SV4YJJC2VDA54WTLY6CTJKEUZH"

        //PRD CREDS
        val clientId = "AIZ1SHB77YJBZ6HFAGYR4BTUI84A6DOF"
        val clientSecret = "ZLYKT9FT7UUAIZGVVUPIWSFN3N62Y99O"

        val base = "$clientId:$clientSecret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getBankDetailsApiService(this).verifyBank(authHeader, jsonObject)
            .enqueue(object :
                Callback<BankDetilsResponse> {
                override fun onResponse(
                    call: Call<BankDetilsResponse>,
                    response: Response<BankDetilsResponse>
                ) {
                    try {
                        val bankAccountResponse = response.body() as BankDetilsResponse
                        Log.e("TAG", bankAccountResponse.name.toString())
                        val verified = bankAccountResponse.verified
                        val name = bankAccountResponse.name
                        if (verified) {
                            saveBankAccountDetails(ifsc, accNum, bankName, name)
                        }
//                        Toast.makeText(
//                            this@UploadBankDetailsActivity,
//                            "" + verified,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UploadBankDetailsActivity,
                            "Service Failure, Please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BankDetilsResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(
                        this@UploadBankDetailsActivity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun saveBankAccountDetails(
        ifsc: String,
        accNum: String,
        bankName: String,
        name: String
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("bankName", bankName)
        jsonObject.addProperty("customerAccountNumber", accNum)
        jsonObject.addProperty("customerName", name)
        jsonObject.addProperty("customerIfscCode", ifsc)

        ApiClient().getAuthApiService(this).uploadCustomerBankDetails(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {

                if (response.body() != null) {
                    val docResponse = response.body() as AuthenticationResponse
                    val apiCode = docResponse.apiCode
                    //val message = docResponse.message

                    if (response.body() != null) {
                        hideProgressDialog()
                        showDialog()
                        /* val sharedPref: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
                         val prefsEditor = sharedPref?.edit()
                         val gson = Gson()
                         val json: String = gson.toJson(docResponse)
                         prefsEditor?.putString("customerData", json)
                         prefsEditor?.apply()
                         customerData = gson.fromJson(json, CustomerHomeTabResponse::class.java)*/

                    } else {
                        hideProgressDialog()
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            Toast.makeText(
                                this@UploadBankDetailsActivity,
                                jObjError.getJSONObject("error").getString("message"),
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@UploadBankDetailsActivity,
                                e.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Toast.makeText(
                            this@UploadBankDetailsActivity,
                            "Account number verification failed. Please try After again",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressDialog()
                    }
                } else {
                    hideProgressDialog()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadBankDetailsActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@UploadBankDetailsActivity, e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Toast.makeText(
                        this@UploadBankDetailsActivity,
                        "Bank Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBankDetailsActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.videokyc_layout)
        val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        yesBtn.setOnClickListener {
            val intent = Intent(
                this@UploadBankDetailsActivity,
                UploadBusinessDetailsActivity::class.java
            )
            intent.putExtra("customerData", customerData)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun checkIFSCCode(ifscCode: String): Boolean {
        return IFSCCode_Patter.matcher(ifscCode).matches()
    }

    override fun onBackPressed() {
        finish()
    }


    private fun fetchAllBanks() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("bankName", tieBankName.text.trim().toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).getBank(jsonObject).enqueue(object :
            Callback<Bank> {
            override fun onFailure(call: Call<Bank>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBankDetailsActivity, "API call Failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<Bank>,
                response: Response<Bank>
            ) {
                hideProgressDialog()
                if (response.body() != null) {
                    val docResponse = response.body() as Bank
                    Log.d("TAG", docResponse.banks!![0].bankName.toString())
                    banklistArray = arrayOf(docResponse.banks.toString())
                    Log.d("TAGLIST", docResponse.banks.toString())
                    val banksnames = arrayOf(docResponse.banks[0].bankName)
                    val adapter = ArrayAdapter(
                        this@UploadBankDetailsActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        banksnames
                    )
                    activityUploadBankDetailsBinding.tieBankName.setAdapter(adapter)
                    activityUploadBankDetailsBinding.tieBankName.setOnItemClickListener { adapterView, view, i, l ->
                        Toast.makeText(applicationContext, "" + banklistArray[i], Toast.LENGTH_LONG)
                            .show()
                        mBankId = docResponse.banks[i].bankId
                    }

                }
            }
        })
    }

    private fun fetchAllBranches(bankId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("bankId", bankId)
        jsonObject.addProperty("branchName", tieBankBranch.text.trim().toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this).getBranch(jsonObject).enqueue(object :
            Callback<Bank> {
            override fun onFailure(call: Call<Bank>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBankDetailsActivity, "API call Failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<Bank>,
                response: Response<Bank>
            ) {
                hideProgressDialog()
                if (response.body() != null) {
                    val docResponse = response.body() as Bank
                    Log.d("TAG", docResponse.branches!![0].ifsc.toString())
                    branchlistArray = arrayOf(docResponse.branches.toString())
                    Log.d("TAGLIST", docResponse.branches.toString())
                    val branchnames = arrayOf(docResponse.branches[0].branchName)
                    val adapter = ArrayAdapter(
                        this@UploadBankDetailsActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        branchnames
                    )
                    activityUploadBankDetailsBinding.tieBankBranch.setAdapter(adapter)
                    activityUploadBankDetailsBinding.tieBankBranch.setOnItemClickListener { adapterView, view, i, l ->
                        Toast.makeText(
                            applicationContext,
                            "" + branchlistArray[i],
                            Toast.LENGTH_LONG
                        )
                            .show()
                        activityUploadBankDetailsBinding.tieIfscCode.setText(docResponse.branches[i].ifsc.toString())
                    }
                }
            }
        })
    }

    private fun updateStage(stage: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("stage", stage)
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBankDetailsActivity, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {

                    val intent = Intent(
                        this@UploadBankDetailsActivity,
                        UploadBusinessDetailsActivity::class.java
                    )
                    intent.putExtra("customerData", customerData)
                    startActivity(intent)
                    finish()
                }

            }
        })
    }
}