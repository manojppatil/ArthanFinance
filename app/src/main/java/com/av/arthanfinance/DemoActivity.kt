package com.av.arthanfinance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import `in`.digio.sdk.kyc.DigioStateLessSession
import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import android.graphics.Color
import java.lang.Exception
import `in`.digio.sdk.kyc.DigioTaskResponse
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import android.util.Base64
import android.widget.Toast
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_demo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.av.arthanfinance.applyLoan.BankDetilsResponse
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import java.io.File


class DemoActivity : AppCompatActivity(), DigioResponseListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        uploadPan.setOnClickListener {
            if (requestPermission()) {
                getPanDataFromDigio()
            }
        }

        uploadAadhar.setOnClickListener {
            getAadharDataFromDigio()
        }

        bankDetails.setOnClickListener {
            val ifsc = ifscCode.text.toString()
            val accountNumber = accountNum.text.toString()

            if (ifsc.equals("")) {
                Toast.makeText(this, "Please Enter Your IFSC Code", Toast.LENGTH_SHORT).show()
            } else if (accountNumber.equals("")) {
                Toast.makeText(this, "Please Enter Your Bank Account Number", Toast.LENGTH_SHORT)
                    .show()
            } else {
                getBankDetailsFromDigio(ifsc.toString(), accountNumber.toString())
            }
        }
    }

    private fun getBankDetailsFromDigio(ifscCode: String, accountNumber: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("beneficiary_account_no", "33220439293")
        jsonObject.addProperty("beneficiary_ifsc", "SBIN0007049")

        val clientId = "AIV9X8IU1LYFM9RE7EKINUO98RE6MX6L"
        val clientSecret = "FJ872EVIBCC11XJE1HR3KAZRK2ZU4O5P"

        val base = clientId + ":" + clientSecret

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getBankDetailsApiService(this).verifyBank(authHeader, jsonObject)
            .enqueue(object :
                Callback<BankDetilsResponse> {
                override fun onResponse(
                    call: Call<BankDetilsResponse>,
                    response: Response<BankDetilsResponse>
                ) {
                    val bankAccountResponse = response.body() as BankDetilsResponse
                    val verified = bankAccountResponse.verified
                    if (verified) {
                        val accountHolderName = bankAccountResponse.name
                    }
                    Toast.makeText(this@DemoActivity, "" + verified, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<BankDetilsResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        this@DemoActivity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getAadharDataFromDigio() {
        val digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()

        val kycRequest = DigioTaskRequest()
        kycRequest.taskType = DigioTaskType.OFFLINE_KYC// For Aadhaar card
        kycRequest.isFaceMatch =
            false // Optional,  In case business required selfie and face match with Aadhaar
        digioTaskList.add(kycRequest)

        val config = DigioKycConfig()
        config.setEnvironment(DigioEnvironment.SANDBOX)
        config.setPrimaryColor(Color.parseColor("#17c39b"))
        config.setSecondaryColor(Color.parseColor("#B4E9D8"))

        try {
            val digioStateLessSession = DigioStateLessSession()
            digioStateLessSession.init(
                this,
                config,
                "SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X",
                "SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB"
            )
            digioStateLessSession.startStateLessSession(digioTaskList, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPanDataFromDigio() {
        val digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()

        val kycRequest = DigioTaskRequest()
        kycRequest.taskType = DigioTaskType.ID_ANALYSIS
        kycRequest.isFaceMatch =
            false // Optional,  In case business required selfie and face match with Aadhaar
        digioTaskList.add(kycRequest)

        val config = DigioKycConfig()
        config.setEnvironment(DigioEnvironment.SANDBOX)
        config.setPrimaryColor(Color.parseColor("#17c39b"))
        config.setSecondaryColor(Color.parseColor("#B4E9D8"))
        try {
            val digioStateLessSession = DigioStateLessSession()
            digioStateLessSession.init(
                this,
                config,
                "SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X",
                "SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB"
            )
            digioStateLessSession.startStateLessSession(digioTaskList, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onDigioKycSuccess(documentId: String?, message: String?) {
        Toast.makeText(this, documentId, Toast.LENGTH_SHORT).show()
    }

    fun onDigioKycFailure(documentId: String?, message: String?) {
        Toast.makeText(this, documentId, Toast.LENGTH_SHORT).show()
    }

    private fun encodeString(str: String): String? {
        val data: ByteArray
        var base64 = ""
        try {
            data = str.toByteArray(charset("UTF-8"))
            base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return base64
    }

    override fun onDigioEventTracker(eventTracker: JSONObject) {

    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
        for (digiTaskResponse in failure) {
            val value = digiTaskResponse.isSuccess
            Toast.makeText(this, "${value}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {

            val digioTaskRequest = digiTaskResponse.getTask()

            val taskType = digioTaskRequest.taskType
            if (taskType == DigioTaskType.ID_ANALYSIS) {
                val mainResponse =
                    digiTaskResponse.getResponse()// offline_kyc or idCard analysis response
                val analysisResponse = mainResponse.getJSONObject("analysis_response")
                val name = analysisResponse.getString("name")
                val panNumber = analysisResponse.getString("id_no")
                val panImage = mainResponse.getString("id_front_uri")

                Glide.with(this).load(panImage).into(pan_img)
                panName.setText(name)
                panNum.setText(panNumber)

            } else if (taskType == DigioTaskType.OFFLINE_KYC) {
                val mainResponse = digiTaskResponse.getResponse()// offline_kyc or idCard analysis response
                val analysisResponse = mainResponse.getJSONObject("offline_kyc_response")
                val aadharImage = analysisResponse.getString("photo")
                val personalInformation = analysisResponse.getJSONObject("personal_information")
                val addressInformation = analysisResponse.getJSONObject("address_information")

                val name = personalInformation.getString("name")
                val address = addressInformation.getString("text")

                aadharName.setText(name)
                aadharAddress.setText(address)
            }
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapScaler(bm: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        val width: Int = bm!!.getWidth()
        val height: Int = bm.getHeight()
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }

    private fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 100
                )
            } else {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPanDataFromDigio()
                //Toast.makeText(activity?.applicationContext, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(
                    this.applicationContext,
                    "camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        }
    }

}



