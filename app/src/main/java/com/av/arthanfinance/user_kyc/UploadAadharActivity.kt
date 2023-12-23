package com.av.arthanfinance.user_kyc

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioKycResponseListener
import `in`.digio.sdk.kyc.DigioSession
import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.BitmapUtils
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.DigilockerDataResponse
import com.av.arthanfinance.applyLoan.model.DigilockerTokenResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityUploadAadharBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.AppLocationProvider
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class UploadAadharActivity : BaseActivity(), DigioKycResponseListener {
    private val AAADHAR_BACK = 103
    private val AAADHAR_FRONT = 104
    private var REQ_CODE_AADHAR_FP = 33
    private var REQ_CODE_AADHAR_BP = 34
    private var CROP_REQ_CODE_AADHAR_FP = 330
    private var CROP_REQ_CODE_AADHAR_BP = 340
    private val CROP_AAADHAR_BACK = 1030
    private val CROP_AAADHAR_FRONT = 1040
    private lateinit var activityUploadAadharBinding: ActivityUploadAadharBinding
    private var afUploadStatus = 0
    private var abUploadStatus = 0
    private var mobNo: String? = null
    private var customerId: String? = null
    private var leadId: String? = null
    private var lat: String? = null
    private var lng: String? = null
    private var kycCompleteStatus = "66"
    override val layoutId: Int
        get() = R.layout.activity_upload_aadhar
    private var customerData: AuthenticationResponse? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadAadharBinding = ActivityUploadAadharBinding.inflate(layoutInflater)
        setContentView(activityUploadAadharBinding.root)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: AuthenticationResponse =
                gson.fromJson(json, AuthenticationResponse::class.java)
            customerData = obj
        }

        setSupportActionBar(activityUploadAadharBinding.tbUploadAdharDetails)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Let's finish your KYC!"

        fetchLocation(1)

        activityUploadAadharBinding.pbKycAdhar.max = 100
        ObjectAnimator.ofInt(activityUploadAadharBinding.pbKycAdhar, "progress", 66)
            .setDuration(1000).start()
        activityUploadAadharBinding.tvPercent.text = "${kycCompleteStatus}%"

        mobNo = mPrefs?.getString("mobNo", null)
        customerId = mPrefs?.getString("customerId", null)
        leadId = mPrefs?.getString("leadId", null)

        activityUploadAadharBinding.aadharFront.tag = 1
        activityUploadAadharBinding.aadharBack.tag = 2

        activityUploadAadharBinding.btnOfflineKyc.setOnClickListener {
            getTokenDataFromDigilocker()
        }

        activityUploadAadharBinding.btnOfflineKyc2.setOnClickListener {
            activityUploadAadharBinding.digilockerLayout.visibility = View.GONE
            activityUploadAadharBinding.arthanLayout.visibility = View.VISIBLE
        }

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

