package com.example.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.arthanfinance.networkService.ApiClient
import com.example.arthanfinance.R
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream


/**
 * A simple [Fragment] subclass.
 * Use the [UploadPanCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadPanCardFragment : Fragment() {

    private lateinit var btnSubmit: Button
    private lateinit var btnPanCamera: Button
    private lateinit var customerName: TextView
    private lateinit var customerDOB: TextView
    private lateinit var customerPhoneNo: TextView
    private lateinit var apiClient: ApiClient
    private var loanResponse: LoanProcessResponse? = null
    private var REQ_CODE = 33
    private var MY_CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_upload_kyc_details, container, false)

        apiClient = ApiClient()
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnPanCamera = view.findViewById(R.id.btn_panCamera)
        customerName = view.findViewById(R.id.tv_name_value)
        customerDOB = view.findViewById(R.id.tv_dob_value)
        customerPhoneNo = view.findViewById(R.id.tv_mob_num_value)

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        customerName.text = loanResponse?.customerName
        customerPhoneNo.text = loanResponse?.mobileNo
        customerDOB.text = loanResponse?.dob

        btnPanCamera.setOnClickListener {
            if(requestPermission()) {
                launchCamera(REQ_CODE)
            }
        }

        btnSubmit.setOnClickListener {

            updatePanDetails()
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
                launchCamera(REQ_CODE)
                //Toast.makeText(activity?.applicationContext, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity?.applicationContext, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQ_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            panImage.setImageBitmap(bitmap)
            val encodedImageStr = encodeImageString(bitmap)

            print("base64 Stirng $encodedImageStr")
            uploadPanImage(encodedImageStr)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadPanImage(encodedImageStr: String?) {
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("idType", "PAN")
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val docResponse = response.body()
                Toast.makeText(
                    context,
                    "PAN Details Uploaded successfully",
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

    private fun updatePanDetails() {
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("customerName", loanResponse?.customerName)
        jsonObject.addProperty("mobileNo", loanResponse?.mobileNo)
        jsonObject.addProperty("dob", loanResponse?.dob)

        val context = activity?.applicationContext!!
        ApiClient().getApiService(context).updatePan(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val panDetailsUpdateResposne = response.body() as LoanProcessResponse
                if(panDetailsUpdateResposne.apiStatus != "200"){
                    Toast.makeText(
                        context,
                        panDetailsUpdateResposne.apiMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (panDetailsUpdateResposne.nextScreen == "CUST_IMG") {
                        (activity as UploadKycDetailsActivity?)?.selectIndex(1)
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

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}