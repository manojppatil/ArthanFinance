package com.example.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.content.Context
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class UploadAdharCardFragment : Fragment() {
    private lateinit var tvUploadAdhar: TextView
    private lateinit var btnCameraAadharFrontPhoto: Button
    private lateinit var btnCameraAadharBackPhoto: Button

    private lateinit var btnRemoveAadharFrontPhoto: ImageButton
    private lateinit var btnRemoveAadharBackPhoto: ImageButton

    private lateinit var btnUploadFileAadharFrontPhoto: Button
    private lateinit var btnUploadFileAadharBackPhoto: Button

    private lateinit var btnAadharFrontRetake: Button
    private lateinit var btnAadharBackRetake: Button

    private lateinit var aadharFrontLayout: LinearLayout
    private lateinit var aadharBackLayout: LinearLayout

    private lateinit var aadharFrontImgView: ImageView
    private lateinit var aadharBackImgView: ImageView

    private lateinit var btnNext: Button

    private lateinit var apiClient: ApiClient
    private var loanResponse: LoanProcessResponse? = null
    private var loanResponseData: UpdateLoanResponseData? = null

    private var REQ_CODE_AADHAR_FP = 33
    private var REQ_CODE_AADHAR_BP = 34
    private var MY_CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    interface UpdateLoanResponseData {
        fun sendData(loanProcessResponse: LoanProcessResponse)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loanResponseData = activity as UpdateLoanResponseData
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.layout_adhar, container, false)
        tvUploadAdhar = view.findViewById(R.id.tv_upload_aadhar_text)

        btnCameraAadharBackPhoto = view.findViewById(R.id.btn_aadhar_back_photo_camera)
        btnCameraAadharFrontPhoto = view.findViewById(R.id.btn_aadhar_front_photo_camera)

        btnRemoveAadharFrontPhoto = view.findViewById(R.id.remove_aadhar_front_photo)
        btnRemoveAadharBackPhoto = view.findViewById(R.id.remove_aadhar_back_photo)

        btnUploadFileAadharFrontPhoto = view.findViewById(R.id.btn_aadhar_front_photo_upload)
        btnUploadFileAadharBackPhoto = view.findViewById(R.id.btn_aadhar_back_photo_upload)

        aadharFrontImgView = view.findViewById(R.id.aadharFront)
        aadharFrontImgView.tag = 1
        aadharBackImgView = view.findViewById(R.id.aadharBack)
        aadharBackImgView.tag = 2

        btnAadharFrontRetake = view.findViewById(R.id.btn_retake_front)
        btnAadharBackRetake = view.findViewById(R.id.btn_retake_back)
        aadharFrontLayout = view.findViewById(R.id.ll_front_aadhar)
        aadharBackLayout = view.findViewById(R.id.ll_back_aadhar)
        btnNext = view.findViewById(R.id.btn_next_aadhar)

        apiClient = ApiClient()
        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        btnAadharFrontRetake.visibility = View.GONE
        btnAadharBackRetake.visibility = View.GONE
        aadharFrontLayout.visibility = View.VISIBLE
        aadharBackLayout.visibility = View.VISIBLE
        btnRemoveAadharFrontPhoto.visibility = View.GONE
        btnRemoveAadharBackPhoto.visibility = View.GONE

        btnCameraAadharFrontPhoto.setOnClickListener {
            if(requestPermission()) {
                launchCamera(REQ_CODE_AADHAR_FP)
            }
        }

        btnCameraAadharBackPhoto.setOnClickListener {
            if(requestPermission()) {
                launchCamera(REQ_CODE_AADHAR_BP)
            }
        }

        btnUploadFileAadharFrontPhoto.setOnClickListener {

        }

        btnUploadFileAadharBackPhoto.setOnClickListener {

        }

        btnAadharFrontRetake.setOnClickListener {
            if(requestPermission()) {
                launchCamera(REQ_CODE_AADHAR_FP)
            }
        }

        btnAadharBackRetake.setOnClickListener {
            if(requestPermission()) {
                launchCamera(REQ_CODE_AADHAR_BP)
            }
        }

        btnRemoveAadharFrontPhoto.setOnClickListener {
            aadharFrontImgView.setImageDrawable(activity?.applicationContext?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, // Context
                    R.drawable.ic_adhar_pic // Drawable
                )
            })

            aadharFrontLayout.visibility = View.VISIBLE
            btnAadharFrontRetake.visibility = View.GONE
            btnRemoveAadharFrontPhoto.visibility = View.GONE
            aadharFrontImgView.tag = 1

        }
        btnRemoveAadharBackPhoto.setOnClickListener {
            aadharBackImgView.setImageDrawable(activity?.applicationContext?.let { it1 ->
                ContextCompat.getDrawable(
                    it1, // Context
                    R.drawable.ic_adhar_pic // Drawable
                )
            })

            aadharBackLayout.visibility = View.VISIBLE
            btnAadharBackRetake.visibility = View.GONE
            btnRemoveAadharBackPhoto.visibility = View.GONE
            aadharBackImgView.tag = 2
        }

        btnNext.setOnClickListener{
//            if(aadharFrontImgView.tag == 1 || aadharBackImgView.tag == 2) {
//                Toast.makeText(activity?.applicationContext, "Please Upload aadhar front and back Images", Toast.LENGTH_LONG).show();
//            } else {
                uploadAadharData()
//            }
        }

        val paint: TextPaint = tvUploadAdhar.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(0f, 0f, width, tvUploadAdhar.textSize, resources.getColor(
            R.color.dark_orange2, activity?.theme), resources.getColor(
            R.color.indigoBlue, activity?.theme), Shader.TileMode.CLAMP)
        tvUploadAdhar.paint.shader = shader
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
        if(requestCode == REQ_CODE_AADHAR_FP && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            aadharFrontImgView.setImageBitmap(bitmap)
            val encodedImageStr = encodeImageString(bitmap)
            print("base64 Stirng $encodedImageStr")
            aadharFrontLayout.visibility = View.GONE
            btnAadharFrontRetake.visibility = View.VISIBLE
            btnRemoveAadharFrontPhoto.visibility = View.VISIBLE
            aadharFrontImgView.tag = 3
            uploadAadharImage(encodedImageStr,"AF")
        }else if(requestCode == REQ_CODE_AADHAR_BP && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            aadharBackImgView.setImageBitmap(bitmap)
            val encodedImageStr = encodeImageString(bitmap)
            print("base64 Stirng $encodedImageStr")
            aadharBackLayout.visibility = View.GONE
            btnAadharBackRetake.visibility = View.VISIBLE
            btnRemoveAadharBackPhoto.visibility = View.VISIBLE
            aadharBackImgView.tag = 4
            uploadAadharImage(encodedImageStr,"AB")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadAadharImage(encodedImageStr: String?, idType: String) {
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("idType", idType)
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                Toast.makeText(
                    context,
                    "Aadhar photo uploaded successfully",
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

    private fun uploadAadharData() {
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
        ApiClient().getApiService(context).updateAadhar(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val docResponse = response.body()
                loanResponse?.addressLine1 = docResponse?.addressLine1
                loanResponse?.addressLine2 = docResponse?.addressLine2
                loanResponse?.addressLine3 = docResponse?.addressLine3
                loanResponse?.city = docResponse?.city
                loanResponse?.state = docResponse?.state
                loanResponse?.pincode = docResponse?.pincode
                loanResponse?.let { loanResponseData?.sendData(it) }
                (activity as UploadKycDetailsActivity?)?.selectIndex(3)
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