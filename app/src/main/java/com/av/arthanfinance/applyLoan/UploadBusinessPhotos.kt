package com.av.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import com.av.arthanfinance.UploadReferenceActivity
import com.av.arthanfinance.databinding.ActivityUploadBusinessDetailsBinding
import com.av.arthanfinance.databinding.ActivityUploadBusinessPhotosBinding
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

class UploadBusinessPhotos : BaseActivity() {
    private lateinit var activityUploadBusinessPhotosBinding: ActivityUploadBusinessPhotosBinding
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var REQ_CODE_CAMERA = 322

    private var REQUEST_CODE_GALLERY_SHOP_OWNER = 201
    private var CROP_REQUEST_CODE_GALLERY_SHOP_OWNER = 202
    private var CROP_REQUEST_CODE_CAMERA_SHOP_OWNER = 203
    private var REQ_CODE_CAMERA_SHOP_OWNER = 422

    private var REQUEST_CODE_GALLERY_STOCK = 301
    private var CROP_REQUEST_CODE_GALLERY_STOCK = 302
    private var CROP_REQUEST_CODE_CAMERA_STOCK = 303
    private var REQ_CODE_CAMERA_STOCK = 522

    private var REQUEST_CODE_GALLERY_SHOP_LOCALITY = 401
    private var CROP_REQUEST_CODE_GALLERY_SHOP_LOCALITY = 402
    private var CROP_REQUEST_CODE_CAMERA_SHOP_LOCALITY = 403
    private var REQ_CODE_CAMERA_SHOP_LOCALITY = 622

    private var kycCompleteStatus = "80"

    private var mCustomerId: String? = null

    var customerData: CustomerHomeTabResponse? = null
    override val layoutId: Int
        get() = R.layout.activity_upload_business_photos


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadBusinessPhotosBinding =
            ActivityUploadBusinessPhotosBinding.inflate(layoutInflater)
        setContentView(activityUploadBusinessPhotosBinding.root)

        if (intent.hasExtra("customerData")) {
            try {
                customerData =
                    intent.getSerializableExtra("customerData") as CustomerHomeTabResponse
            } catch (e: Exception) {
                Log.e("Error", e.printStackTrace().toString())
            }

        }
        activityUploadBusinessPhotosBinding.tvPercent.text = "${kycCompleteStatus}%"
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

        activityUploadBusinessPhotosBinding.btnUploadOwnerPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "ChooseFile"),
                REQUEST_CODE_GALLERY_SHOP_OWNER
            )
        }

        activityUploadBusinessPhotosBinding.btnCameraOwnerPhoto.setOnClickListener {
            if (requestPermission(REQ_CODE_CAMERA_SHOP_OWNER)) {
                launchCamera(REQ_CODE_CAMERA_SHOP_OWNER)
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

        activityUploadBusinessPhotosBinding.btnUploadShopLocality.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(
                Intent.createChooser(intent, "ChooseFile"),
                REQUEST_CODE_GALLERY_SHOP_LOCALITY
            )
        }

        activityUploadBusinessPhotosBinding.btnCameraShopLocality.setOnClickListener {
            if (requestPermission(REQ_CODE_CAMERA_SHOP_LOCALITY)) {
                launchCamera(REQ_CODE_CAMERA_SHOP_LOCALITY)
            }
        }

        activityUploadBusinessPhotosBinding.btnNext.setOnClickListener {
            updateStage()
        }
    }

    private fun updateStage() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("stage", "BUSINESS_PHOTOS")
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
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {

                    val intent =
                        Intent(this@UploadBusinessPhotos, UploadReferenceActivity::class.java)
                    intent.putExtra("customerData", customerData)
                    startActivity(intent)
                    finish()
                }

            }
        })
    }

    private fun requestPermission(REQ_CODE: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                    REQ_CODE_CAMERA_SHOP_OWNER -> {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ), REQ_CODE_CAMERA_SHOP_OWNER
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
                    REQ_CODE_CAMERA_SHOP_LOCALITY -> {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ), REQ_CODE_CAMERA_SHOP_LOCALITY
                        )
                    }
