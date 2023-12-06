package com.av.arthanfinance.user_kyc

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.databinding.ActivityUploadPanBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.AppLocationProvider
import com.clevertap.android.sdk.CleverTapAPI
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class UploadPanActivity : BaseActivity() {
    private lateinit var uploadPanBinding: ActivityUploadPanBinding
    private var kycCompleteStatus = "33"
    private var customerData: AuthenticationResponse? = null
    private var REQ_CODE_PHOTO_ID = 321
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var mCustomerId: String? = null
    private var leadId: String? = null
    private var lat: String? = null
    private var lng: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.activity_upload_pan

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uploadPanBinding = ActivityUploadPanBinding.inflate(layoutInflater)
        setContentView(uploadPanBinding.root)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
//        Log.e("registerTime3", intent.getStringExtra("registerTime")!!)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: AuthenticationResponse =
                gson.fromJson(json, AuthenticationResponse::class.java)
            customerData = obj
        }
        mCustomerId = mPrefs?.getString("customerId", null)
        leadId = mPrefs?.getString("leadId", null)

        setSupportActionBar(uploadPanBinding.tbUploadPan)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Let's get you onboard faster!"

        fetchLocation(1)

        uploadPanBinding.pbKyc.max = 100
        ObjectAnimator.ofInt(uploadPanBinding.pbKyc, "progress", 33).setDuration(1000).start()
        uploadPanBinding.tvPercent.text = "${kycCompleteStatus}%"

