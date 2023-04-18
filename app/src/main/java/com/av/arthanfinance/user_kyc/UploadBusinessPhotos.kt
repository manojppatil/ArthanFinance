package com.av.arthanfinance.user_kyc

import android.Manifest
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
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.applyLoan.model.UdyamDetailsResponse
import com.av.arthanfinance.databinding.ActivityUploadBusinessPhotosBinding
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.AppLocationProvider
import com.av.arthanfinance.util.ArthanFinConstants
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class UploadBusinessPhotos : BaseActivity() {
    private lateinit var activityUploadBusinessPhotosBinding: ActivityUploadBusinessPhotosBinding
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var REQ_CODE_CAMERA = 322

    private var REQUEST_CODE_GALLERY_STOCK = 301
    private var CROP_REQUEST_CODE_GALLERY_STOCK = 302
    private var CROP_REQUEST_CODE_CAMERA_STOCK = 303
    private var REQ_CODE_CAMERA_STOCK = 522

    private var shopUploadStatus = 0
    private var stockUploadStatus = 0

    private var mCustomerId: String? = null
    private var leadId: String? = null

    private var lat: String? = null
    private var lng: String? = null

    var customerData: AuthenticationResponse? = null
    override val layoutId: Int
        get() = R.layout.activity_upload_business_photos


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadBusinessPhotosBinding =
            ActivityUploadBusinessPhotosBinding.inflate(layoutInflater)
        setContentView(activityUploadBusinessPhotosBinding.root)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        leadId = mPrefs?.getString("leadId", null)

        fetchLocation(1)

        activityUploadBusinessPhotosBinding.btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
        }

        activityUploadBusinessPhotosBinding.btnCamera.setOnClickListener {
            if (requestPermission(REQ_CODE_CAMERA)) {
                launchCamera(REQ_CODE_CAMERA)
            }
        }

        activityUploadBusinessPhotosBinding.btnUploadStockPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "ChooseFile"),
                REQUEST_CODE_GALLERY_STOCK
            )
        }

        activityUploadBusinessPhotosBinding.btnCameraStock.setOnClickListener {
            if (requestPermission(REQ_CODE_CAMERA_STOCK)) {
                launchCamera(REQ_CODE_CAMERA_STOCK)
            }
        }


        activityUploadBusinessPhotosBinding.btnNext.setOnClickListener {
            if (shopUploadStatus == 0) {
                Toast.makeText(this, "Please upload shop photo", Toast.LENGTH_SHORT).show()
            } else if (stockUploadStatus == 0) {
                Toast.makeText(this, "Please upload stock photo", Toast.LENGTH_SHORT).show()
            } else {
                updateStage(ArthanFinConstants.business_photos)
            }
        }

        activityUploadBusinessPhotosBinding.btnSkipBusinessPics.setOnClickListener {
            updateStage(ArthanFinConstants.skip_business_photos)
        }

        activityUploadBusinessPhotosBinding.imgBack.setOnClickListener {
            finish()
        }

        activityUploadBusinessPhotosBinding.tvVerifyUdyam.setOnClickListener {
            val jsonObject = JsonObject()
            showProgressDialog()
//            activityUploadBusinessPhotosBinding.lytUdyam.visibility = View.GONE
            jsonObject.addProperty("consent", "Y")
            jsonObject.addProperty(
                "udyamRegistrationNo",
                activityUploadBusinessPhotosBinding.tieUdhyogAadharNo.text.toString()
            )
            jsonObject.addProperty("isPDFRequired", "N")

            val clientId = "QyxO5SCDPkDIX00"

            ApiClient().getUdyamApiService(this).verifyUdyamAadhar(clientId, jsonObject)
                .enqueue(object :
                    Callback<UdyamDetailsResponse> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(
                        call: Call<UdyamDetailsResponse>,
                        response: Response<UdyamDetailsResponse>,
                    ) {
                        hideProgressDialog()
                        try {
                            val udyamReturnResponse = response.body()
                            val statusCode = udyamReturnResponse!!.statusCode
                            if (statusCode == 101) {
                                val requestId = udyamReturnResponse.requestId
                                Log.e("UdyamRID", requestId.toString())
                                activityUploadBusinessPhotosBinding.tvVerifyUdyam.text = "Verified"
                                activityUploadBusinessPhotosBinding.ivUdyamVerified.visibility =
                                    View.VISIBLE
                                val bname = udyamReturnResponse.result!!.profile!!.name.toString()
                                val bdoi =
                                    udyamReturnResponse.result!!.profile!!.dateOfIncorporation.toString()
                                val borgType =
                                    udyamReturnResponse.result!!.profile!!.organizationType.toString()
                                val bindustry =
                                    udyamReturnResponse.result!!.industry[0].industry.toString()
                                val bsector =
                                    udyamReturnResponse.result!!.industry[0].subSector.toString()
                                val bactivity =
                                    udyamReturnResponse.result!!.industry[0].activity.toString()
                                val bpan = udyamReturnResponse.result!!.profile!!.pan.toString()
                                val bAddress =
                                    udyamReturnResponse.result!!.officialAddress!!.flat + "," + udyamReturnResponse.result!!.officialAddress!!.premises + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.village + "," + udyamReturnResponse.result!!.officialAddress!!.block + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.road + "," + udyamReturnResponse.result!!.officialAddress!!.city + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.state + "," + udyamReturnResponse.result!!.officialAddress!!.pincode + "," + udyamReturnResponse.result!!.officialAddress!!.district

                                saveCustomerBusinessData(
                                    1,
                                    bname,
                                    bdoi,
                                    borgType,
                                    bindustry,
                                    bsector,
                                    bactivity,
                                    bpan,
                                    bAddress
                                )
                            } else {
                                Toast.makeText(
                                    this@UploadBusinessPhotos,
                                    "Please check your UDYAM Aadhaar ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            Log.e("TAG", ex.toString())
                        }
                    }

                    override fun onFailure(call: Call<UdyamDetailsResponse>, t: Throwable) {
                        t.printStackTrace()
                        hideProgressDialog()
                        Toast.makeText(
                            this@UploadBusinessPhotos,
                            "Service Failure, Once Network connection is stable, will try to resend again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
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

    private fun saveCustomerBusinessData(
        statusFrom: Int,
        bName: String,
        bDoi: String,
        bOrgType: String,
        bIndustry: String,
        bSector: String,
        bActivity: String,
        bPan: String,
        bAddress: String,
    ) {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty(
            "udhyogAadhar",
            activityUploadBusinessPhotosBinding.tieUdhyogAadharNo.text.toString()
        )
        jsonObject.addProperty("businessName", bName)
        jsonObject.addProperty("category", bIndustry)
        jsonObject.addProperty("segment", bSector)
        jsonObject.addProperty("type", bActivity)
        jsonObject.addProperty("businessPan", bPan)
        jsonObject.addProperty("businessAddress", bAddress)
        jsonObject.addProperty("dateOfIncorporation", bDoi)
        jsonObject.addProperty("constitution", bOrgType)
        jsonObject.addProperty("gstNo", "")
        jsonObject.addProperty("turnover", "")
        jsonObject.addProperty("income", "")
        jsonObject.addProperty("expenses", "")
        jsonObject.addProperty("margin", 25)
        jsonObject.addProperty("addressType", false)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)

        Log.e("TAG", jsonObject.toString())

        ApiClient().getAuthApiService(this).saveCustBusiness(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    if (statusFrom == 1) {
                        Toast.makeText(
                            this@UploadBusinessPhotos,
                            "UDYAM Aadhaar verified successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@UploadBusinessPhotos,
                        "Something went wrong, Please try again later!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadBusinessPhotos,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateStage(stage: String) {
        showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("stage", stage)
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBusinessPhotos, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {
                    val intent =
                        Intent(
                            this@UploadBusinessPhotos,
                            HomeDashboardActivity::class.java
                        )
                    intent.putExtra("customerData", customerData)
                    startActivity(intent)
                    finish()
                }

            }
        })
    }

    private fun requestPermission(REQ_CODE: Int): Boolean {
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            when (REQ_CODE) {
                REQ_CODE_CAMERA -> {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), REQ_CODE_CAMERA
                    )
                }
                REQ_CODE_CAMERA_STOCK -> {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), REQ_CODE_CAMERA_STOCK
                    )
                }
            }

        } else {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == REQ_CODE_CAMERA_STOCK) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA_STOCK)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun launchCamera(REQ_CODE: Int) {
        when (REQ_CODE) {
            REQ_CODE_CAMERA -> {
                startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                    putExtra("doc_type", this@UploadBusinessPhotos.REQ_CODE_CAMERA)
                    val dir = File(
                        this@UploadBusinessPhotos.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArthanFinance"
                    )
                    // if (!dir.exists())
                    dir.mkdirs()
                    val time = System.currentTimeMillis().toString()
                    putExtra("FilePath", "${dir.absolutePath}/IMG_PAN_ID_${time}.jpg")
                    putExtra("is_front", true)
                }, this.REQ_CODE_CAMERA)

            }
            REQ_CODE_CAMERA_STOCK -> {
                startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                    putExtra("doc_type", this@UploadBusinessPhotos.REQ_CODE_CAMERA_STOCK)
                    val dir = File(
                        this@UploadBusinessPhotos.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArthurFinance"
                    )
                    // if (!dir.exists())
                    dir.mkdirs()
                    val time = System.currentTimeMillis().toString()
                    putExtra("FilePath", "${dir.absolutePath}/IMG_PAN_ID_${time}.jpg")
                    putExtra("is_front", true)
                }, this.REQ_CODE_CAMERA_STOCK)

            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_CAMERA -> {
                    cropCameraImage(CROP_REQUEST_CODE_CAMERA, data)
                }

                REQ_CODE_CAMERA_STOCK -> {
                    cropCameraImage(CROP_REQUEST_CODE_CAMERA_STOCK, data)
                }

                ////////////////////////////////
                REQUEST_CODE_GALLERY -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY, data)

                }
                REQUEST_CODE_GALLERY_STOCK -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY_STOCK, data)
                }

                /////////////////////////////////////
                CROP_REQUEST_CODE_CAMERA, CROP_REQUEST_CODE_GALLERY -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    shopUploadStatus = 1
                    uploadBusinessCard(encodedImageStr, "BUSINESS", 1)
                }

                CROP_REQUEST_CODE_CAMERA_STOCK, CROP_REQUEST_CODE_GALLERY_STOCK -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessStockImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    stockUploadStatus = 1
                    uploadBusinessCard(encodedImageStr, "SHOP_STOCK_PHOTO", 1)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun cropGalleryImage(cropRequestCodeGallery: Int, data: Intent?) {
        try {
            val fileUri = data!!.data
            val intent = CropImage.activity(fileUri)
                .getIntent(this)
            startActivityForResult(intent, cropRequestCodeGallery)
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }

    private fun cropCameraImage(cropRequestCodeCamera: Int, data: Intent?) {
        try {
            val filepath = data?.extras?.get("FilePath")
            val finalFilePath = "file://${filepath}"
            val fileUri = Uri.parse(finalFilePath)
            data?.data?.let { uri ->
                compressImage(uri.toString())
            }
            val intent = CropImage.activity(fileUri)
                .getIntent(this)
            startActivityForResult(intent, cropRequestCodeCamera)
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }

    private fun uploadBusinessCard(encodedImageStr: String?, idType: String, statusFrom: Int) {

        val jsonObject = JsonObject()
        showProgressDialog("Uploading Image...")
        jsonObject.addProperty("loanId", mCustomerId)
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("amId", mCustomerId)
        jsonObject.addProperty("leadId", leadId)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("idType", idType)
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        ApiClient().getAuthApiService(this).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>,
            ) {
                response.body()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadBusinessPhotos,
                    "Business Photo Uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadBusinessPhotos,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

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
        finish()
    }

    fun compressImage(imageUri: String): String {
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