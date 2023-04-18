package com.av.arthanfinance.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_my_account.*
import kotlinx.android.synthetic.main.content_profile.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class MyAccount : BaseActivity() {
    var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null

    private var lat: String? = null
    private var lng: String? = null

    private var mobNo: String? = null

    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQ_CODE_PHOTO_ID = 321
    private var REQ_CODE = 33
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var isPhotoUploaded: Boolean = false
    private lateinit var circleImg: CircleImageView
    private var maritalStatusList =
        arrayOf("Select Marital Status", "Single", "Married", "Separated", "Divorced")
    override val layoutId: Int
        get() = R.layout.activity_my_account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        val mPrefs: SharedPreferences? =
            getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        circleImg = findViewById(R.id.iv_profile_image)

        val maritalStatusAdapter =
            ArrayAdapter(this, R.layout.emi_options, maritalStatusList)
        sp_maritalstatus.adapter = maritalStatusAdapter

        getProfileData()

        update_profile.setOnClickListener {
            if (findViewById<EditText>(R.id.edt_email)?.text.toString().isNotEmpty()) {
                if (!isValidEmailId(findViewById<EditText>(R.id.edt_email)?.text.toString())) {
                    Toast.makeText(
                        this@MyAccount,
                        "Please enter valid Email Address.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

//                if (!btnTC.isChecked) {
//                    Toast.makeText(
//                        this@RegistrationActivity,
//                        "Please accept terms and Conditions.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
                updateProfileData()
            } else {
                Toast.makeText(
                    this@MyAccount,
                    "Mandatory fields missing please enter all data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        iv_profile_image.setOnClickListener {
            if (requestPermission()) {
                launchCamera(REQ_CODE)
            }
        }

        img_back.setOnClickListener {
            finish()
        }
    }

    private fun isValidEmailId(phoneNumber: CharSequence): Boolean {
        return if (!TextUtils.isEmpty(phoneNumber)) {
            Patterns.EMAIL_ADDRESS.matcher(phoneNumber).matches()
        } else false
    }

    private fun getProfileData() {
        showProgressDialog()
        val customerId = mCustomerId.toString()

        ApiClient().getAuthApiService(this).getProfile(customerId).enqueue(object :
            Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                hideProgressDialog()
                val userData = response.body()

                findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                if (userData != null) {
                    mobNo = userData.mobileNo
                    userData.name?.let {
                        findViewById<TextView>(R.id.edt_name)
                            ?.setText(it)
                    }
                    userData.name?.let {
                        findViewById<TextView>(R.id.name)
                            ?.setText(it)
                    }
                    userData.mobileNo?.let {
                        findViewById<TextView>(R.id.mobno)?.setText(it)
                    }
                    userData.dob?.let {
                        findViewById<TextView>(R.id.edt_dob)?.setText(it)
                    }
                    userData.emailId?.let {
                        findViewById<TextView>(R.id.edt_email)?.setText(it)
                    }
                    userData.panNo?.let {
                        findViewById<TextView>(R.id.edt_pan_no)?.setText(it)
                    }
                    userData.resiAddress?.let {
                        findViewById<TextView>(R.id.edt_resi_address)?.setText(it)
                    }
                    userData.ofcAddress?.let {
                        findViewById<TextView>(R.id.edt_ofc_address)?.setText(it)
                    }
                    if (response.body()?.customerImg.equals("") || response.body()?.customerImg == null) {
                        Glide
                            .with(this@MyAccount)
                            .load(R.drawable.upload).fitCenter()
                            .placeholder(R.drawable.upload)
                            .into(circleImg)

                    } else {
                        response.body()?.customerImg?.let {
                            Glide
                                .with(this@MyAccount)
                                .load(it).fitCenter()
                                .placeholder(R.drawable.upload)
                                .into(circleImg)
                        }
                    }
                } else {
                    findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                Toast.makeText(
                    this@MyAccount,
                    "A server error occurred. Please try after some time",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


    }

    private fun updateProfileData() {
        val maritalStatus = sp_maritalstatus.selectedItem.toString()
        if (maritalStatus == "" || maritalStatus == "Select Marital Status") {
            Toast.makeText(this, "Please select your marital status", Toast.LENGTH_SHORT).show()
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", findViewById<EditText>(R.id.edt_name)?.text.toString())
        jsonObject.addProperty("dob", findViewById<EditText>(R.id.edt_dob)?.text.toString())
        jsonObject.addProperty(
            "emailId",
            findViewById<EditText>(R.id.edt_email)?.text.toString()
        )
        jsonObject.addProperty(
            "mobileNo", mobNo
        )
        jsonObject.addProperty(
            "resiAddress",
            findViewById<EditText>(R.id.edt_resi_address)?.text.toString()
        )
        jsonObject.addProperty(
            "ofcAddress",
            findViewById<EditText>(R.id.edt_ofc_address)?.text.toString()
        )
        jsonObject.addProperty(
            "customerId", mCustomerId
        )
        jsonObject.addProperty("maritalStatus", maritalStatus)
        Log.e("Payload", jsonObject.toString())
        showProgressDialog()
        ApiClient().getAuthApiService(this@MyAccount).updateProfile(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val updateRespoonse = response.body() as AuthenticationResponse
                if (updateRespoonse.apiCode == 200.toString()) {
                    Toast.makeText(
                        this@MyAccount,
                        "Profile Updated Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(
                        this@MyAccount,
                        HomeDashboardActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@MyAccount,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@MyAccount,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun launchCamera(reqCode: Int) {
        startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(
                this@MyAccount.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
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
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmapImage = BitmapFactory.decodeFile(resultUri.path)
                    val nh = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                    val scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true)
                    iv_profile_image.setImageBitmap(scaled)
                    val encodedImageStr = encodeImageString(scaled)
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
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("customerImage", encodedImageStr)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)

        Log.e("TAGREQ", jsonObject.toString())

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
                    hideProgressDialog()
                    isPhotoUploaded = true
                    Toast.makeText(
                        this@MyAccount,
                        "Photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    hideProgressDialog()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@MyAccount,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(
                            this@MyAccount,
                            e.message,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    Toast.makeText(
                        this@MyAccount,
                        "Photo Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@MyAccount,
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
            scaledBitmap =
                Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
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
        reqHeight: Int
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