package com.example.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextPaint
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.arthanfinance.networkService.ApiClient
import com.example.arthanfinance.R
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


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

        btncamera.setOnClickListener{
            if(requestPermission()) {
                launchCamera(REQ_CODE)
            }
        }
        btnNext.setOnClickListener{
            updatePhotoDetails()
        }

        return view
    }

    private fun launchCamera(cameraRequest: Int) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, cameraRequest)
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
        if(requestCode == REQ_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            photoImageView.setImageBitmap(bitmap)
            val encodedImageStr = encodeImageString(bitmap)
            print("base64 Stirng $encodedImageStr")
            uploadPhotoImage(encodedImageStr)
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
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
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

    private fun updatePhotoDetails() {
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)

        val context = activity?.applicationContext!!
         ApiClient().getApiService(context).updatePhoto(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
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
                        (activity as UploadKycDetailsActivity?)?.selectIndex(2)
                    }
                }
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

}