//        uploadPanBinding.btnCaptureDigioPan.setOnClickListener {
//            if (requestPermission()) {
//                getPanDataFromDigio()
//            }
//        }

        uploadPanBinding.btnSendDigioPan.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Pan Camera Launched")//added by CleverTap Assistant
            launchCamera()
        }
    }

    private fun fetchLocation(from: Int) {

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
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
                            clevertapDefaultInstance?.pushEvent("Pan location fetched")//added by CleverTap Assistant
                            // use location, this might get called in a different thread if a location is a last known location. In that case, you can post location on main thread
                        }
                    })
            }
            else -> {
                val request = permissionsBuilder(
                    ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION
                ).build()
                request.listeners {
                    onAccepted {
                        fetchLocation(from)
                        clevertapDefaultInstance?.pushEvent("Pan Location Permission Accepted")//added by CleverTap Assistant
                    }
                    onDenied {
                        clevertapDefaultInstance?.pushEvent("Pan Location Permission Denied")//added by CleverTap Assistant
                    }
                    onPermanentlyDenied {
                        clevertapDefaultInstance?.pushEvent("Pan Location Permission Denied Permanently")//added by CleverTap Assistant
                    }
                }
                request.send()
            }
        }
    }

    private fun launchCamera() {
        startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(
                this@UploadPanActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "ArthurFinance"
            )
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
                    data?.data?.let { uri ->
                        compressImage(uri.toString())
                    }
                    val intent = CropImage.activity(fileUri).getIntent(this)
                    startActivityForResult(intent, CROP_REQUEST_CODE_CAMERA)
                }

                CROP_REQUEST_CODE_CAMERA -> {
                    try {
                        val result = CropImage.getActivityResult(data)
                        val resultUri = result.uri
                        val bitmapImage = BitmapFactory.decodeFile(resultUri.path)
                        val nh = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                        val scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true)
                        uploadPanBinding.digioPanImage.setImageBitmap(scaled)
                        val encodedImageStr = encodeImageString(scaled)
                        showProgressDialog()
                        verifyPanImage(encodedImageStr)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun verifyPanImage(encodedImageStr: String?) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("loanId", mCustomerId)
        jsonObject.addProperty("amId", mCustomerId)
        jsonObject.addProperty("leadId", leadId)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("idType", "PAN")
        jsonObject.addProperty("imageBase64", encodedImageStr)
        jsonObject.addProperty("applicationType", "CUSTOMER")
        clevertapDefaultInstance?.pushEvent("Pan upload started")//added by CleverTap Assistant
        Log.e("TAG", jsonObject.toString())
        ApiClient().getAuthApiService(this).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>,
            ) {
                try {
                    val docResponse = response.body() as LoanProcessResponse
                    val apiCode = docResponse.apiCode
                    val message = docResponse.apiMessage

                    if (apiCode.equals("200")) {
                        clevertapDefaultInstance?.pushEvent("Pan upload success")//added by CleverTap Assistant
                        Toast.makeText(
                            this@UploadPanActivity,
                            "PAN uploaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressDialog()
                        val intent1 =
                            Intent(
                                this@UploadPanActivity,
                                UploadAadharActivity::class.java
                            )
//                        intent1.putExtra("registerTime", intent.getStringExtra("registerTime"))
                        startActivity(intent1)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else if (apiCode.equals("400")) {
                        hideProgressDialog()
                        clevertapDefaultInstance?.pushEvent("Pan upload failed")//added by CleverTap Assistant

                        try {
                            Toast.makeText(
                                this@UploadPanActivity,
                                docResponse.apiMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        hideProgressDialog()
                        clevertapDefaultInstance?.pushEvent("PAN Card is not visible properly")//added by CleverTap Assistant
                        Toast.makeText(
                            this@UploadPanActivity,
                            "Your PAN Card is not visible properly, Please scan again",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideProgressDialog()
                    }
                } catch (ex: NullPointerException) {
                    Log.e("TAG", ex.toString())
                    clevertapDefaultInstance?.pushEvent("PAN Card is not visible properly")//added by CleverTap Assistant
                    Toast.makeText(
                        this@UploadPanActivity,
                        "Your PAN Card is not visible properly, Please scan again",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("PAN service failure")//added by CleverTap Assistant
                Toast.makeText(
                    this@UploadPanActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })

    }

//    private fun getPanDataFromDigio() {
//        val digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()
//
//        val kycRequest = DigioTaskRequest()
//        kycRequest.taskType = DigioTaskType.ID_ANALYSIS
//        kycRequest.isFaceMatch =
//            false // Optional,  In case business required selfie and face match with Aadhaar
//        digioTaskList.add(kycRequest)
//
//        val config = DigioKycConfig()
//        config.setEnvironment(DigioEnvironment.SANDBOX)
//        config.setPrimaryColor(Color.parseColor("#17c39b"))
//        config.setSecondaryColor(Color.parseColor("#B4E9D8"))
//        try {
//            val digioStateLessSession = DigioStateLessSession()
//            digioStateLessSession.init(
//                this,
//                config,
//                "SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X",
//                "SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB"
//            )
//            digioStateLessSession.startStateLessSession(digioTaskList, this)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun onDigioEventTracker(eventTracker: JSONObject) {
//
//    }
//
//    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
//        for (digiTaskResponse in failure) {
//            val digioTaskRequest = digiTaskResponse.getResponse()
//            val msg = digioTaskRequest.getString("message")
//            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
//        for (digiTaskResponse in taskResponseList) {
//
//            val digioTaskRequest = digiTaskResponse.getTask()
//
//            val taskType = digioTaskRequest.taskType
//            if (taskType == DigioTaskType.ID_ANALYSIS) {
//                val mainResponse = digiTaskResponse.getResponse()
//                val analysisResponse = mainResponse.getJSONObject("analysis_response")
//                val customerName = analysisResponse.getString("name")
//                val customerFatherName = analysisResponse.getString("fathers_name")
//                val panNo = analysisResponse.getString("id_no")
//                val customerDob = analysisResponse.getString("dob")
//                val panImg = mainResponse.getString("id_front_uri")
//
//                val sharedPref: SharedPreferences =
//                    getSharedPreferences("father_name", Context.MODE_PRIVATE)
//                val editor = sharedPref.edit()
//                editor.putString("father_name", customerFatherName)
//                editor.apply()
//
//                Glide.with(this)
//                    .asBitmap()
//                    .load(panImg)
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap>?
//                        ) {
//                            try {
//                                digioPanImage.setImageBitmap(resource)
//                                val encodedImageStr = encodeImageString(resource)
//                                uploadPanImage(
//                                    encodedImageStr,
//                                    customerName,
//                                    customerFatherName,
//                                    panNo,
//                                    customerDob
//                                )
//                                showProgressDialog()
//                            } catch (ex: Exception) {
//                                ex.printStackTrace()
//                            }
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {
//
//                        }
//                    })
//            }
//
//        }
//    }

//    private fun requestPermission(): Boolean {
//        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.CAMERA,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ), MY_CAMERA_PERMISSION_CODE
//            )
//        } else {
//            return true
//        }
//        return false
//    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getPanDataFromDigio()
//            } else {
//                Toast.makeText(
//                    this,
//                    "camera permission denied",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

//    private fun uploadPanImage(
//        encodedImageStr: String?,
//        customerName: String,
//        customerFatherName: String,
//        panNo: String,
//        customerDob: String
//    ) {
//
//        val jsonObject = JsonObject()
//
//        var customerId = ""
//        if (intent.hasExtra("loanResponse")) {
//            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
//            customerId = intent.getStringExtra("coCustomerId").toString()
//
//            jsonObject.addProperty("customerId", customerId)
//            jsonObject.addProperty("applicantType", "CA")
//            jsonObject.addProperty("panNo", panNo)
//            jsonObject.addProperty("panImage", encodedImageStr)
//            jsonObject.addProperty("customerName", customerName)
//            jsonObject.addProperty("customerDob", customerDob)
//            jsonObject.addProperty("fatherName", customerFatherName)
//
//        } else {
//            customerId = customerData!!.customerId.toString()
//
//            jsonObject.addProperty("customerId", customerId)
//            jsonObject.addProperty("applicantType", "PA")
//            jsonObject.addProperty("panNo", panNo)
//            jsonObject.addProperty("panImage", encodedImageStr)
//            jsonObject.addProperty("customerName", customerName)
//            jsonObject.addProperty("customerDob", customerDob)
//            jsonObject.addProperty("fatherName", customerFatherName)
//        }
//
//
//        ApiClient().getAuthApiService(this).uploadCustomerPan(jsonObject).enqueue(object :
//            Callback<AuthenticationResponse> {
//            override fun onResponse(
//                call: Call<AuthenticationResponse>,
//                response: Response<AuthenticationResponse>
//            ) {
//                hideProgressDialog()
//                val docResponse = response.body() as AuthenticationResponse
//                val apiCode = docResponse.apiCode
//                //val message = docResponse.message
//
//                if (apiCode.equals("200")) {
//                    Toast.makeText(
//                        this@UploadPanActivity,
//                        "Pan data uploaded successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//
//                    if (intent.hasExtra("loanResponse")) {
//                        val intent1 =
//                            Intent(this@UploadPanActivity, HomeDashboardActivity::class.java)
//                        intent1.putExtra("customerId", customerId)
//                        intent1.putExtra("fatherName", customerFatherName)
//                        intent1.putExtra("customerName", customerName)
//                        intent1.putExtra("customerDob", customerDob)
//                        intent1.putExtra("customerGender", customerFatherName)
//                        intent1.putExtra("loanResponse", loanResponse)
//                        startActivity(intent1)
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                    } else {
//                        val intent1 =
//                            Intent(this@UploadPanActivity, UploadPhotoActivity::class.java)
//                        intent1.putExtra("customerId", customerId)
//                        intent1.putExtra("fatherName", customerFatherName)
//                        intent1.putExtra("customerName", customerName)
//                        intent1.putExtra("customerDob", customerDob)
//                        intent1.putExtra("customerGender", customerFatherName)
//                        startActivity(intent1)
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//                    }
//
//
//                } else {
//                    try {
//                        val jObjError = JSONObject(response.errorBody()!!.string())
//                        Toast.makeText(
//                            this@UploadPanActivity,
//                            jObjError.getJSONObject("error").getString("message"),
//                            Toast.LENGTH_LONG
//                        ).show()
//                    } catch (e: Exception) {
//                        Toast.makeText(this@UploadPanActivity, e.message, Toast.LENGTH_LONG).show()
//                    }
//                    Toast.makeText(
//                        this@UploadPanActivity,
//                        "PAN Details Upload Failed. Please Try After Sometime",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    hideProgressDialog()
//                }
//
//            }
//
//            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
//                t.printStackTrace()
//                Toast.makeText(
//                    this@UploadPanActivity,
//                    "Service Failure, Once Network connection is stable, will try to resend again",
//                    Toast.LENGTH_SHORT
//                ).show()
//                hideProgressDialog()
//            }
//        })
//    }


    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        var options = 90
        while (byteArrayOutputStream.toByteArray()
                .toString().length / 1024 > 400
        ) {  //Loop if compressed picture is greater than 400kb, than to compression
            byteArrayOutputStream.reset() //Reset baos is empty baos
            bm.compress(
                Bitmap.CompressFormat.JPEG,
                options,
                byteArrayOutputStream
            ) //The compression options%, storing the compressed data to the baos
            options -= 10 //Every time reduced by 10
        }
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    override fun onBackPressed() {
        clevertapDefaultInstance?.pushEvent("Back from PAN upload")//added by CleverTap Assistant
        finish()
    }

    fun compressImage(imageUri: String): String? {
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

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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