//        activityUploadAadharBinding.imgBack.setOnClickListener {
//            finish()
//        }
    }

    private fun fetchLocation(from: Int) {

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            -> {
                // You can use the API that requires the permission.
                AppLocationProvider().getLocation(
                    this,
                    object : AppLocationProvider.LocationCallBack {
                        override fun locationResult(location: Location?) {

                            lat = location?.latitude.toString()
                            lng = location?.longitude.toString()
                            Log.d("latlng", lng.toString())
                            AppLocationProvider().stopLocation()

                            // use location, this might get called in a different thread if a location is a last known location. In that case, you can post location on main thread
                        }

                    })

            }
            else -> {
                val request = permissionsBuilder(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).build()
                request.listeners {
                    onAccepted {
                        fetchLocation(from)
                    }
                    onDenied {
                    }
                    onPermanentlyDenied {
                    }
                }
                request.send()
            }
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
                    data?.data?.let { uri ->
                        compressImage(uri.toString())
                    }
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
                        clevertapDefaultInstance?.pushEvent("Aadhaar Front captured")//added by CleverTap Assistant
                        uploadAadharImage2(base64, "AF", 1)
                    } else {
                        activityUploadAadharBinding.aadharBack.setImageBitmap(bitmap)
                        activityUploadAadharBinding.llBackAadhar.visibility = View.GONE
                        activityUploadAadharBinding.btnRetakeBack.visibility = View.VISIBLE
                        activityUploadAadharBinding.removeAadharBackPhoto.visibility = View.VISIBLE
                        activityUploadAadharBinding.aadharBack.tag = 4
                        val base64 = BitmapUtils.getBase64(bitmap)
                        abUploadStatus = 1
                        clevertapDefaultInstance?.pushEvent("Aadhaar Back captured")//added by CleverTap Assistant
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
                        clevertapDefaultInstance?.pushEvent("Aadhaar Front selected")//added by CleverTap Assistant
                        uploadAadharImage2(base64, "AF", 1)
                    } else {
                        activityUploadAadharBinding.aadharBack.setImageBitmap(bitmap)
                        abUploadStatus = 1
                        clevertapDefaultInstance?.pushEvent("Aadhaar Back selected")//added by CleverTap Assistant
                        uploadAadharImage2(base64, "AB", 1)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadAadharImage2(encodedImageStr: String?, idType: String, statusFrom: Int) {
        val jsonObject = JsonObject()
        showProgressDialog()

        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("loanId", customerId)
        jsonObject.addProperty("amId", customerId)
        jsonObject.addProperty("leadId", leadId)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("idType", idType)
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")
        clevertapDefaultInstance?.pushEvent("Aadhaar upload service started")//added by CleverTap Assistant

        ApiClient().getAuthApiService(this).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>,
            ) {
                val docResponse = response.body()
                hideProgressDialog()
                if (statusFrom == 1 && idType == "AF") {
                    clevertapDefaultInstance?.pushEvent("Aadhaar front uploaded")//added by CleverTap Assistant
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Aadhaar front photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (statusFrom == 1 && idType == "AB") {
                    clevertapDefaultInstance?.pushEvent("Aadhaar back uploaded")//added by CleverTap Assistant
                    Toast.makeText(
                        this@UploadAadharActivity,
                        "Aadhaar back photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uploadAadharData(statusFrom)
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Aadhaar upload service failure")//added by CleverTap Assistant
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
                response: Response<LoanProcessResponse>,
            ) {
                val docResponse = response.body()
                if (statusFrom == 1) {
                    try {
                        updateStage(ArthanFinConstants.offline_aadhar)

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

    private fun updateStage(stage: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("stage", stage)
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharActivity, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    clevertapDefaultInstance?.pushEvent("Offline aadhaar uploaded")//added by CleverTap Assistant
                    val intent = Intent(
                        this@UploadAadharActivity,
                        UploadBankDetailsActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                    overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                }
            }
        })
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
        aadharId: String,
        requestId: String,
        createdAt: String,
        rawEncodedXml: String,
    ) {

        showProgressDialog()
        val jsonObject = JsonObject()

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
        jsonObject.addProperty("aadharId", aadharId)
        jsonObject.addProperty("requestId", requestId)
        jsonObject.addProperty("createdAt", createdAt)
        clevertapDefaultInstance?.pushEvent("Upload aadhaar service started")//added by CleverTap Assistant
        ApiClient().getAuthApiService(this).uploadCustomerAadhar(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                try {
                    val docResponse = response.body() as AuthenticationResponse
                    val apiCode = docResponse.apiCode

                    if (apiCode.equals("200")) {
                        Toast.makeText(
                            this@UploadAadharActivity,
                            "Aadhaar data uploaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        clevertapDefaultInstance?.pushEvent("Aadhaar upload success")//added by CleverTap Assistant
                        val intent1 = Intent(
                            this@UploadAadharActivity,
                            UploadBankDetailsActivity::class.java
                        )
                        startActivity(intent1)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
                        clevertapDefaultInstance?.pushEvent("Aadhaar upload failure")//added by CleverTap Assistant
                        Toast.makeText(
                            this@UploadAadharActivity,
                            "Aadhaar Details Upload Failed. Please Try After Sometime",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressDialog()
                    }
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                clevertapDefaultInstance?.pushEvent("Aadhaar upload service failure")//added by CleverTap Assistant
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
        clevertapDefaultInstance?.pushEvent("Digio KYC clicked")//added by CleverTap Assistant
        val jsonObject = JsonObject()
        val jsonObject1 = JsonObject()
        val jsonArray = JsonArray()
        val jsonArray1 = JsonArray()
        jsonObject.addProperty("customer_identifier", mobNo)
        jsonObject.addProperty("customer_name", customerData!!.customerId)
        jsonObject.addProperty("notify_customer", false)
        jsonObject.addProperty("generate_access_token", true)
        jsonObject1.addProperty("type", "DIGILOCKER")
        jsonObject1.addProperty("title", "Digilocker KYC")
        jsonObject1.addProperty("description",
            "Please share your aadhaar card and Pan from digilocker")
        val arr = arrayOf("AADHAAR", "PAN")

        for (i in arr.indices) {
            jsonArray1.add(arr[i])
        }
        jsonObject1.add("document_types", jsonArray1)

        jsonArray.add(jsonObject1)
        jsonObject.add("actions", jsonArray)

        Log.e("PAYLOD", jsonObject.toString())

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
                    response: Response<DigilockerTokenResponse>,
                ) {
                    try {
                        val tokenBody = response.body()
                        val accessToken = tokenBody!!.accessToken1
                        val tokenId = accessToken.token
                        val kId = accessToken.kId
                        clevertapDefaultInstance?.pushEvent("Digio token generated")//added by CleverTap Assistant

                        mobNo?.let { getRid(tokenId, kId, it) }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        Log.e("TAG", ex.toString())
                        clevertapDefaultInstance?.pushEvent("User cancel digio journey")//added by CleverTap Assistant
                        activityUploadAadharBinding.digilockerLayout.visibility = View.VISIBLE
                        activityUploadAadharBinding.arthanLayout.visibility = View.GONE

                    }
                }

                override fun onFailure(call: Call<DigilockerTokenResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    clevertapDefaultInstance?.pushEvent("Digio token service failure")//added by CleverTap Assistant
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
            clevertapDefaultInstance?.pushEvent("Digio session started")//added by CleverTap Assistant
            val digioSession = DigioSession()
            digioSession.init(this@UploadAadharActivity, config)
            digioSession.startSession(
                kId,
                mobNo,
                tokenId,
                this
            )
        } catch (e: Exception) {
            clevertapDefaultInstance?.pushEvent("Digio session start failure")//added by CleverTap Assistant
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDigioKycFailure(requestId: String?, response: String?) {
        Toast.makeText(
            this,
            "Unable to fetch data from Digilocker, try again",
            Toast.LENGTH_LONG
        ).show()
        clevertapDefaultInstance?.pushEvent("Digio KYC not fetched")//added by CleverTap Assistant
        activityUploadAadharBinding.btnOfflineKyc2.isEnabled = false
        activityUploadAadharBinding.btnOfflineKyc2.setBackgroundResource(R.drawable.bg_register_rect_orange)
        activityUploadAadharBinding.btnOfflineKyc.setBackgroundResource(R.drawable.bg_register_rect_orange)
        activityUploadAadharBinding.constraintLayout6.visibility = View.GONE
        activityUploadAadharBinding.constraintLayout4.visibility = View.VISIBLE
        activityUploadAadharBinding.constraintLayout6.setBackgroundResource(R.drawable.bg_white_rect)
        activityUploadAadharBinding.constraintLayout4.setBackgroundResource(R.drawable.bg_white_rect)
    }

    override fun onDigioKycSuccess(requestId: String?, response: String?) {
        clevertapDefaultInstance?.pushEvent("Digio KYC fetched")//added by CleverTap Assistant
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
                    response: Response<DigilockerDataResponse>,
                ) {
                    val digilockerResponse = response.body() as DigilockerDataResponse
                    Log.e("TAGRES", digilockerResponse.toString())
                    val dataObject = digilockerResponse.actions[0]
                    val requestId = digilockerResponse.id
                    val created_at = digilockerResponse.created_at
                    val name = dataObject.details.aadhaarDetails.name
                    val gender = dataObject.details.aadhaarDetails.gender
                    val dob = dataObject.details.aadhaarDetails.dob
                    val image = dataObject.details.aadhaarDetails.image
                    val addressDetails = dataObject.details.aadhaarDetails.aadhaarAddressDetails
                    val fullAddress = addressDetails.address
                    val aadharid = dataObject.details.aadhaarDetails.id_number

                    val parts: List<String> = fullAddress!!.split(",")
                    val addressLine = parts[0]
                    val addressLine0 = parts[1]

                    val addressLine1 = fullAddress
//                    val addressLine1 = "$addressLine , $addressLine0"
                    val addressLine2 = addressDetails.localityPostOffice
                    val addressLine3 = addressDetails.districtCity
                    val state = addressDetails.state
                    val pincode = addressDetails.pincode
                    clevertapDefaultInstance?.pushEvent("Aadhaar data generated")//added by CleverTap Assistant
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
                        aadharid!!,
                        requestId!!,
                        created_at!!,
                        ""
                    )
                    showProgressDialog()
                }
            })

    }

    private fun compressImage(imageUri: String): String {
        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp: Bitmap = BitmapFactory.decodeFile(filePath, options)
        var actualHeight: Int = options.outHeight
        var actualWidth: Int = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270F)
                Log.d("EXIF", "Exif: $orientation")
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        val filename = getFilename()
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
    }

    private fun getFilename(): String {
        val file = File(
            Environment.getExternalStorageDirectory().path,
            "MyFolder/Images"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
    }

    @SuppressLint("Recycle")
    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor: Cursor? = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}