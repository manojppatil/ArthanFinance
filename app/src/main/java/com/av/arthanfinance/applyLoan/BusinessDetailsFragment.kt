package com.av.arthanfinance.applyLoan

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.arthanfinance.core.util.FiniculeUtil
import com.av.arthanfinance.R
import com.av.arthanfinance.models.BusinessDetails
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_business_details.*
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BusinessDetailsFragment : Fragment() {
    private lateinit var btnNext: Button
    private lateinit var apiClient: ApiClient
    private lateinit var businessName: EditText
    private lateinit var constitutionSpiner: Spinner
    private lateinit var typeSpinner: Spinner

    private lateinit var categorySpiner: Spinner
    private lateinit var segmentSpinner: Spinner
    private lateinit var dateOfIncorporation: EditText
    private lateinit var panNum: EditText
    private lateinit var udhyogAadhar: EditText
    private lateinit var ssiNum: EditText
    private lateinit var gstNum: EditText
    private lateinit var turnOver: EditText
    private lateinit var income: EditText
    private lateinit var expense: EditText
    private lateinit var address: EditText
    private lateinit var percentage: EditText
    private lateinit var radioGroup: RadioGroup
    private var loanResponse: LoanProcessResponse? = null
    private var constitutionList = arrayOf("Individual", "Sole Proprietorship", "Partnership")
    private var typeList = arrayOf("Manufacturing", "wholesaler", "Retailer","Service")
    private var categoryList = ArrayList<String>()
    private var segmentList = ArrayList<String>()
    private var categories = ArrayList<Category>()
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQUEST_CODE_GALLERY = 101
    private var CROP_REQUEST_CODE_GALLERY = 102
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var REQ_CODE_PAN_ID = 322

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business_details, container, false)
        btnNext = view.findViewById(R.id.btn_next_in_businessDetails)
        constitutionSpiner = view.findViewById(R.id.constitutionSpiner)
        typeSpinner = view.findViewById(R.id.typeSpinner)
        categorySpiner = view.findViewById(R.id.categorySpinner)
        segmentSpinner = view.findViewById(R.id.segmentSpinner)
        dateOfIncorporation = view.findViewById(R.id.edt_dateOfIncorporation)
        businessName = view.findViewById(R.id.edt_bus_name)
        panNum = view.findViewById(R.id.edt_bus_pan_num)
        udhyogAadhar = view.findViewById(R.id.edt_udhyog)
        ssiNum = view.findViewById(R.id.edt_ssi_num)
        gstNum = view.findViewById(R.id.edt_gst_num)
        turnOver = view.findViewById(R.id.edt_turnover)
        income = view.findViewById(R.id.edt_income)
        expense = view.findViewById(R.id.edt_expenses)
        address = view.findViewById(R.id.edt_address)
        percentage = view.findViewById(R.id.edt_percentage)
        radioGroup = view.findViewById(R.id.radio_group)
        apiClient = ApiClient()

        val mPrefs: SharedPreferences? = requireActivity()!!.getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("categoriesList", null)
        if(json != null) {//ArrayList<Category>
            val obj: Categories? = gson.fromJson(json, Categories::class.java)

            categories = obj?.categories!!
            categoryList.clear()
            for(category: Category in categories) {
                categoryList.add(category.categoryDesc)
            }
        }
        val constitutionAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, constitutionList) }
        constitutionSpiner.adapter = constitutionAdapter

        constitutionSpiner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val typesAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, typeList) }
        typeSpinner.adapter = typesAdapter

        typeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val categoryAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, categoryList) }
        categorySpiner.adapter = categoryAdapter
        categorySpiner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                val category = categories[position]//categoryList[position]
                segmentList = category.segments as ArrayList<String>

                val segmentAdapter = activity?.let { ArrayAdapter(it,
                    R.layout.emi_options, segmentList) }
                segmentSpinner.adapter = segmentAdapter
                segmentSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View?, position: Int, id: Long) {

                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        dateOfIncorporation.setOnClickListener {
            view.clearFocus()
            hideSoftKeyboard(requireActivity(),it)
            val picker: DialogFragment = DatePickerFragment()
            picker.show(requireFragmentManager(), "datePicker")
        }

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW,false)
        isCreateFlow?.let {
            if (!it){
                getBusinessDetailsFromServer()
                disableEditFeature()
            }
        }
        btnNext.setOnClickListener {
            if (!isCreateFlow!!){
                (activity as UploadKycDetailsActivity?)?.selectIndex(5)
            }else
                 saveCustomerBusinessData()
        }

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.radio_yes){
                address.visibility = View.GONE
            }else{
                address.visibility = View.VISIBLE
            }
        }

        view.findViewById<Button>(R.id.btn_camera).setOnClickListener {
            if(requestPermission()) {
                launchCamera()
            }
        }
        view.findViewById<Button>(R.id.btn_upload).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
        }

        return view
    }


    private fun disableEditFeature() {
        constitutionSpiner.isEnabled = false
        typeSpinner.isEnabled = false
        categorySpiner.isEnabled = false
        segmentSpinner.isEnabled = false
        dateOfIncorporation.isEnabled = false
        businessName.isEnabled = false
        panNum.isEnabled = false
        udhyogAadhar.isEnabled = false
        ssiNum.isEnabled = false
        gstNum.isEnabled = false
        turnOver.isEnabled = false
        income.isEnabled = false
        expense.isEnabled = false
        address.isEnabled = false
        percentage.isEnabled = false
    }
    private fun getBusinessDetailsFromServer() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRBusinessDetails(loanResponse!!.loanId!!).enqueue(object :
            Callback<BusinessDetails> {
            override fun onFailure(call: Call<BusinessDetails>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<BusinessDetails>,
                response: Response<BusinessDetails>
            ) {
                val businessDetails = response.body()
                businessDetails?.businessName?.let {
                    businessName.setText(it)
                }
                businessDetails?.dateOfIncorp?.let {
                    dateOfIncorporation.setText(it)
                }
                businessDetails?.constitution?.let {
                    for (index in constitutionList.indices){
                        if (constitutionList[index].equals(it,true)){
                            constitutionSpiner.setSelection(index,false)
                            break
                        }
                    }
                }
                businessDetails?.category?.let {
                    for (index in categoryList.indices){
                        if (categoryList[index].equals(it,true)){
                            categorySpiner.setSelection(index,false)
                            break
                        }
                    }
                }
                businessDetails?.segment?.let {
                    for (index in segmentList.indices){
                        if (segmentList[index].equals(it,true)){
                            segmentSpinner.setSelection(index,false)
                            break
                        }
                    }
                }
                businessDetails?.type?.let {
                    for (index in typeList.indices){
                        if (typeList[index].equals(it,true)){
                            typeSpinner.setSelection(index,false)
                            break
                        }
                    }
                }
                businessDetails?.businessPan?.let {
                    panNum.setText(it)
                }
                businessDetails?.udhyogAadhar?.let {
                    udhyogAadhar.setText(it)
                }
                businessDetails?.gstNo?.let {
                    gstNum.setText(it)
                }
                businessDetails?.turnover?.let {
                    turnOver.setText(it)
                }
                businessDetails?.income?.let {
                    income.setText(it)
                }
                businessDetails?.expenses?.let {
                    expense.setText(it)
                }
                businessDetails?.businessAddress?.let {
                    address.setText(it)
                }
                businessDetails?.margin?.let {
                    percentage.setText(it.toString())
                }
                businessDetails?.businessImage.let {
                    Glide
                        .with(this@BusinessDetailsFragment)
                        .load(it).fitCenter()
                        .placeholder(R.drawable.ic_arthan_logo)
                        .into(businessImage);
                }
            }
        })
    }



    fun hideSoftKeyboard(activity: Activity, view: View) {
        val imm: InputMethodManager =activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    private fun saveCustomerBusinessData() {

        if ( (activity as UploadKycDetailsActivity).currentLocation == null){
            FiniculeUtil.showGPSNotEnabledDialog(requireContext())
            return
        }

        val jsonObject = JsonObject()
        (activity as UploadKycDetailsActivity).showProgressDialog()

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("businessName", businessName.text.toString())
        jsonObject.addProperty("constitution", constitutionSpiner.selectedItem.toString())
        jsonObject.addProperty("category", categorySpiner.selectedItem.toString())
        jsonObject.addProperty("segment", segmentSpinner.selectedItem.toString())
        jsonObject.addProperty("type", typeSpinner.selectedItem.toString())
        jsonObject.addProperty("businessPan", panNum.text.toString())
        jsonObject.addProperty("udhyogAadhar", udhyogAadhar.text.toString())
        jsonObject.addProperty("gstNo", gstNum.text.toString())
        jsonObject.addProperty("turnover", turnOver.text.toString())
        jsonObject.addProperty("income", income.text.toString())
        jsonObject.addProperty("expenses", expense.text.toString())
        jsonObject.addProperty("businessAddress", address.text.toString())
        jsonObject.addProperty("margin", percentage.text.toString())
        jsonObject.addProperty("addressType",address.visibility == View.INVISIBLE)
        jsonObject.addProperty("dateOfIncorporation",dateOfIncorporation.text.toString())
        (activity as UploadKycDetailsActivity).currentLocation?.latitude?.let {
            jsonObject.addProperty("lat",it )
        }
        (activity as UploadKycDetailsActivity).currentLocation?.longitude?.let {
            jsonObject.addProperty("lng",it)
        }
        val context = activity?.applicationContext
        if (context != null) {
            ApiClient().getAuthApiService(context).saveCustBusiness(jsonObject).enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>
                ) {
                    (activity as UploadKycDetailsActivity).hideProgressDialog()
                    (activity as UploadKycDetailsActivity?)?.selectIndex(5)
//                        val fragment =
//                            UploadDocsFragment()
//                        val fragmentManger = activity?.supportFragmentManager
//                        val transaction = fragmentManger?.beginTransaction()
//                        val args = Bundle()
//                        args.putString("loanId", loanResponse?.loanId)
//                        args.putString("customerId", loanResponse?.customerId)
//                        fragment.setArguments(args)
//
//                        transaction?.setCustomAnimations(
//                            R.anim.enter_from_right,
//                            R.anim.exit_to_left,
//                            R.anim.enter_from_left,
//                            R.anim.exit_to_right
//                        )
//                        transaction?.replace(R.id.container, fragment)
//                        transaction?.addToBackStack(null)
//                        transaction?.commit()
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    t.printStackTrace()
                    (activity as UploadKycDetailsActivity).hideProgressDialog()
                    Toast.makeText(
                        context,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        LENGTH_SHORT
                    ).show()
                }
            })
        }
//        }
    }

    private fun launchCamera() {
        startActivityForResult(Intent(activity, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PAN_ID)
            val dir = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ArthurFinance")
            // if (!dir.exists())
            dir.mkdirs()
            val time =System.currentTimeMillis().toString()
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

        if (resultCode == Activity.RESULT_OK){

            when(requestCode){
                REQ_CODE_PAN_ID->{
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    val intent = CropImage.activity(fileUri)
                        .getIntent(requireContext())
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
                    businessImage.setImageBitmap(bitmap)
                    val encodedImageStr = encodeImageString(bitmap)
                    print("base64 Stirng $encodedImageStr")
                    uploadBusinessCard(encodedImageStr)
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadBusinessCard(encodedImageStr: String?) {
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("idType", "BUSINESS_IMG")
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
                    "Business Card Uploaded successfully",
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

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}

open class DatePickerFragment : DialogFragment(),
    OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
// Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

// Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c[year, month] = day
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate: String = sdf.format(c.time)
        (requireActivity().findViewById(R.id.edt_dateOfIncorporation) as EditText).setText(formattedDate)
    }
}