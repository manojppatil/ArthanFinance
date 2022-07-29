package com.av.arthanfinance.applyLoan

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioStateLessSession
import `in`.digio.sdk.kyc.DigioTaskResponse
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.av.arthanfinance.R
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.JsonObject

import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class UploadPanCardFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    DigioResponseListener {

    private lateinit var customerName: EditText
    private lateinit var customerDOB: TextView
    private lateinit var customerPhoneNo: EditText
    private lateinit var apiClient: ApiClient
    private var loanResponse: LoanProcessResponse? = null
    private var REQ_CODE = 33
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var isCameraOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_upload_kyc_details, container, false)
        (activity as UploadKycDetailsActivity).setFormStatus(24)
        apiClient = ApiClient()
        customerName = view.findViewById(R.id.tv_name_value)
        customerDOB = view.findViewById(R.id.tv_dob_value)
        customerPhoneNo = view.findViewById(R.id.tv_mob_num_value)

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse
        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)

        isCreateFlow?.let {
            if (!it) {
                getKYCDetailsFromServer()
                disableEditFeature(view)
            }
        }
        return view
    }

    private fun getPanDataFromDigio() {
        val digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()

        val kycRequest = DigioTaskRequest()
        kycRequest.taskType = DigioTaskType.ID_ANALYSIS
        kycRequest.isFaceMatch = false // Optional,  In case business required selfie and face match with Aadhaar
        digioTaskList.add(kycRequest)

        val config = DigioKycConfig()
        config.setEnvironment(DigioEnvironment.SANDBOX)
        config.setPrimaryColor(Color.parseColor("#17c39b"))
        config.setSecondaryColor(Color.parseColor("#B4E9D8"))
        try {
            val digioStateLessSession = DigioStateLessSession()
            digioStateLessSession.init(
                activity as UploadKycDetailsActivity,
                config,
                "SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X",
                "SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB"
            )
            digioStateLessSession.startStateLessSession(digioTaskList, this)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun disableEditFeature(view: View) {
        view.findViewById<ImageView>(R.id.imgEdit).visibility = View.GONE
        view.findViewById<AppCompatTextView>(R.id.tv_upload_pan).setText("Uploaded PAN")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)
        /*if (isCreateFlow!!) {
            Handler(Looper.getMainLooper()).postDelayed({
                *//* Create an Intent that will start the Menu-Activity. *//*
                if (loanResponse!!.applicantType != "PA") {
                    rl_rect.visibility = View.VISIBLE
                    customerName.text.clear()
                    customerPhoneNo.text.clear()
                    customerDOB.setText("DD/MM/YYYY")

                } else {

                }
            }, 500)
        }*/

        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        if(applicantType.equals("CA")){
            rl_rect.visibility = View.VISIBLE
            customerName.text.clear()
            customerPhoneNo.text.clear()
            customerDOB.setText("DD/MM/YYYY")
        }
        customerDOB.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(
                    requireContext(), this, calendar.get(
                        Calendar.YEAR
                    ), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        btn_uploadDigioPan.setOnClickListener {
            if (requestPermission()) {
                getPanDataFromDigio()
            }
        }

    }

    private fun getKYCDetailsFromServer() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRPanDetails(
            loanResponse!!.loanId!!,
            loanResponse!!.applicantType
        ).enqueue(object :
            Callback<LoanKYCDetailsResponse> {
            override fun onFailure(call: Call<LoanKYCDetailsResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<LoanKYCDetailsResponse>,
                response: Response<LoanKYCDetailsResponse>
            ) {
                if (response.code() != 200) {
                    return
                }
                val kycResponse = response.body()
                Glide
                    .with(this@UploadPanCardFragment)
                    .load(kycResponse!!.panUrl).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(digioPanImage);
                kycResponse.customerName?.let {
                    customerName.setText(it)
                }
                kycResponse.mobileNo?.let {
                    customerPhoneNo.setText(it)
                }
                kycResponse.dob?.let {
                    customerDOB.setText(it)
                }
                customerName.isEnabled = false
                customerDOB.isEnabled = false
                customerPhoneNo.isEnabled = false
            }
        })
    }

    private var REQ_CODE_PAN_ID = 322

    private fun launchCamera() {
        isCameraOpen = true
        startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PAN_ID)
            val dir =
                File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ArthurFinance")
            // if (!dir.exists())
            dir.mkdirs()
            val time = System.currentTimeMillis().toString()
            putExtra("FilePath", "${dir.absolutePath}/IMG_PAN_ID_${time}.jpg")
        }, REQ_CODE_PAN_ID)
    }

    private fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED && activity?.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED && activity?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
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
                getPanDataFromDigio()
            } else {
                Toast.makeText(
                    activity?.applicationContext,
                    "camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private fun uploadPanImage(encodedImageStr: String?) {
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if ((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        if (applicantType.equals("CA")){
            custId = "NA"
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("idType", "PAN")
        jsonObject.addProperty("imageBase64", encodedImageStr)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val docResponse = response.body()
                docResponse?.customerId?.let {
                    (activity as UploadKycDetailsActivity?)?.coAppCustId = it
                }
                Toast.makeText(
                    context,
                    "PAN Details Uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                updatePanDetails()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as UploadKycDetailsActivity).hideProgressDialog()
            }
        })
    }

    private fun updatePanDetails() {
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if ((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        (activity as UploadKycDetailsActivity).showProgressDialog()

        val jsonObject = JsonObject()

        if (applicantType.equals("PA")) {
            jsonObject.addProperty("loanId", loanResponse?.loanId)
            jsonObject.addProperty("customerId", custId)
            jsonObject.addProperty("applicantType", applicantType)
            jsonObject.addProperty("customerName", loanResponse?.customerName)
            jsonObject.addProperty("mobileNo", loanResponse?.mobileNo)
            jsonObject.addProperty("dob", loanResponse?.dob)
        } else {
            if (customerName.text.toString() == ""){
                Toast.makeText(context, "Please Provide Co-Applicant's Name", Toast.LENGTH_SHORT).show()
            } else if (customerPhoneNo.text.toString() == ""){
                Toast.makeText(context, "Please Provide Co-Applicant's Mobile Number", Toast.LENGTH_SHORT).show()
            }else if (customerPhoneNo.text.toString().length < 10){
                Toast.makeText(context, "Please Provide A Valid Mobile Number", Toast.LENGTH_SHORT).show()
            }else if (customerDOB.text.toString() == ""){
                Toast.makeText(context, "Please Provide Co-Applicant's DOB", Toast.LENGTH_SHORT).show()
            }else{
                jsonObject.addProperty("loanId", loanResponse?.loanId)
                jsonObject.addProperty("customerId", custId)
                jsonObject.addProperty("applicantType", applicantType)
                jsonObject.addProperty("customerName", customerName.text.toString())
                jsonObject.addProperty("mobileNo", customerPhoneNo.text.toString())
                jsonObject.addProperty("dob", customerDOB.text.toString())
            }
        }

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).updatePan(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                if (response.code() != 200) {
                    return
                }
                val panDetailsUpdateResposne = response.body() as LoanProcessResponse
                if (panDetailsUpdateResposne.apiStatus != "200") {
                    Toast.makeText(
                        context,
                        panDetailsUpdateResposne.apiMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (panDetailsUpdateResposne.nextScreen == "CUST_IMG") {
                        (activity as UploadKycDetailsActivity).coAppCustId = panDetailsUpdateResposne.customerId!!
                        (activity as UploadKycDetailsActivity).selectIndex(1)
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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        customerDOB.setText("${dayOfMonth}/${month + 1}/${year}")
    }

    override fun onDigioEventTracker(eventTracker: JSONObject) {

    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
        for (digiTaskResponse in failure) {
            val digioTaskRequest = digiTaskResponse.getResponse()
            val msg = digioTaskRequest.getString("message")
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {

            val digioTaskRequest = digiTaskResponse.getTask()

            val taskType = digioTaskRequest.taskType
            if (taskType == DigioTaskType.ID_ANALYSIS) {
                val mainResponse = digiTaskResponse.getResponse()// offline_kyc or idCard analysis response
                val analysisResponse = mainResponse.getJSONObject("analysis_response")
                val name = analysisResponse.getString("name")
                val panNumber = analysisResponse.getString("id_no")
                val image = analysisResponse.getString("encoded_image")
                val panImg = mainResponse.getString("id_front_uri")

                //Glide.with(this).load(panImg).into(digioPanImage)

                Glide.with(this)
                    .asBitmap()
                    .load(panImg)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            digioPanImage.setImageBitmap(resource)
                            val encodedImageStr = encodeImageString(resource)
                            uploadPanImage(encodedImageStr)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // this is called when imageView is cleared on lifecycle call or for
                            // some other reason.
                            // if you are referencing the bitmap somewhere else too other than this imageView
                            // clear it here as you can no longer have the bitmap
                        }
                    })

                /*val encodedImageStr = encodeImageString(panImg)
                uploadPanImage(encodedImageStr)*/
            }
        }
    }

    private fun encodeString(str: String): String? {
        val data: ByteArray
        var base64 = ""
        try {
            val decodedString: ByteArray = Base64.decode(str, Base64.DEFAULT)
            data = str.toByteArray(charset("UTF-8"))
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            //val byteArrayOutputStream = ByteArrayOutputStream()
            base64 = Base64.encodeToString(data, Base64.DEFAULT)

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return base64
    }


}