package com.av.arthanfinance.applyLoan

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioStateLessSession
import `in`.digio.sdk.kyc.DigioTaskResponse
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextPaint
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.av.arthanfinance.R
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_demo.*
import kotlinx.android.synthetic.main.layout_adhar.*
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.graphics.BitmapFactory


class UploadAdharCardFragment : Fragment(), DigioResponseListener {
    private val AAADHAR_BACK = 103
    private val AAADHAR_FRONT = 104
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

    private var CROP_REQ_CODE_AADHAR_FP = 330
    private var CROP_REQ_CODE_AADHAR_BP = 340
    private val CROP_AAADHAR_BACK = 1030
    private val CROP_AAADHAR_FRONT = 1040

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
//            if(requestPermission()) {
//                launchCamera(REQ_CODE_AADHAR_FP)
//            }

            startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_FP)
                val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AB")
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_FRONT.jpg")
            }, REQ_CODE_AADHAR_FP)
        }

        btnCameraAadharBackPhoto.setOnClickListener {
//            if(requestPermission()) {
//                launchCamera(REQ_CODE_AADHAR_BP)
//            }

            startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_BP)
                val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AB")
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_BACK.jpg")
            }, REQ_CODE_AADHAR_BP)

        }

        btnOfflineKyc.setOnClickListener {
            getAadharDataFromDigio()
        }

        btnUploadFileAadharFrontPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), AAADHAR_FRONT)
        }

        btnUploadFileAadharBackPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), AAADHAR_BACK)
        }

        btnAadharFrontRetake.setOnClickListener {
//            if(requestPermission()) {
//                launchCamera(REQ_CODE_AADHAR_FP)
//            }
            startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_FP)
                val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AB")
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_FRONT.jpg")
            }, REQ_CODE_AADHAR_FP)
        }

        btnAadharBackRetake.setOnClickListener {
//            if(requestPermission()) {
//                launchCamera(REQ_CODE_AADHAR_BP)
//            }

            startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
                putExtra("doc_type", REQ_CODE_AADHAR_BP)
                val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "AB")
                // if (!dir.exists())
                dir.mkdirs()
                putExtra("FilePath", "${dir.absolutePath}/IMG_AADHAR_BACK.jpg")
            }, REQ_CODE_AADHAR_BP)
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

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW, false)
        isCreateFlow?.let {
            if (!it) {
                getKYCDetailsFromServer()
                disableEditFeature(view)
            }
        }
        btnNext.setOnClickListener {
//            if(aadharFrontImgView.tag == 1 || aadharBackImgView.tag == 2) {
//                Toast.makeText(activity?.applicationContext, "Please Upload aadhar front and back Images", Toast.LENGTH_LONG).show();
//            } else {
//            }
            if (!isCreateFlow!!) {
                (activity as UploadKycDetailsActivity?)?.selectIndex(3)
            } else
                uploadAadharData()
        }

        val paint: TextPaint = tvUploadAdhar.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(
            0f, 0f, width, tvUploadAdhar.textSize, resources.getColor(
                R.color.dark_orange2, activity?.theme
            ), resources.getColor(
                R.color.indigoBlue, activity?.theme
            ), Shader.TileMode.CLAMP
        )
        tvUploadAdhar.paint.shader = shader
        return view
    }


    private fun disableEditFeature(view: View) {
        btnCameraAadharBackPhoto.visibility = View.GONE
        btnCameraAadharFrontPhoto.visibility = View.GONE

        btnRemoveAadharFrontPhoto.visibility = View.GONE
        btnRemoveAadharBackPhoto.visibility = View.GONE

        btnUploadFileAadharFrontPhoto.visibility = View.GONE
        btnUploadFileAadharBackPhoto.visibility = View.GONE
        btnAadharFrontRetake.visibility = View.GONE
        btnAadharBackRetake.visibility = View.GONE
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
                Toast.makeText(
                    activity?.applicationContext,
                    "camera permission granted",
                    Toast.LENGTH_LONG
                ).show();
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

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_CODE_AADHAR_FP, REQ_CODE_AADHAR_BP -> {
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri)
                        .getIntent(requireContext())
                    if (requestCode == REQ_CODE_AADHAR_FP)
                        startActivityForResult(intent, CROP_REQ_CODE_AADHAR_FP)
                    else
                        startActivityForResult(intent, CROP_REQ_CODE_AADHAR_BP)
                }
                AAADHAR_FRONT, AAADHAR_BACK -> {
                    try {
                        val fileUri = data!!.data
                        val intent = CropImage.activity(fileUri)
                            .getIntent(requireContext())
                        if (requestCode == AAADHAR_FRONT)
                            startActivityForResult(intent, CROP_AAADHAR_FRONT)
                        else
                            startActivityForResult(intent, CROP_AAADHAR_BACK)
                    } catch (e: Exception) {
                        Log.e("Exception", e.toString())
                    }
                }

                CROP_REQ_CODE_AADHAR_FP, CROP_REQ_CODE_AADHAR_BP -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                    if (requestCode == CROP_REQ_CODE_AADHAR_FP) {
                        aadharFrontImgView.setImageBitmap(bitmap)
                        aadharFrontLayout.visibility = View.GONE
                        btnAadharFrontRetake.visibility = View.VISIBLE
                        btnRemoveAadharFrontPhoto.visibility = View.VISIBLE
                        aadharFrontImgView.tag = 3
                        val base64 = BitmapUtils.getBase64(bitmap)
                        uploadAadharImage(base64, "AF")
                    } else {
                        aadharBackImgView.setImageBitmap(bitmap)
                        aadharBackLayout.visibility = View.GONE
                        btnAadharBackRetake.visibility = View.VISIBLE
                        btnRemoveAadharBackPhoto.visibility = View.VISIBLE
                        aadharBackImgView.tag = 4
                        val base64 = BitmapUtils.getBase64(bitmap)
                        uploadAadharImage(base64, "AB")
                    }
                }
                CROP_AAADHAR_FRONT, CROP_AAADHAR_BACK -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver, resultUri)
                    val base64 = BitmapUtils.getBase64(bitmap)

                    if (requestCode == CROP_AAADHAR_FRONT) {
                        aadharFrontImgView.setImageBitmap(bitmap)
                        uploadAadharImage(base64, "AF")
                    } else {
                        aadharBackImgView.setImageBitmap(bitmap)
                        uploadAadharImage(base64, "AB")
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    private fun getFileName(contentResolver: ContentResolver?, uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        uri.let { returnUri ->
            contentResolver?.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            cursor.moveToFirst()
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
        return fileName
    }

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadAadharImage(encodedImageStr: String?, idType: String) {
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
        jsonObject.addProperty("idType", idType)
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                Toast.makeText(
                    context,
                    "Aadhar photo uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                uploadAadharData()
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

    private fun uploadAadharData() {
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

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).updateAadhar(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                val docResponse = response.body()
                loanResponse?.addressLine1 = docResponse?.addressLine1
                loanResponse?.addressLine2 = docResponse?.addressLine2
                loanResponse?.addressLine3 = docResponse?.addressLine3
                loanResponse?.city = docResponse?.city
                loanResponse?.state = docResponse?.state
                loanResponse?.pincode = docResponse?.pincode
                docResponse?.customerId?.let {
                    (activity as UploadKycDetailsActivity?)?.coAppCustId = it
                }
                loanResponse?.let {
                    loanResponseData?.sendData(it)
                }
                (activity as UploadKycDetailsActivity?)?.selectIndex(3)
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

    private fun getKYCDetailsFromServer() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRAadharDetails(
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
                val kycResponse = response.body()
                Glide
                    .with(this@UploadAdharCardFragment)
                    .load(kycResponse!!.aadharFrontUrl).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(aadharFrontImgView);
                Glide
                    .with(this@UploadAdharCardFragment)
                    .load(kycResponse.aadharBackUrl).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(aadharBackImgView);
            }
        })

    }

    private fun getAadharDataFromDigio() {
        val digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()

        val kycRequest = DigioTaskRequest()
        kycRequest.taskType = DigioTaskType.OFFLINE_KYC// For Aadhaar card
        kycRequest.isFaceMatch =
            false // Optional,  In case business required selfie and face match with Aadhaar
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

    override fun onDigioEventTracker(eventTracker: JSONObject) {

    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {

    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {

            val digioTaskRequest = digiTaskResponse.getTask()

            val taskType = digioTaskRequest.taskType
            if (taskType == DigioTaskType.OFFLINE_KYC) {
                val mainResponse =
                    digiTaskResponse.getResponse()// offline_kyc or idCard analysis response
                val analysisResponse = mainResponse.getJSONObject("offline_kyc_response")
                val aadharImage = mainResponse.getString("photo")
                val personalInformation = analysisResponse.getJSONObject("personal_information")
                val addressInformation = analysisResponse.getJSONObject("address_information")
                val decodedString: ByteArray = Base64.decode(aadharImage, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                val name = personalInformation.getString("name")
                val address = addressInformation.getString("text")

                adharImg.setImageBitmap(decodedByte)

                uploadAadharImage(aadharImage, "AF")
            }
        }
    }
}