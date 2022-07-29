package com.av.arthanfinance.user_kyc

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioKycResponseListener
import `in`.digio.sdk.kyc.DigioSession
import android.Manifest
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.BankDetilsResponse
import com.av.arthanfinance.applyLoan.IFSCCode_Patter
import com.av.arthanfinance.applyLoan.model.DigilockerTokenResponse
import com.av.arthanfinance.databinding.ActivityUploadBankDetailsBinding
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.copyFile
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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

class UploadBankDetailsActivity : BaseActivity(), DigioKycResponseListener {
    private lateinit var activityUploadBankDetailsBinding: ActivityUploadBankDetailsBinding
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var kycCompleteStatus = "90"
    private var customerData: CustomerHomeTabResponse? = null
    private lateinit var tokenId: String
    private lateinit var kId: String
    private var REQ_CODE_BANK_STATEMENT = 444
    private var mLoanId: String? = null
    private var mCustomerId: String? = null
    override val layoutId: Int
        get() = R.layout.activity_upload_bank_details

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

        setSupportActionBar(activityUploadBankDetailsBinding.tbUploadBankDetails)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Provide Bank Account Details"

        activityUploadBankDetailsBinding.pbKycBank.max = 100
        ObjectAnimator.ofInt(activityUploadBankDetailsBinding.pbKycBank, "progress", 90)
            .setDuration(1000).start()
        activityUploadBankDetailsBinding.tvPercent.text = "${kycCompleteStatus}%"

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

        activityUploadBankDetailsBinding.btnUploadbankstmt.setOnClickListener {
            val pdfPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            pdfPickerIntent.type = "application/pdf"
            startActivityForResult(
                Intent.createChooser(pdfPickerIntent, "Choose File"),
                REQ_CODE_BANK_STATEMENT
            )
        }


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
                        .uploadStatement(multiPartBody, "KBPI-I00X-P6WR", mCustomerId)

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

