package com.av.arthanfinance.user_kyc

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioStateLessSession
import `in`.digio.sdk.kyc.DigioTaskResponse
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.LoanProcessResponse
import com.av.arthanfinance.applyLoan.UserKYCDetailsResponse
import com.av.arthanfinance.databinding.ActivityUploadPanBinding
import com.av.arthanfinance.networkService.ApiClient
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_loan_details.*
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.ArrayList

class UploadPanActivity : BaseActivity(), DigioResponseListener {
    private lateinit var uploadPanBinding: ActivityUploadPanBinding
    private var kycCompleteStatus = "20"
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var customerData: CustomerHomeTabResponse? = null
    private var loanResponse: LoanProcessResponse? = null
    private var panImg: String = ""
    private var userImg: String = ""
    private var REQ_CODE_PHOTO_ID = 321
    private var REQ_CODE = 33
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var customerId: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadPanBinding = ActivityUploadPanBinding.inflate(layoutInflater)
        setContentView(uploadPanBinding.root)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)


        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj

            customerId = customerData!!.customerId.toString()
        }

        //getPreUploadPan()
        setSupportActionBar(uploadPanBinding.tbUploadPan)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Upload Pan Details"

        uploadPanBinding.pbKyc.max = 100
        ObjectAnimator.ofInt(uploadPanBinding.pbKyc, "progress", 20).setDuration(1000).start()
        uploadPanBinding.tvPercent.text = "${kycCompleteStatus}%"

        uploadPanBinding.btnCaptureDigioPan.setOnClickListener {
            /* val intent1 = Intent(this@UploadPanActivity, UploadPanActivity::class.java)
             startActivity(intent1)
             overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)*/
            if (requestPermission()) {
                getPanDataFromDigio()
            }
        }

        uploadPanBinding.btnSendDigioPan.setOnClickListener {
            launchCamera(REQ_CODE)
        }
    }

    private fun launchCamera(reqCode: Int) {
        startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(
                this@UploadPanActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "ArthurFinance"
            )
            // if (!dir.exists())
            dir.mkdirs()
            val time = System.currentTimeMillis().toString()
            putExtra("FilePath", "${dir.absolutePath}/IMG_PHOTO_ID_${time}.jpg")
            putExtra("is_front", true)
        }, REQ_CODE_PHOTO_ID)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_PHOTO_ID -> {
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri).getIntent(this)
                    startActivityForResult(intent, CROP_REQUEST_CODE_CAMERA)
                }

                CROP_REQUEST_CODE_CAMERA -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    uploadPanBinding.digioPanImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    print("base64 Stirng $encodedImageStr")
                    showProgressDialog()
                    verifyPanImage(encodedImageStr)


                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun verifyPanImage(encodedImageStr: String?) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("imageBase64", encodedImageStr)
        jsonObject.addProperty("idType", "PAN")
        jsonObject.addProperty("userId", customerId)
        jsonObject.addProperty("loanId", "")

        Log.e("TAG", jsonObject.toString())
        ApiClient().getAuthApiService(this).verifyCustomerPan(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {

                val docResponse = response.body() as AuthenticationResponse
                val apiCode = docResponse.apiCode
                //val message = docResponse.message

                if (apiCode.equals("200")) {
                    Toast.makeText(
                        this@UploadPanActivity,
                        "PAN uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                    val intent1 = Intent(this@UploadPanActivity, UploadPhotoActivity::class.java)
                    intent1.putExtra("customerId", customerId)
                    intent1.putExtra("fatherName", "")
                    intent1.putExtra("customerName", customerData!!.customerName)
                    intent1.putExtra("customerDob", "")
                    startActivity(intent1)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    hideProgressDialog()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadPanActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@UploadPanActivity, e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Toast.makeText(
                        this@UploadPanActivity,
                        "PAN Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadPanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })

    }

    private fun getPreUploadPan() {
        showProgressDialog()
        val customerId = customerData!!.customerId.toString()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)

        ApiClient().getAuthApiService(this).getCustomerKYCDetails(jsonObject).enqueue(object :
            Callback<UserKYCDetailsResponse> {
            override fun onResponse(
                call: Call<UserKYCDetailsResponse>,
                response: Response<UserKYCDetailsResponse>
            ) {
                hideProgressDialog()
                val docResponse = response.body() as UserKYCDetailsResponse
                //val message = docResponse.message

                if (response.body() != null) {

                    hideProgressDialog()
                    panImg = docResponse.panUrl!!
                    userImg = docResponse.applicantImgUrl!!
                    if (!panImg.equals("")) {
                        uploadPanBinding.btnSendDigioPan.visibility = View.VISIBLE
                    } else {
                        uploadPanBinding.btnSendDigioPan.visibility = View.GONE
                    }

                    val intent1 = Intent(this@UploadPanActivity, UploadPanActivity::class.java)
                    intent1.putExtra("panImage", panImg)
                    intent1.putExtra("userImage", userImg)
                    startActivity(intent1)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadPanActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@UploadPanActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                    Toast.makeText(
                        this@UploadPanActivity,
                        "PAN Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }

            }

            override fun onFailure(call: Call<UserKYCDetailsResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadPanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    override val layoutId: Int
        get() = R.layout.activity_upload_pan

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

    override fun onDigioEventTracker(eventTracker: JSONObject) {

    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
        for (digiTaskResponse in failure) {
            val digioTaskRequest = digiTaskResponse.getResponse()
            val msg = digioTaskRequest.getString("message")
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {

            val digioTaskRequest = digiTaskResponse.getTask()

            val taskType = digioTaskRequest.taskType
            if (taskType == DigioTaskType.ID_ANALYSIS) {
                val mainResponse = digiTaskResponse.getResponse()
                val analysisResponse = mainResponse.getJSONObject("analysis_response")
                val customerName = analysisResponse.getString("name")
                val customerFatherName = analysisResponse.getString("fathers_name")
                val panNo = analysisResponse.getString("id_no")
                val customerDob = analysisResponse.getString("dob")
                val panImg = mainResponse.getString("id_front_uri")

                val sharedPref: SharedPreferences =
                    getSharedPreferences("father_name", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("father_name", customerFatherName)
                editor.apply()

                Glide.with(this)
                    .asBitmap()
                    .load(panImg)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            digioPanImage.setImageBitmap(resource)
                            val encodedImageStr = encodeImageString(resource)
                            uploadPanImage(
                                encodedImageStr,
                                customerName,
                                customerFatherName,
                                panNo,
                                customerDob
                            )
                            showProgressDialog()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }

        }
    }

    private fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), MY_CAMERA_PERMISSION_CODE
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
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPanDataFromDigio()
            } else {
                Toast.makeText(
                    this,
                    "camera permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun uploadPanImage(
        encodedImageStr: String?,
        customerName: String,
        customerFatherName: String,
        panNo: String,
        customerDob: String
    ) {

        val jsonObject = JsonObject()

        var customerId = ""
        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
            customerId = intent.getStringExtra("coCustomerId").toString()

            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "CA")
            jsonObject.addProperty("panNo", panNo)
            jsonObject.addProperty("panImage", encodedImageStr)
            jsonObject.addProperty("customerName", customerName)
            jsonObject.addProperty("customerDob", customerDob)
            jsonObject.addProperty("fatherName", customerFatherName)

        } else {
            customerId = customerData!!.customerId.toString()

            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "PA")
            jsonObject.addProperty("panNo", panNo)
            jsonObject.addProperty("panImage", encodedImageStr)
            jsonObject.addProperty("customerName", customerName)
            jsonObject.addProperty("customerDob", customerDob)
            jsonObject.addProperty("fatherName", customerFatherName)
        }


        ApiClient().getAuthApiService(this).uploadCustomerPan(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val docResponse = response.body() as AuthenticationResponse
                val apiCode = docResponse.apiCode
                //val message = docResponse.message

                if (apiCode.equals("200")) {
                    Toast.makeText(
                        this@UploadPanActivity,
                        "Pan data uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (intent.hasExtra("loanResponse")) {
                        val intent1 =
                            Intent(this@UploadPanActivity, UploadAadharActivity::class.java)
                        intent1.putExtra("customerId", customerId)
                        intent1.putExtra("fatherName", customerFatherName)
                        intent1.putExtra("customerName", customerName)
                        intent1.putExtra("customerDob", customerDob)
                        intent1.putExtra("customerGender", customerFatherName)
                        intent1.putExtra("loanResponse", loanResponse)
                        startActivity(intent1)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        val intent1 =
                            Intent(this@UploadPanActivity, UploadPhotoActivity::class.java)
                        intent1.putExtra("customerId", customerId)
                        intent1.putExtra("fatherName", customerFatherName)
                        intent1.putExtra("customerName", customerName)
                        intent1.putExtra("customerDob", customerDob)
                        intent1.putExtra("customerGender", customerFatherName)
                        startActivity(intent1)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }


                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadPanActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@UploadPanActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                    Toast.makeText(
                        this@UploadPanActivity,
                        "PAN Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }

            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadPanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }


    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    override fun onBackPressed() {
        finish()
    }
}