//                    REQ_CODE_BANK_STATEMENT -> {
//                        requestPermissions(
//                            arrayOf(
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_EXTERNAL_STORAGE
//                            ), REQ_CODE_BANK_STATEMENT
//                        )
//                    }

                }

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
        if (requestCode == REQ_CODE_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        } else if (requestCode == REQ_CODE_CAMERA_SHOP_OWNER) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA_SHOP_OWNER)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        } else if (requestCode == REQ_CODE_CAMERA_STOCK) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA_STOCK)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        } else if (requestCode == REQ_CODE_CAMERA_SHOP_LOCALITY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera(REQ_CODE_CAMERA_SHOP_LOCALITY)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
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
            REQ_CODE_CAMERA_SHOP_OWNER -> {
                startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                    putExtra(
                        "doc_type",
                        this@UploadBusinessPhotos.REQ_CODE_CAMERA_SHOP_OWNER
                    )
                    val dir = File(
                        this@UploadBusinessPhotos.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArthurFinance"
                    )
                    // if (!dir.exists())
                    dir.mkdirs()
                    val time = System.currentTimeMillis().toString()
                    putExtra("FilePath", "${dir.absolutePath}/IMG_PAN_ID_${time}.jpg")
                    putExtra("is_front", false)
                }, this.REQ_CODE_CAMERA_SHOP_OWNER)

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

            REQ_CODE_CAMERA_SHOP_LOCALITY -> {
                startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                    putExtra(
                        "doc_type",
                        this@UploadBusinessPhotos.REQ_CODE_CAMERA_SHOP_LOCALITY
                    )
                    val dir = File(
                        this@UploadBusinessPhotos.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArthurFinance"
                    )
                    // if (!dir.exists())
                    dir.mkdirs()
                    val time = System.currentTimeMillis().toString()
                    putExtra("FilePath", "${dir.absolutePath}/IMG_PAN_ID_${time}.jpg")
                    putExtra("is_front", true)
                }, this.REQ_CODE_CAMERA_SHOP_LOCALITY)
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

                REQ_CODE_CAMERA_SHOP_OWNER -> {
                    cropCameraImage(CROP_REQUEST_CODE_CAMERA_SHOP_OWNER, data)
                }

                REQ_CODE_CAMERA_STOCK -> {
                    cropCameraImage(CROP_REQUEST_CODE_CAMERA_STOCK, data)
                }

                REQ_CODE_CAMERA_SHOP_LOCALITY -> {
                    cropCameraImage(CROP_REQUEST_CODE_CAMERA_SHOP_LOCALITY, data)
                }

                ////////////////////////////////
                REQUEST_CODE_GALLERY -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY, data)

                }

                REQUEST_CODE_GALLERY_SHOP_OWNER -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY_SHOP_OWNER, data)
                }

                REQUEST_CODE_GALLERY_STOCK -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY_STOCK, data)
                }

                REQUEST_CODE_GALLERY_SHOP_LOCALITY -> {
                    cropGalleryImage(CROP_REQUEST_CODE_GALLERY_SHOP_LOCALITY, data)
                }

                /////////////////////////////////////
                CROP_REQUEST_CODE_CAMERA, CROP_REQUEST_CODE_GALLERY -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    uploadBusinessCard(encodedImageStr, "SHOP_PHOTO")
                }

                CROP_REQUEST_CODE_CAMERA_SHOP_OWNER, CROP_REQUEST_CODE_GALLERY_SHOP_OWNER -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessOwnerImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    uploadBusinessCard(encodedImageStr, "SHOP_OWNER_PHOTO")
                }

                CROP_REQUEST_CODE_CAMERA_STOCK, CROP_REQUEST_CODE_GALLERY_STOCK -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessStockImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    uploadBusinessCard(encodedImageStr, "SHOP_STOCK_PHOTO")
                }

                CROP_REQUEST_CODE_CAMERA_SHOP_LOCALITY, CROP_REQUEST_CODE_GALLERY_SHOP_LOCALITY -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
                    activityUploadBusinessPhotosBinding.businessLocalityImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    uploadBusinessCard(encodedImageStr, "SHOP_LOCALITY_PHOTO")
                }
//                REQ_CODE_BANK_STATEMENT -> {
//                    data?.apply {
//                        if (this.data == null) return
//                        val file = copyFile(this@UploadBusinessDetailsActivity, this.data!!)
//                        if (file != null && file.absolutePath.isNotEmpty()) {
//                            uploadStatement(file.absolutePath)
//                        }
//                    }
//                }
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
            val intent = CropImage.activity(fileUri)
                .getIntent(this)
            startActivityForResult(intent, cropRequestCodeCamera)
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
    }

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadBusinessCard(encodedImageStr: String?, idType: String) {

        val jsonObject = JsonObject()
//        jsonObject.addProperty("loanId", loanResponse!!.loanId)
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("applicantType", customerData!!.applicantType)
        jsonObject.addProperty("idType", idType)
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        ApiClient().getAuthApiService(this).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                response.body()

                Toast.makeText(
                    this@UploadBusinessPhotos,
                    "Business Photo Uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBusinessPhotos,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}