package com.av.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.av.arthanfinance.R
import com.av.arthanfinance.networkService.ApiClient
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


/**
 * A simple [Fragment] subclass.
 * Use the [UploadPanCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UploadPanCardFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var btnSubmit: Button
    private lateinit var btnPanCamera: Button
    private lateinit var customerName: EditText
    private lateinit var customerDOB: EditText
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

        apiClient = ApiClient()
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnPanCamera = view.findViewById(R.id.btn_panCamera)
        customerName = view.findViewById(R.id.tv_name_value)
        customerDOB = view.findViewById(R.id.tv_dob_value)
        customerPhoneNo = view.findViewById(R.id.tv_mob_num_value)

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)

        btnPanCamera.setOnClickListener {
            if (requestPermission()) {
                launchCamera()
            }
        }
        view.findViewById<Button>(R.id.btn_uploadPan).setOnClickListener {
            isCameraOpen = true
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
        }
        isCreateFlow?.let {
            if (!it) {
                getKYCDetailsFromServer()
                disableEditFeature(view)
            }
        }
        btnSubmit.setOnClickListener {
            if (!isCreateFlow!!) {
                (activity as UploadKycDetailsActivity?)?.selectIndex(1)
            } else
                updatePanDetails()
        }

        return view
    }

    private fun disableEditFeature(view: View) {
        view.findViewById<ImageView>(R.id.imgEdit).visibility = View.GONE
        view.findViewById<AppCompatButton>(R.id.btn_uploadPan).visibility = View.GONE
        view.findViewById<AppCompatButton>(R.id.btn_panCamera).visibility = View.GONE
        view.findViewById<AppCompatTextView>(R.id.tv_upload_pan).setText("Uploaded PAN")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)
        if (isCreateFlow!!) {
            Handler(Looper.getMainLooper()).postDelayed({
                /* Create an Intent that will start the Menu-Activity. */
                if (loanResponse!!.applicantType == "PA") {
                    customerName.setText(loanResponse?.customerName)
                    customerPhoneNo.setText(loanResponse?.mobileNo)
                    customerDOB.setText(loanResponse?.dob)
                    customerName.isEnabled = false
                    customerDOB.isEnabled = false
                    customerPhoneNo.isEnabled = false
                } else {
                    customerName.text.clear()
                    customerPhoneNo.text.clear()
                    customerDOB.text.clear()
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
                }
            }, 500)
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
                    .into(panImage);
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
                launchCamera()
                //Toast.makeText(activity?.applicationContext, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(
                    activity?.applicationContext,
                    "camera permission denied",
                    Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        isCameraOpen = false
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                REQ_CODE_PAN_ID -> {
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri)
                        .getIntent(requireContext())
                    startActivityForResult(intent, CROP_REQUEST_CODE_CAMERA)
                }
                REQUEST_CODE_GALLERY -> {
                    try {
                        val fileUri = data!!.data
                        val intent = CropImage.activity(fileUri)
                            .getIntent(requireContext())
                        startActivityForResult(intent, CROP_REQUEST_CODE_GALLERY)
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                }
                CROP_REQUEST_CODE_CAMERA, CROP_REQUEST_CODE_GALLERY -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                    panImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    print("base64 Stirng $encodedImageStr")
                    uploadPanImage(encodedImageStr)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun uploadPanImage(encodedImageStr: String?) {
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if ((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
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
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if ((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("customerName", customerName.text.toString())
        jsonObject.addProperty("mobileNo", customerPhoneNo.text.toString())
        jsonObject.addProperty("dob", customerDOB.text.toString())

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
                        (activity as UploadKycDetailsActivity).coAppCustId =
                            panDetailsUpdateResposne.customerId!!
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

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        customerDOB.setText("${dayOfMonth}/${month + 1}/${year}")
    }
}