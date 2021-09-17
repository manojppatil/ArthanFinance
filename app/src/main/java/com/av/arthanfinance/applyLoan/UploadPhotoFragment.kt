package com.av.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextPaint
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.R
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.util.ArthanFinConstants
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class UploadPhotoFragment : Fragment() {
    private lateinit var tvUploadPhoto: TextView
    private lateinit var btnNext: Button
    private lateinit var btncamera: Button
    private lateinit var btnUploadFromLocal: Button
    private lateinit var photoImageView: ImageView
    private var REQ_CODE = 33
    private var MY_CAMERA_PERMISSION_CODE = 100
    private lateinit var apiClient: ApiClient
    private var loanResponse: LoanProcessResponse? = null
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_upload_photo, container, false)
        apiClient = ApiClient()
        tvUploadPhoto = view.findViewById(R.id.tv_upload_photo)
        btnNext = view.findViewById(R.id.btn_next_in_uploadPhoto)
        btncamera = view.findViewById(R.id.btn_camera_uploadPhoto)
        btnUploadFromLocal = view.findViewById(R.id.btn_uploadPhoto)
        photoImageView = view.findViewById(R.id.photoImgView)

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val paint: TextPaint = tvUploadPhoto.getPaint()
        val width = paint.measureText("Personal Details")
        val shader = LinearGradient(0f, 0f, width, tvUploadPhoto.textSize, resources.getColor(
            R.color.dark_orange2, activity?.theme), resources.getColor(
            R.color.indigoBlue, activity?.theme), Shader.TileMode.CLAMP)
        tvUploadPhoto.paint.shader = shader

        view.findViewById<Button>(R.id.btn_uploadPhoto).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
        }
        btncamera.setOnClickListener{
            if(requestPermission()) {
                launchCamera(REQ_CODE)
            }
        }
        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW,false)
        isCreateFlow?.let {
            if (!it){
                getKYCDetailsFromServer()
                disableEditFeature(view)
            }
        }
        btnNext.setOnClickListener{
            if (!isCreateFlow!!){
                (activity as UploadKycDetailsActivity?)?.selectIndex(2)
            }else
                updatePhotoDetails()
        }


        return view
    }
    private var REQ_CODE_PHOTO_ID = 321

    private fun disableEditFeature(view: View) {
        view.findViewById<AppCompatButton>(R.id.btn_uploadPhoto).visibility = View.GONE
        view.findViewById<AppCompatButton>(R.id.btn_camera_uploadPhoto).visibility = View.GONE
        view.findViewById<AppCompatTextView>(R.id.tv_upload_photo).setText("Your Photo")
    }


    private fun launchCamera(cameraRequest: Int) {
        startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ArthurFinance")
            // if (!dir.exists())
            dir.mkdirs()
            val time =System.currentTimeMillis().toString()
            putExtra("FilePath", "${dir.absolutePath}/IMG_PHOTO_ID_${time}.jpg")
            putExtra("is_front",true)
        }, REQ_CODE_PHOTO_ID)
    }


    private fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED && activity?.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED && activity?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
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
                Toast.makeText(activity?.applicationContext, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity?.applicationContext, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK){

            when(requestCode){
                REQ_CODE_PHOTO_ID->{
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri).getIntent(requireContext())
                    startActivityForResult(intent,CROP_REQUEST_CODE_CAMERA)
                }
                REQUEST_CODE_GALLERY->{
                    try {
                        val fileUri = data!!.data
                        val intent = CropImage.activity(fileUri)
                            .getIntent(requireContext())
                        startActivityForResult(intent, CROP_REQUEST_CODE_GALLERY)
                    }catch (e : Exception){
                        Log.e("Exception",e.toString())
                    }
                }
                CROP_REQUEST_CODE_CAMERA,CROP_REQUEST_CODE_GALLERY ->{
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                    photoImageView.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    print("base64 Stirng $encodedImageStr")
                    uploadPhotoImage(encodedImageStr)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }



    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadPhotoImage(encodedImageStr: String?) {
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("idType", "PHOTO")
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val docResponse = response.body()
                Toast.makeText(
                    context,
                    "Photo Uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getKYCDetailsFromServer() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRApplicantPhoto(loanResponse!!.loanId!!,loanResponse!!.applicantType).enqueue(object :
            Callback<LoanKYCDetailsResponse> {
            override fun onFailure(call: Call<LoanKYCDetailsResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<LoanKYCDetailsResponse>,
                response: Response<LoanKYCDetailsResponse>
            ) {
                val kycResponse = response.body()
                Glide
                    .with(this@UploadPhotoFragment)
                    .load(kycResponse!!.applicantImgUrl).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(photoImageView);
            }
        })

    }

    private fun updatePhotoDetails() {
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val context = activity?.applicationContext!!
         ApiClient().getAuthApiService(context).updatePhoto(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                val panDetailsUpdateResposne = response.body() as LoanProcessResponse
                print(panDetailsUpdateResposne)
                if(panDetailsUpdateResposne.apiStatus != "200"){
                    Toast.makeText(
                        context,
                        panDetailsUpdateResposne.apiMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (panDetailsUpdateResposne.nextScreen == "CUST_AADHAR") {
                        panDetailsUpdateResposne.customerId?.let {
                            (activity as UploadKycDetailsActivity?)?.coAppCustId = it
                        }
                        (activity as UploadKycDetailsActivity?)?.selectIndex(2)
                    }
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}