        val clientId = "AIV9X8IU1LYFM9RE7EKINUO98RE6MX6L"
        val clientSecret = "FJ872EVIBCC11XJE1HR3KAZRK2ZU4O5P"

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
                        val verified = bankAccountResponse.verified
                        val name = bankAccountResponse.name
                        if (verified) {
                            saveBankAccountDetails(ifsc, accNum, bankName, name)
                        }
                        Toast.makeText(
                            this@UploadBankDetailsActivity,
                            "" + verified,
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
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
                        activityUploadBankDetailsBinding.btnBankDetails.visibility = View.GONE
                        activityUploadBankDetailsBinding.btnVideoKyc.visibility = View.VISIBLE
                        /* val sharedPref: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
                         val prefsEditor = sharedPref?.edit()
                         val gson = Gson()
                         val json: String = gson.toJson(docResponse)
                         prefsEditor?.putString("customerData", json)
                         prefsEditor?.apply()
                         customerData = gson.fromJson(json, CustomerHomeTabResponse::class.java)*/
                        activityUploadBankDetailsBinding.btnSkipVideoKyc.setOnClickListener {
                            val intent = Intent(
                                this@UploadBankDetailsActivity,
                                HomeDashboardActivity::class.java
                            )
                            intent.putExtra("customerData", customerData)
                            startActivity(intent)
                            finish()
                        }
                        activityUploadBankDetailsBinding.btnVideoKyc.setOnClickListener {
//                            getKid()
                            showDialog()
                        }

                        Toast.makeText(
                            this@UploadBankDetailsActivity,
                            "Bank account verified successfully",
                            Toast.LENGTH_SHORT
                        ).show()


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
                        "PAN Details Upload Failed. Please Try After Sometime",
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
//        val body = dialog.findViewById(R.id.body) as TextView
//        body.text = title
        val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        val noBtn = dialog.findViewById(R.id.noBtn) as TextView
        yesBtn.setOnClickListener {
            getKid()
            dialog.dismiss()
        }
        noBtn.setOnClickListener {
            val intent = Intent(
                this@UploadBankDetailsActivity,
                HomeDashboardActivity::class.java
            )
            intent.putExtra("customerData", customerData)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun getKid() {
        val jsonObject = JsonObject()
        val jsonObject1 = JsonObject()
        val jsonObject2 = JsonObject()
        val actionArray = JsonArray()
        val subActionArray = JsonArray()

        jsonObject.addProperty("customer_identifier", customerData!!.mobNo)
        jsonObject.addProperty("notify_customer", false)
        jsonObject.addProperty("generate_access_token", true)

        jsonObject1.addProperty("type", "VIDEO")
        jsonObject1.addProperty("method", "OTP_NONE")
        jsonObject1.addProperty("title", "Self Video KYC")
        jsonObject1.addProperty("description", "Please follow the instructions and record a video")
        jsonObject1.addProperty("allow_ocr_data_update", true)
        jsonObject1.addProperty("face_match_obj_type", "MATCH_REQUIRED")

        jsonObject2.addProperty("type", "USER_INSTRUCTION")
        jsonObject2.addProperty("title", "Documents preparation")
        jsonObject2.addProperty(
            "description",
            "Please keep your Pan and Aadhar card ready to show in the video"
        )

        subActionArray.add(jsonObject2)
        jsonObject1.add("sub_actions", subActionArray)

        actionArray.add(jsonObject1)
        jsonObject.add("actions", actionArray)

        val clientId = "AIV9X8IU1LYFM9RE7EKINUO98RE6MX6L"
        val clientSecret = "FJ872EVIBCC11XJE1HR3KAZRK2ZU4O5P"

        val base = clientId + ":" + clientSecret

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getBankDetailsApiService(this).createRequestDigilocker(authHeader, jsonObject)
            .enqueue(object : Callback<DigilockerTokenResponse> {
                override fun onResponse(
                    call: Call<DigilockerTokenResponse>,
                    response: Response<DigilockerTokenResponse>
                ) {
                    hideProgressDialog()
                    val accessToken = response.body()!!.accessToken1
                    tokenId = accessToken.token
                    kId = accessToken.kId

                    val permissionCamera = ContextCompat.checkSelfPermission(
                        this@UploadBankDetailsActivity,
                        Manifest.permission.CAMERA
                    )
                    val permissionRecordAudio = ContextCompat.checkSelfPermission(
                        this@UploadBankDetailsActivity,
                        Manifest.permission.RECORD_AUDIO
                    )
                    val permissionLocation = ContextCompat.checkSelfPermission(
                        this@UploadBankDetailsActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    val permissionRead = ContextCompat.checkSelfPermission(
                        this@UploadBankDetailsActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionRecordAudio == PackageManager.PERMISSION_GRANTED && permissionLocation == PackageManager.PERMISSION_GRANTED && permissionRead == PackageManager.PERMISSION_GRANTED) {
                        customerData!!.mobNo?.let { startVideoKyc(tokenId, kId, it) }
                    } else {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ), MY_CAMERA_PERMISSION_CODE
                        )
                    }

                    //startVideoKyc(tokenId, kId, "7609963809")

                }

                override fun onFailure(call: Call<DigilockerTokenResponse>, t: Throwable) {
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

    private fun requestPermission(): Boolean {

        if (this.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED
            && this.checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
            && this.checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ), MY_CAMERA_PERMISSION_CODE
            )
        } else {
            return true
        }

        return false
    }

    private fun startVideoKyc(tokenId: String, kId: String, mobNo: String) {
        if (requestPermission()) {
            try {
                val config = DigioKycConfig()
                config.setEnvironment(DigioEnvironment.SANDBOX)
                val digioSession = DigioSession()
                digioSession.init(this, config)
                digioSession.startSession(
                    kId,
                    mobNo,
                    tokenId,
                    this
                )
            } catch (e: Exception) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val permissionCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val permissionRecordAudio = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionRecordAudio == PackageManager.PERMISSION_GRANTED && permissionLocation == PackageManager.PERMISSION_GRANTED && permissionRead == PackageManager.PERMISSION_GRANTED) {
            startVideoKyc(tokenId, kId, customerData!!.mobNo.toString())
        } else {
            requestPermission()
            // Toast.makeText(this, "Permission denied by user. Could not able to record video.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkIFSCCode(ifscCode: String): Boolean {
        return IFSCCode_Patter.matcher(ifscCode).matches()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDigioKycFailure(requestId: String?, response: String?) {
        Toast.makeText(this, "Video KYC failed", Toast.LENGTH_SHORT).show()
        val intent = Intent(
            this@UploadBankDetailsActivity,
            HomeDashboardActivity::class.java
        )
        intent.putExtra("customerData", customerData)
        startActivity(intent)
        finish()

    }

    override fun onDigioKycSuccess(requestId: String?, response: String?) {
        val intent = Intent(
            this@UploadBankDetailsActivity,
            HomeDashboardActivity::class.java
        )
        intent.putExtra("customerData", customerData)
        startActivity(intent)
        finish()
    }
}