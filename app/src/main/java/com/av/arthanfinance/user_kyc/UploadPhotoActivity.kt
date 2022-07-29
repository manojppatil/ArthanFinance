package com.av.arthanfinance.user_kyc

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.databinding.ActivityUploadAadharBinding
import com.av.arthanfinance.databinding.ActivityUploadPhotoBinding
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class UploadPhotoActivity : BaseActivity() {
    private lateinit var activityUploadPhotoBinding: ActivityUploadPhotoBinding
    private var kycCompleteStatus = "30"
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQ_CODE_PHOTO_ID = 321
    private var REQ_CODE = 33
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var customerId: String = ""
    private var customerFatherName: String = ""
    private var customerName: String = ""
    private var customerDob: String = ""
    private lateinit var customerGender: String
    private lateinit var userImg: String
    private var customerData: CustomerHomeTabResponse? = null
    override val layoutId: Int
        get() = R.layout.activity_upload_photo

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadPhotoBinding = ActivityUploadPhotoBinding.inflate(layoutInflater)
        setContentView(activityUploadPhotoBinding.root)

        if (intent.hasExtra("fatherName")) {
            customerId = intent.getStringExtra("customerId")!!
            customerFatherName = intent.getStringExtra("fatherName")!!
            customerName = intent.getStringExtra("customerName")!!
            customerDob = intent.getStringExtra("customerDob")!!

        } else {
            val mPrefs: SharedPreferences? =
                getSharedPreferences("customerData", Context.MODE_PRIVATE)
            val gson = Gson()
            val json: String? = mPrefs?.getString("customerData", null)
            if (json != null) {
                val obj: CustomerHomeTabResponse =
                    gson.fromJson(json, CustomerHomeTabResponse::class.java)
                customerData = obj

                customerId = customerData!!.customerId.toString()
            }
        }


        setSupportActionBar(activityUploadPhotoBinding.tbUploadPhoto)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Upload Your Photo"

        activityUploadPhotoBinding.pbKycPhoto.max = 100
        ObjectAnimator.ofInt(activityUploadPhotoBinding.pbKycPhoto, "progress", 30)
            .setDuration(1000).start()
        activityUploadPhotoBinding.tvPercent.text = "${kycCompleteStatus}%"

        activityUploadPhotoBinding.btnUploadPhoto.setOnClickListener {
            /*val intent1 = Intent(this@UploadPhotoActivity, UploadAadharActivity::class.java)
            startActivity(intent1)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)*/
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
        }

        activityUploadPhotoBinding.btnCameraUploadPhoto.setOnClickListener {
            if (requestPermission()) {
                launchCamera(REQ_CODE)
            }
            /*val intent1 = Intent(this@UploadPhotoActivity, UploadAadharActivity::class.java)
            startActivity(intent1)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)*/

        }
    }

    private fun launchCamera(reqCode: Int) {
        startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(
                this@UploadPhotoActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "ArthanFinance"
            )
            // if (!dir.exists())
            dir.mkdirs()
            val time = System.currentTimeMillis().toString()
            putExtra("FilePath", "${dir.absolutePath}/IMG_PHOTO_ID_${time}.jpg")
            putExtra("is_front", false)
        }, REQ_CODE_PHOTO_ID)
    }

    @SuppressLint("ObsoleteSdkInt")
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
                    ), MY_CAMERA_PERMISSION_CODE
                )
            } else {
                return true
            }
        }
        return false
    }

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
                REQUEST_CODE_GALLERY -> {
                    try {
                        val fileUri = data!!.data
                        val intent = CropImage.activity(fileUri)
                            .getIntent(this)
                        startActivityForResult(intent, CROP_REQUEST_CODE_GALLERY)
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                }
                CROP_REQUEST_CODE_CAMERA, CROP_REQUEST_CODE_GALLERY -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadPhotoBinding.photoImgView.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    print("base64 Stirng $encodedImageStr")
                    showProgressDialog()

                    uploadPhotoImage(encodedImageStr)

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadPhotoImage(encodedImageStr: String?) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("customerImage", encodedImageStr)

        ApiClient().getAuthApiService(this).uploadCustomerImage(jsonObject).enqueue(object :
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
                        this@UploadPhotoActivity,
                        "Photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                    val intent1 = Intent(this@UploadPhotoActivity, UploadAadharActivity::class.java)
                    intent1.putExtra("customerId", customerId)
                    intent1.putExtra("fatherName", customerFatherName)
                    intent1.putExtra("customerName", customerName)
                    intent1.putExtra("customerDob", customerDob)

                    startActivity(intent1)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    hideProgressDialog()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadPhotoActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this@UploadPhotoActivity, e.message, Toast.LENGTH_LONG)
                            .show()
                    }
                    Toast.makeText(
                        this@UploadPhotoActivity,
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
                    this@UploadPhotoActivity,
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
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onBackPressed() {
        finish()
    }
}