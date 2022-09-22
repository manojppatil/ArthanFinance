package com.av.arthanfinance.user_kyc

import `in`.digio.sdk.kyc.*
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.applyLoan.model.DigilockerDataResponse
import com.av.arthanfinance.applyLoan.model.DigilockerTokenResponse
import com.av.arthanfinance.databinding.ActivityUploadAadharBinding
import com.av.arthanfinance.manager.DataManager
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_upload_aadhar.*
import kotlinx.android.synthetic.main.layout_adhar.*
import kotlinx.android.synthetic.main.layout_adhar.arthanLayout
import kotlinx.android.synthetic.main.layout_adhar.digioLayout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception

class UploadAadharActivity : BaseActivity(), DigioResponseListener, DigioKycResponseListener {
    private val AAADHAR_BACK = 103
    private val AAADHAR_FRONT = 104
    private var REQ_CODE_AADHAR_FP = 33
    private var REQ_CODE_AADHAR_BP = 34
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var CROP_REQ_CODE_AADHAR_FP = 330
    private var CROP_REQ_CODE_AADHAR_BP = 340
    private val CROP_AAADHAR_BACK = 1030
    private val CROP_AAADHAR_FRONT = 1040
    private var loanResponse: LoanProcessResponse? = null
    private lateinit var activityUploadAadharBinding: ActivityUploadAadharBinding
    private var kycCompleteStatus = "30"
    private var afUploadStatus = 0
    private var abUploadStatus = 0
    private var gender = ""
    private lateinit var customerId: String
    private lateinit var customerName: String
    private lateinit var customerDob: String
    private var customerGender = ""
    private lateinit var customerFatherName: String
    override val layoutId: Int
        get() = R.layout.activity_upload_aadhar
    private var customerData: CustomerHomeTabResponse? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadAadharBinding = ActivityUploadAadharBinding.inflate(layoutInflater)
        setContentView(activityUploadAadharBinding.root)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
        }

        activityUploadAadharBinding.aadharFront.tag = 1
        activityUploadAadharBinding.aadharBack.tag = 2

        if (intent.hasExtra("loanResponse")) {
            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
            customerId = intent.getStringExtra("customerId")!!
            customerFatherName = intent.getStringExtra("fatherName")!!
            customerName = intent.getStringExtra("customerName")!!
            customerDob = intent.getStringExtra("customerDob")!!
        } else {
            if (intent.hasExtra("customerId")) {
                customerId = intent.getStringExtra("customerId")!!
                customerFatherName = intent.getStringExtra("fatherName")!!
                customerName = intent.getStringExtra("customerName")!!
                customerDob = intent.getStringExtra("customerDob")!!
            } else {
                customerId = customerData!!.customerId!!
                val sharedPref: SharedPreferences =
                    getSharedPreferences("father_name", Context.MODE_PRIVATE)
                customerFatherName = sharedPref.getString("father_name", "").toString()
                customerName = customerData!!.customerName.toString()
                customerDob = customerData!!.dob.toString()
                customerGender = customerData!!.customerName.toString()
            }
        }

        setSupportActionBar(activityUploadAadharBinding.tbUploadAadhar)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Upload Aadhar Details"

        activityUploadAadharBinding.pbKycAadhar.max = 100
        ObjectAnimator.ofInt(activityUploadAadharBinding.pbKycAadhar, "progress", 30)
            .setDuration(1000).start()
        activityUploadAadharBinding.tvPercent.text = "${kycCompleteStatus}%"

        activityUploadAadharBinding.btnOfflineKyc.setOnClickListener {
            getTokenDataFromDigilocker()
            //getAadharDataFromDigio()
        }

        /////////////////////////////////////////////

        activityUploadAadharBinding.btnNextAadhar.setOnClickListener {
            if (afUploadStatus == 0) {
                Toast.makeText(this, "Please upload Aadhar front photo", Toast.LENGTH_SHORT).show()
            } else if (abUploadStatus == 0) {
                Toast.makeText(this, "Please upload Aadhar back photo", Toast.LENGTH_SHORT).show()
            } else {
                uploadAadharData(1)
            }

        }

        activityUploadAadharBinding.btnAadharFrontPhotoCamera.setOnClickListener {
            startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_FP)
                val dir = File(
                    this@UploadAadharActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "AB"
                )
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_FRONT.jpg")
                putExtra("is_front", true)
            }, REQ_CODE_AADHAR_FP)
        }

        activityUploadAadharBinding.btnAadharBackPhotoCamera.setOnClickListener {
            startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_BP)
                val dir = File(
                    this@UploadAadharActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "AB"
                )
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_BACK.jpg")
                putExtra("is_front", true)
            }, REQ_CODE_AADHAR_BP)
        }

        activityUploadAadharBinding.btnAadharFrontPhotoUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), AAADHAR_FRONT)
        }

        activityUploadAadharBinding.btnAadharBackPhotoUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), AAADHAR_BACK)
        }

        activityUploadAadharBinding.btnRetakeFront.setOnClickListener {
            startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_FP)
                val dir = File(
                    this@UploadAadharActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "AB"
                )
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_FRONT.jpg")
            }, REQ_CODE_AADHAR_FP)
        }

        activityUploadAadharBinding.btnRetakeBack.setOnClickListener {
            startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_BP)
                val dir = File(
                    this@UploadAadharActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "AB"
                )
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_BACK.jpg")
            }, REQ_CODE_AADHAR_BP)
        }

        activityUploadAadharBinding.removeAadharFrontPhoto.setOnClickListener {
            activityUploadAadharBinding.aadharFront.setImageDrawable(this.applicationContext?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, // Context
                    R.drawable.ic_adhar_pic // Drawable
                )
            })

            activityUploadAadharBinding.llFrontAadhar.visibility = View.VISIBLE
            activityUploadAadharBinding.btnRetakeFront.visibility = View.GONE
            activityUploadAadharBinding.removeAadharFrontPhoto.visibility = View.GONE
            activityUploadAadharBinding.aadharFront.tag = 1

        }

        activityUploadAadharBinding.removeAadharBackPhoto.setOnClickListener {
            activityUploadAadharBinding.aadharBack.setImageDrawable(this.applicationContext?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, // Context
                    R.drawable.ic_adhar_pic // Drawable
                )
            })

            activityUploadAadharBinding.llBackAadhar.visibility = View.VISIBLE
            activityUploadAadharBinding.btnRetakeBack.visibility = View.GONE
            activityUploadAadharBinding.removeAadharBackPhoto.visibility = View.GONE
            activityUploadAadharBinding.aadharBack.tag = 2
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_AADHAR_FP, REQ_CODE_AADHAR_BP -> {
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri)
                        .getIntent(this)
                    if (requestCode == REQ_CODE_AADHAR_FP)
                        startActivityForResult(intent, CROP_REQ_CODE_AADHAR_FP)
                    else
                        startActivityForResult(intent, CROP_REQ_CODE_AADHAR_BP)
                }

                AAADHAR_FRONT, AAADHAR_BACK -> {
                    try {
                        val fileUri = data!!.data
                        val intent = CropImage.activity(fileUri)
                            .getIntent(this)
                        if (requestCode == AAADHAR_FRONT)
                            startActivityForResult(intent, CROP_AAADHAR_FRONT)
                        else
                            startActivityForResult(intent, CROP_AAADHAR_BACK)
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                }

                CROP_REQ_CODE_AADHAR_FP, CROP_REQ_CODE_AADHAR_BP -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    if (requestCode == CROP_REQ_CODE_AADHAR_FP) {
                        activityUploadAadharBinding.aadharFront.setImageBitmap(bitmap)
                        activityUploadAadharBinding.llFrontAadhar.visibility = View.GONE
                        activityUploadAadharBinding.btnRetakeFront.visibility = View.VISIBLE
                        activityUploadAadharBinding.removeAadharFrontPhoto.visibility = View.VISIBLE
                        activityUploadAadharBinding.aadharFront.tag = 3
                        val base64 = BitmapUtils.getBase64(bitmap)
                        afUploadStatus = 1
                        uploadAadharImage2(base64, "AF", 1)
                    } else {
                        activityUploadAadharBinding.aadharBack.setImageBitmap(bitmap)
                        activityUploadAadharBinding.llBackAadhar.visibility = View.GONE
                        activityUploadAadharBinding.btnRetakeBack.visibility = View.VISIBLE
                        activityUploadAadharBinding.removeAadharBackPhoto.visibility = View.VISIBLE
                        activityUploadAadharBinding.aadharBack.tag = 4
                        val base64 = BitmapUtils.getBase64(bitmap)
                        abUploadStatus = 1
                        uploadAadharImage2(base64, "AB", 1)
                    }
                }
                CROP_AAADHAR_FRONT, CROP_AAADHAR_BACK -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    val base64 = BitmapUtils.getBase64(bitmap)

                    if (requestCode == CROP_AAADHAR_FRONT) {
                        activityUploadAadharBinding.aadharFront.setImageBitmap(bitmap)
                        afUploadStatus = 1
                        uploadAadharImage2(base64, "AF", 1)
                    } else {
                        activityUploadAadharBinding.aadharBack.setImageBitmap(bitmap)
                        abUploadStatus = 1
                        uploadAadharImage2(base64, "AB", 1)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadAadharImage2(encodedImageStr: String?, idType: String, statusFrom: Int) {

        val jsonObject = JsonObject()

        if (intent.hasExtra("loanResponse")) {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "CA")
            jsonObject.addProperty("idType", idType)
            jsonObject.addProperty("imageBase64", encodedImageStr!!)
            jsonObject.addProperty("applicationType", "CUSTOMER")
        } else {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "PA")
            jsonObject.addProperty("idType", idType)
            jsonObject.addProperty("imageBase64", encodedImageStr!!)
            jsonObject.addProperty("applicationType", "CUSTOMER")
        }


        ApiClient().getAuthApiService(this).verifyKYCDocs2(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val docResponse = response.body()
                hideProgressDialog()
                if (statusFrom == 1 && idType.equals("AF")) {
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Aadhar front photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (statusFrom == 1 && idType.equals("AB")) {
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Aadhar back photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uploadAadharData(statusFrom)
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun uploadAadharData(statusFrom: Int) {
        val jsonObject = JsonObject()

        if (intent.hasExtra("loanResponse")) {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "CA")
        } else {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "PA")
        }
        Log.e("TAG", jsonObject.toString())

        ApiClient().getAuthApiService(this).updateAadhar2(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val docResponse = response.body()
                if (statusFrom == 1) {
                    try {
                        val addressLine1 = docResponse?.addressLine1
                        /*val address2 = docResponse?.addressLine2
                        val address3 = docResponse?.addressLine3
                        val district = docResponse?.city*/
                        val state = docResponse?.state
                        val pinCode = docResponse?.pincode

                        val parts: List<String> = addressLine1!!.split(",")
                        val addr0 = parts[0]
                        val addr1 = parts[1]
                        val address1 = "$addr0, $addr1"

                        val addr2 = parts[2]
                        val addr3 = parts[3]
                        val address2 = "$addr2, $addr3"

                        val addr4 = parts[4]
                        val addr5 = parts[5]
                        val address3 = "$addr4, $addr5"

                        if (intent.hasExtra("loanResponse")) {
                            val intent1 =
                                Intent(this@UploadAadharActivity, ApplyForLoanActivity::class.java)
                            intent1.putExtra("loanResponse", loanResponse)
                            startActivity(intent1)
                            finish()
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        } else {
                            val intent1 = Intent(
                                this@UploadAadharActivity,
                                UploadAadharAddressActivity::class.java
                            )
                                .putExtra("name", customerName)
                                .putExtra("fatherName", customerFatherName)
                                .putExtra("gender", gender)
                                .putExtra("dob", customerDob)
                                .putExtra("image1", "")
                                .putExtra("addressLine1", address1)
                                .putExtra("addressLine2", address2)
                                .putExtra("addressLine3", address3)
                                .putExtra("city", "")
                                .putExtra("state", state)
                                .putExtra("pincode", pinCode)
                            startActivity(intent1)
                            finish()
                            overridePendingTransition(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                            )
                        }

                        /*val intent1 =
                            Intent(this@UploadAadharActivity, UploadAadharAddressActivity::class.java)
                                .putExtra("name", customerName)
                                .putExtra("fatherName", customerFatherName)
                                .putExtra("gender", customerGender)
                                .putExtra("dob", customerDob)
                                .putExtra("image1", "")
                                .putExtra("addressLine1", address1)
                                .putExtra("addressLine2", address2)
                                .putExtra("addressLine3", address3)
                                .putExtra("city", "")
                                .putExtra("state", state)
                                .putExtra("pincode", pinCode)
                        startActivity(intent1)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)*/
                    } catch (ex: NullPointerException) {
                        Log.e("TAG", ex.toString())
                    }
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharActivity,
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
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onDigioEventTracker(eventTracker: JSONObject) {

    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
        for (digiTaskResponse in failure) {
            val errorCode = digiTaskResponse.getResponse().get("responseCode")
            val msg = digiTaskResponse.getResponse().get("message")
            digilockerLayout.visibility = View.GONE
            arthanLayout.visibility = View.VISIBLE
            Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
            /*if (errorCode == 10012) {
                digioLayout.visibility = View.GONE
                arthanLayout.visibility = View.VISIBLE
                Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
            }*/
        }
    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {

            val digioTaskRequest = digiTaskResponse.getTask()

            val taskType = digioTaskRequest.taskType
            if (taskType == DigioTaskType.OFFLINE_KYC) {
                val mainResponse =
                    digiTaskResponse.getResponse()// offline_kyc or idCard analysis response
                val analysisResponse = mainResponse.getJSONObject("offline_kyc_response")
                val aadharImage = analysisResponse.getString("photo")
                val rawEncodedXml = analysisResponse.getString("raw_encoded_xml")
                val personalInformation = analysisResponse.getJSONObject("personal_information")
                val addressInformation = analysisResponse.getJSONObject("address_information")

                val dob = personalInformation.getString("date_of_birth")
                val gender = personalInformation.getString("gender")
                val name = personalInformation.getString("name")

                val careOf = addressInformation.getString("care_of")
                val house = addressInformation.getString("house")
                val landmark = addressInformation.getString("landmark")
                val street = addressInformation.getString("street")
                val location = addressInformation.getString("location")
                val postOffice = addressInformation.getString("post_office")

                var address1 = ""
                var address2 = ""
                if (!careOf.equals("") && !house.equals("") && !landmark.equals("")) {
                    address1 = "$careOf, $house, $landmark"
                } else if (careOf.equals("") && house.equals("")) {
                    address1 = landmark
                } else if (house.equals("") && landmark.equals("")) {
                    address1 = careOf
                } else if (careOf.equals("") && landmark.equals("")) {
                    address1 = house
                } else if (careOf.equals("")) {
                    address1 = "$house, $landmark"
                } else if (house.equals("")) {
                    address1 = "$careOf, $landmark"
                } else if (landmark.equals("")) {
                    address1 = "$careOf, $house"
                } else {
                    address1 = "$careOf, $house, $landmark"
                }

                if (!location.equals("") && !street.equals("") && !postOffice.equals("")) {
                    address2 = "$location, $street, $postOffice"
                } else if (location.equals("") && street.equals("")) {
                    address2 = postOffice
                } else if (street.equals("") && postOffice.equals("")) {
                    address2 = location
                } else if (location.equals("") && postOffice.equals("")) {
                    address2 = street
                } else if (location.equals("")) {
                    address2 = "$street, $postOffice"
                } else if (street.equals("")) {
                    address2 = "$location, $postOffice"
                } else if (postOffice.equals("")) {
                    address2 = "$location, $street"
                } else {
                    address2 = "$location, $street, $postOffice"
                }

                val district = addressInformation.getString("district")
                val state = addressInformation.getString("state")
                val pincode = addressInformation.getString("postal_code")

                uploadAadharImage(
                    aadharImage,
                    name,
                    gender,
                    dob,
                    address1,
                    address2,
                    district,
                    pincode,
                    state,
                    rawEncodedXml
                )
                showProgressDialog()
            }
        }
    }

    private fun uploadAadharImage(
        aadharImage: String,
        name: String,
        gender: String,
        dob: String,
        address1: String,
        address2: String,
        district: String,
        pinCode: String,
        state: String,
        rawEncodedXml: String
    ) {
        val jsonObject = JsonObject()

        if (intent.hasExtra("loanResponse")) {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "CA")
            jsonObject.addProperty("customerName", name)
            jsonObject.addProperty("customerGender", gender)
            jsonObject.addProperty("customerDob", dob)
            jsonObject.addProperty("customerPhoto", aadharImage)
            jsonObject.addProperty("rawEncodedXml", rawEncodedXml)
            jsonObject.addProperty("addressLine1", address1)
            jsonObject.addProperty("addressLine2", address2)
            jsonObject.addProperty("addressLine3", district)
            jsonObject.addProperty("pincode", pinCode)
            jsonObject.addProperty("state", state)
        } else {
            jsonObject.addProperty("customerId", customerId)
            jsonObject.addProperty("applicantType", "PA")
            jsonObject.addProperty("customerName", name)
            jsonObject.addProperty("customerGender", gender)
            jsonObject.addProperty("customerDob", dob)
            jsonObject.addProperty("customerPhoto", aadharImage)
            jsonObject.addProperty("rawEncodedXml", rawEncodedXml)
            jsonObject.addProperty("addressLine1", address1)
            jsonObject.addProperty("addressLine2", address2)
            jsonObject.addProperty("addressLine3", district)
            jsonObject.addProperty("pincode", pinCode)
            jsonObject.addProperty("state", state)
        }


        ApiClient().getAuthApiService(this).uploadCustomerAadhar(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val docResponse = response.body() as AuthenticationResponse
                val apiCode = docResponse.apiCode

                if (apiCode.equals("200")) {
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Aadhar data uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (intent.hasExtra("loanResponse")) {
                        val intent1 =
                            Intent(this@UploadAadharActivity, ApplyForLoanActivity::class.java)
                        intent1.putExtra("loanResponse", loanResponse)
                        startActivity(intent1)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        val intent1 = Intent(
                            this@UploadAadharActivity,
                            UploadAadharAddressActivity::class.java
                        )
                            .putExtra("name", name)
                            .putExtra("fatherName", customerFatherName)
                            .putExtra("gender", gender)
                            .putExtra("dob", dob)
                            .putExtra("image1", aadharImage)
                            .putExtra("addressLine1", address1)
                            .putExtra("addressLine2", address2)
                            .putExtra("addressLine3", district)
                            .putExtra("city", district)
                            .putExtra("state", state)
                            .putExtra("pincode", pinCode)
                        startActivity(intent1)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }


                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadAadharActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@UploadAadharActivity, e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "PAN Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    override fun onBackPressed() {
        finish()
    }

    private fun getTokenDataFromDigilocker() {
        val jsonObject = JsonObject()
        val jsonObject1 = JsonObject()
        val jsonArray = JsonArray()
        val jsonArray1 = JsonArray()
        jsonObject.addProperty("customer_identifier", customerData!!.mobNo)
        //jsonObject.addProperty("customer_identifier","9004078995")
        jsonObject.addProperty("customer_name", customerData!!.customerName)
        jsonObject.addProperty("notify_customer", false)
        jsonObject.addProperty("generate_access_token", true)
        jsonObject1.addProperty("type", "DIGILOCKER")
        jsonObject1.addProperty("title", "Digilocker KYC")
        jsonObject1.addProperty("description", "Please share your Aadhar from digilocker")
        jsonArray1.add("AADHAAR")
        jsonObject1.add("document_types", jsonArray1)

        jsonArray.add(jsonObject1)
        jsonObject.add("actions", jsonArray)

        //SANDBOX CREDS
//        val clientId = "AI52KOUVC2PQTONW1ZKB92RU22UL8491"
//        val clientSecret = "B6DXG4SV4YJJC2VDA54WTLY6CTJKEUZH"

        //PRD CREDS
        val clientId = "AIZ1SHB77YJBZ6HFAGYR4BTUI84A6DOF"
        val clientSecret = "ZLYKT9FT7UUAIZGVVUPIWSFN3N62Y99O"

        val base = "$clientId:$clientSecret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getBankDetailsApiService(this).createRequestDigilocker(authHeader, jsonObject)
            .enqueue(object : Callback<DigilockerTokenResponse> {
                override fun onResponse(
                    call: Call<DigilockerTokenResponse>,
                    response: Response<DigilockerTokenResponse>
                ) {
                    try {
                        val tokenBody = response.body()
                        val accessToken = tokenBody!!.accessToken1
                        val tokenId = accessToken.token
                        val kId = accessToken.kId

                        customerData!!.mobNo?.let { getRid(tokenId, kId, it) }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e("TAG", ex.toString())
                        digilockerLayout.visibility = View.GONE
                        arthanLayout.visibility = View.VISIBLE

                    }
                }

                override fun onFailure(call: Call<DigilockerTokenResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getRid(tokenId: String, kId: String, mobNo: String) {
        try {
            val config = DigioKycConfig()
            config.setEnvironment(DigioEnvironment.PRODUCTION)
            config.setLogo("https://arthan.finance/assets/images/logo-blue.png")
            val digioSession = DigioSession()
            digioSession.init(this@UploadAadharActivity, config)
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

    override fun onDigioKycFailure(requestId: String?, response: String?) {
        /*Toast.makeText(
            this,
            "Unable to fetch data from Digilocker, try offline KYC",
            Toast.LENGTH_SHORT
        ).show()
        getAadharDataFromDigio()*/

//        digioLayout.visibility = View.GONE
        digilockerLayout.visibility = View.GONE
        arthanLayout.visibility = View.VISIBLE
        Toast.makeText(
            this,
            "KYC through Digilocker failed. Upload your aadhar photos to complete the KYC",
            Toast.LENGTH_SHORT
        ).show()
        /*if (errorCode == 10012) {
            digioLayout.visibility = View.GONE
            arthanLayout.visibility = View.VISIBLE
            Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show()
        }*/

    }

    override fun onDigioKycSuccess(requestId: String?, response: String?) {
        getAadharDataFromDigilocker(requestId)
    }

    private fun getAadharDataFromDigilocker(requestId: String?) {

        //SANDBOX CREDS
//        val clientId = "AI52KOUVC2PQTONW1ZKB92RU22UL8491"
//        val clientSecret = "B6DXG4SV4YJJC2VDA54WTLY6CTJKEUZH"

        //PRD CREDS
        val clientId = "AIZ1SHB77YJBZ6HFAGYR4BTUI84A6DOF"
        val clientSecret = "ZLYKT9FT7UUAIZGVVUPIWSFN3N62Y99O"

        val base = "$clientId:$clientSecret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getDigilockerApiService(this, requestId!!).getDataFromDigiLocker(authHeader)
            .enqueue(object :
                Callback<DigilockerDataResponse> {
                override fun onFailure(call: Call<DigilockerDataResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                }

                override fun onResponse(
                    call: Call<DigilockerDataResponse>,
                    response: Response<DigilockerDataResponse>
                ) {
                    val digilockerResponse = response.body() as DigilockerDataResponse

                    val dataObject = digilockerResponse.actions[0]
                    val name = dataObject.details.aadhaarDetails.name
                    val gender = dataObject.details.aadhaarDetails.gender
                    val dob = dataObject.details.aadhaarDetails.dob
                    val image = dataObject.details.aadhaarDetails.image
                    val addressDetails = dataObject.details.aadhaarDetails.aadhaarAddressDetails
                    val fullAddress = addressDetails.address

                    val parts: List<String> = fullAddress!!.split(",")
                    val addressLine = parts[0]
                    val addressLine0 = parts[1]

                    val addressLine1 = "$addressLine , $addressLine0"
                    val addressLine2 = addressDetails.localityPostOffice
                    val addressLine3 = addressDetails.districtCity
                    val state = addressDetails.state
                    val pincode = addressDetails.pincode

                    uploadAadharImage(
                        image!!,
                        name!!,
                        gender!!,
                        dob!!,
                        addressLine1,
                        addressLine2!!,
                        addressLine3!!,
                        pincode!!,
                        state!!,
                        ""
                    )
                    showProgressDialog()
                }
            })

    }
}