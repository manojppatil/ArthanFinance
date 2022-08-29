package com.av.arthanfinance.user_kyc

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.*
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.databinding.ActivityUploadBusinessDetailsBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.copyFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_business_details.*
import kotlinx.android.synthetic.main.layout_upload_kyc_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class UploadBusinessDetailsActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var activityUploadBusinessDetailsBinding: ActivityUploadBusinessDetailsBinding
    private var constitutionList =
        arrayOf("Select", "Individual", "Sole Proprietorship", "Partnership")
    private var typeList = arrayOf("Select", "Manufacturing", "wholesaler", "Retailer", "Service")

    /* private var categoryList1 = arrayOf("Automobile", "General Products", "Medicine", "Hotel Restaurant")
     private var segmentList1 = arrayOf("Spare parts", "Kirana Store", "Medicine Shop", "Fast Food ")*/
    private var categoryList = ArrayList<String>()
    private var segmentList = ArrayList<String>()
    private var categories = ArrayList<Category>()
    private var kycCompleteStatus = "80"

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

    private var REQ_CODE_BANK_STATEMENT = 444

    var loanResponse: LoanProcessResponse? = null
    var customerData: CustomerHomeTabResponse? = null

    private lateinit var locationAddress: String
    private lateinit var subLocality: String
    private lateinit var locationCity: String
    private lateinit var locationState: String
    private lateinit var locationCountry: String
    private lateinit var locationPostalCode: String

    private var mLoanId: String? = null
    private var mCustomerId: String? = null

    override val layoutId: Int
        get() = R.layout.activity_upload_business_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadBusinessDetailsBinding =
            ActivityUploadBusinessDetailsBinding.inflate(layoutInflater)
        setContentView(activityUploadBusinessDetailsBinding.root)

        val mPrefs: SharedPreferences? =
            this.getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("categoriesList", null)

//        if (intent.hasExtra("loanResponse")) {
//            loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse
//        }

        if (intent.hasExtra("customerData")) {
            customerData = intent.getSerializableExtra("customerData") as CustomerHomeTabResponse
        }

        /*if (json != null) {//ArrayList<Category>
            val obj: Categories? = gson.fromJson(json, Categories::class.java)

            categories = obj?.categories!!
            categoryList.clear()
            for (category: Category in categories) {
                categoryList.add(category.categoryDesc)
            }
        }*/

        activityUploadBusinessDetailsBinding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (i == R.id.radio_yes) {
                activityUploadBusinessDetailsBinding.tilAddress.visibility = View.GONE
            } else {
                activityUploadBusinessDetailsBinding.tilAddress.visibility = View.VISIBLE
            }
        }

        activityUploadBusinessDetailsBinding.edtDateOfIncorporation.setOnClickListener {
            hideSoftKeyboard(this, it)
            activityUploadBusinessDetailsBinding.edtDateOfIncorporation.clearFocus()
            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this, this, calendar.get(
                    Calendar.YEAR
                ), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        val constitutionAdapter = this.let {
            ArrayAdapter(
                it,
                R.layout.emi_options, constitutionList
            )
        }
        activityUploadBusinessDetailsBinding.constitutionSpiner.adapter = constitutionAdapter

        activityUploadBusinessDetailsBinding.constitutionSpiner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val typesAdapter = this.let {
            ArrayAdapter(
                it,
                R.layout.emi_options, typeList
            )
        }
        activityUploadBusinessDetailsBinding.typeSpinner.adapter = typesAdapter

        activityUploadBusinessDetailsBinding.typeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        /* val categoryAdapter = ArrayAdapter(
             this,
             R.layout.emi_options, categoryList
         )

         activityUploadBusinessDetailsBinding.categorySpinner.adapter = categoryAdapter*/
        activityUploadBusinessDetailsBinding.categorySpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                val category = categories[position]
                segmentList = category.segments as ArrayList<String>

                val segmentAdapter = this@UploadBusinessDetailsActivity.let {
                    ArrayAdapter(
                        it,
                        R.layout.emi_options, segmentList
                    )
                }
                activityUploadBusinessDetailsBinding.segmentSpinner.adapter = segmentAdapter
                activityUploadBusinessDetailsBinding.segmentSpinner.onItemSelectedListener =
                    object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?, position: Int, id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                        }
                    }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        activityUploadBusinessDetailsBinding.tvUdyamlink.setOnClickListener {
            val url: String = "https://udyamregistration.gov.in/UdyamRegistration.aspx"
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }

//        activityUploadBusinessDetailsBinding.btnUpload.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "*/*"
//            startActivityForResult(Intent.createChooser(intent, "ChooseFile"), REQUEST_CODE_GALLERY)
//        }
//
//        activityUploadBusinessDetailsBinding.btnCamera.setOnClickListener {
//            if (requestPermission(REQ_CODE_CAMERA)) {
//                launchCamera(REQ_CODE_CAMERA)
//            }
//        }
//
//        activityUploadBusinessDetailsBinding.btnUploadOwnerPhoto.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "*/*"
//            startActivityForResult(
//                Intent.createChooser(intent, "ChooseFile"),
//                REQUEST_CODE_GALLERY_SHOP_OWNER
//            )
//        }
//
//        activityUploadBusinessDetailsBinding.btnCameraOwnerPhoto.setOnClickListener {
//            if (requestPermission(REQ_CODE_CAMERA_SHOP_OWNER)) {
//                launchCamera(REQ_CODE_CAMERA_SHOP_OWNER)
//            }
//        }
//
//        activityUploadBusinessDetailsBinding.btnUploadStockPhoto.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "*/*"
//            startActivityForResult(
//                Intent.createChooser(intent, "ChooseFile"),
//                REQUEST_CODE_GALLERY_STOCK
//            )
//        }
//
//        activityUploadBusinessDetailsBinding.btnCameraStock.setOnClickListener {
//            if (requestPermission(REQ_CODE_CAMERA_STOCK)) {
//                launchCamera(REQ_CODE_CAMERA_STOCK)
//            }
//        }
//
//        activityUploadBusinessDetailsBinding.btnUploadShopLocality.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "*/*"
//            startActivityForResult(
//                Intent.createChooser(intent, "ChooseFile"),
//                REQUEST_CODE_GALLERY_SHOP_LOCALITY
//            )
//        }
//
//        activityUploadBusinessDetailsBinding.btnCameraShopLocality.setOnClickListener {
//            if (requestPermission(REQ_CODE_CAMERA_SHOP_LOCALITY)) {
//                launchCamera(REQ_CODE_CAMERA_SHOP_LOCALITY)
//            }
//        }

        val mapActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == 101) {
                val data = result.data
                if (data != null) {

                    locationAddress = data.getStringExtra("address").toString()
                    subLocality = data.getStringExtra("sub_locality").toString()
                    locationCity = data.getStringExtra("city").toString()
                    locationState = data.getStringExtra("state").toString()
                    locationPostalCode = data.getStringExtra("postalCode").toString()

                    activityUploadBusinessDetailsBinding.tvLocationAddress.text = locationAddress
                }
            }
        }

        val gstActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == 102) {
                saveCustomerBusinessData()
            }
        }

        activityUploadBusinessDetailsBinding.clLocation.setOnClickListener {
            val intent = Intent(this, MapLocationActivity::class.java)
            mapActivityResultLauncher.launch(intent)
        }

        activityUploadBusinessDetailsBinding.btnAadharDetails.setOnClickListener {
            if (hasValidAadharDetailsData()) {
                val gstIn = activityUploadBusinessDetailsBinding.tieGst.text.toString()
                if (gstIn == "") {
                    saveCustomerBusinessData()
                } else {
                    val intent =
                        Intent(this, GstValidationActivity::class.java).putExtra("gst_num", gstIn)
                            .putExtra("customerId", customerData!!.customerId)
                    gstActivityResultLauncher.launch(intent)
                }
            }
        }
//        activityUploadBusinessDetailsBinding.btnUploadbankstmt.setOnClickListener {
//            val pdfPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
//            pdfPickerIntent.type = "application/pdf"
//            startActivityForResult(
//                Intent.createChooser(pdfPickerIntent, "Choose File"),
//                REQ_CODE_BANK_STATEMENT
//            )
//        }

        activityUploadBusinessDetailsBinding.btnVerifyUdyam.setOnClickListener {
            val jsonObject = JsonObject()
            showProgressDialog()

            jsonObject.addProperty("consent", "Y")
            jsonObject.addProperty(
                "udyamRegistrationNo",
                activityUploadBusinessDetailsBinding.tieUdhyogAadharNo.text.toString()
            )
            jsonObject.addProperty("isPDFRequired", "N")
            Log.d("UdyamReq", jsonObject.toString())

            val clientId = "8jn2bAml0S4xcorY5EQM"

            ApiClient().getUdyamApiService(this).verifyUdyamAadhar(clientId, jsonObject)
                .enqueue(object :
                    Callback<UdyamDetailsResponse> {
                    override fun onResponse(
                        call: Call<UdyamDetailsResponse>,
                        response: Response<UdyamDetailsResponse>
                    ) {
                        hideProgressDialog()
                        try {
                            val udyamReturnResponse = response.body()
                            val statusCode = udyamReturnResponse!!.statusCode
                            if (statusCode == 101) {
                                changeVisibility()
                                val requestId = udyamReturnResponse.requestId
                                Log.e("UdyamRID", requestId.toString())
                                activityUploadBusinessDetailsBinding.tieBusinessName.setText(
                                    udyamReturnResponse.result!!.profile!!.name.toString()
                                )
                                activityUploadBusinessDetailsBinding.edtDateOfIncorporation.setText(
                                    udyamReturnResponse.result!!.profile!!.dateOfIncorporation.toString()
                                )
                                activityUploadBusinessDetailsBinding.tieConstitution.setText(
                                    udyamReturnResponse.result!!.profile!!.organizationType.toString()
                                )
                                activityUploadBusinessDetailsBinding.tieCategory.setText(
                                    udyamReturnResponse.result!!.industry[0].industry.toString()
                                )
                                activityUploadBusinessDetailsBinding.tieSegment.setText(
                                    udyamReturnResponse.result!!.industry[0].subSector.toString()
                                )
                                activityUploadBusinessDetailsBinding.tieType.setText(
                                    udyamReturnResponse.result!!.industry[0].activity.toString()
                                )
                                activityUploadBusinessDetailsBinding.tieBusinessPan.setText(
                                    udyamReturnResponse.result!!.profile!!.pan.toString()
                                )
                                val udyamAddress =
                                    udyamReturnResponse.result!!.officialAddress!!.flat + "," + udyamReturnResponse.result!!.officialAddress!!.premises + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.village + "," + udyamReturnResponse.result!!.officialAddress!!.block + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.road + "," + udyamReturnResponse.result!!.officialAddress!!.city + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.state + "," + udyamReturnResponse.result!!.officialAddress!!.pincode + "," +
                                            udyamReturnResponse.result!!.officialAddress!!.district
                                activityUploadBusinessDetailsBinding.tieBusinessAddress.setText(
                                    udyamAddress
                                )

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
                            this@UploadBusinessDetailsActivity,
                            "Service Failure, Once Network connection is stable, will try to resend again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

        }
        getCategoryAndSegmentData()
    }

    private fun changeVisibility() {
        activityUploadBusinessDetailsBinding.constitutionSpiner.visibility = View.GONE
        activityUploadBusinessDetailsBinding.categorySpinner.visibility = View.GONE
        activityUploadBusinessDetailsBinding.segmentSpinner.visibility = View.GONE
        activityUploadBusinessDetailsBinding.typeSpinner.visibility = View.GONE
        activityUploadBusinessDetailsBinding.tvCategory.visibility = View.GONE
        activityUploadBusinessDetailsBinding.tvConst.visibility = View.GONE
        activityUploadBusinessDetailsBinding.tvSegment.visibility = View.GONE
        activityUploadBusinessDetailsBinding.tvType.visibility = View.GONE

        activityUploadBusinessDetailsBinding.tilConstitution.visibility = View.VISIBLE
        activityUploadBusinessDetailsBinding.tilCategory.visibility = View.VISIBLE
        activityUploadBusinessDetailsBinding.tilSegment.visibility = View.VISIBLE
        activityUploadBusinessDetailsBinding.tilType.visibility = View.VISIBLE
    }

    private fun getCategoryAndSegmentData() {
        showProgressDialog()
        ApiClient().getAuthApiService(this).getCategorySegment().enqueue(
            object :
                Callback<Categories> {
                override fun onFailure(call: Call<Categories>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                }

                override fun onResponse(
                    call: Call<Categories>,
                    response: Response<Categories>
                ) {
                    hideProgressDialog()
                    val custData = response.body()
                    val sharedPref: SharedPreferences? = getSharedPreferences(
                        "categoriesList",
                        Context.MODE_PRIVATE
                    )
                    val prefsEditor = sharedPref?.edit()
                    val gson = Gson()
                    val json: String = gson.toJson(custData)
                    prefsEditor?.putString("categoriesList", "")
                    prefsEditor?.putString("categoriesList", json)
                    prefsEditor?.apply()

                    categories = custData?.categories!!
                    categoryList.clear()
                    for (category: Category in categories) {
                        categoryList.add(category.categoryDesc)
                    }

                    loadAdapter(categoryList)
                }

            })
    }

    private fun loadAdapter(categoryList: java.util.ArrayList<String>) {
        val categoryAdapter = ArrayAdapter(
            this,
            R.layout.emi_options, categoryList
        )

        activityUploadBusinessDetailsBinding.categorySpinner.adapter = categoryAdapter
    }

    private fun saveCustomerBusinessData() {

        val jsonObject = JsonObject()
        showProgressDialog()

//        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", customerData?.customerId)
        jsonObject.addProperty(
            "businessName",
            activityUploadBusinessDetailsBinding.tieBusinessName.text.toString()
        )
        jsonObject.addProperty(
            "constitution",
            activityUploadBusinessDetailsBinding.constitutionSpiner.selectedItem.toString()
        )
        jsonObject.addProperty(
            "category",
            activityUploadBusinessDetailsBinding.categorySpinner.selectedItem.toString()
        )
        jsonObject.addProperty(
            "segment",
            activityUploadBusinessDetailsBinding.segmentSpinner.selectedItem.toString()
        )
        jsonObject.addProperty(
            "type",
            activityUploadBusinessDetailsBinding.typeSpinner.selectedItem.toString()
        )
        jsonObject.addProperty(
            "businessPan",
            activityUploadBusinessDetailsBinding.tieBusinessPan.text.toString()
        )
        jsonObject.addProperty(
            "udhyogAadhar",
            activityUploadBusinessDetailsBinding.tieUdhyogAadharNo.text.toString()
        )
        jsonObject.addProperty("gstNo", activityUploadBusinessDetailsBinding.tieGst.text.toString())
        jsonObject.addProperty(
            "turnover",
            activityUploadBusinessDetailsBinding.tieMonthlyTurnover.text.toString()
        )
        jsonObject.addProperty(
            "income",
            activityUploadBusinessDetailsBinding.tieMonthlyIncome.text.toString()
        )
        jsonObject.addProperty(
            "expenses",
            activityUploadBusinessDetailsBinding.tieMonthlyExpense.text.toString()
        )
        jsonObject.addProperty(
            "businessAddress",
            activityUploadBusinessDetailsBinding.tieAddress.text.toString()
        )
        jsonObject.addProperty(
            "margin",
            activityUploadBusinessDetailsBinding.tieMargin.text.toString()
        )
        jsonObject.addProperty(
            "addressType",
            activityUploadBusinessDetailsBinding.tieAddress.visibility == View.INVISIBLE
        )
        jsonObject.addProperty(
            "dateOfIncorporation",
            activityUploadBusinessDetailsBinding.edtDateOfIncorporation.text.toString()
        )

        currentLocation?.latitude?.let {
            jsonObject.addProperty("lat", it)
        }

        currentLocation?.longitude?.let {
            jsonObject.addProperty("lng", it)
        }

        Log.e("TAG", jsonObject.toString())

        ApiClient().getAuthApiService(this).saveCustBusiness(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
//                if (response.body()?.apiCode == "200") {
////                                    hideProgressDialog()
                    updateStage()
//                } else {
//                    Toast.makeText(
//                        this@UploadBusinessDetailsActivity,
//                        "Something went wrong, Please try again later!!!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadBusinessDetailsActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun hasValidAadharDetailsData(): Boolean {
        if (activityUploadBusinessDetailsBinding.tieBusinessName.text.isNullOrEmpty()) {
            Toast.makeText(
                this, "Please Add Your Business Name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.edtDateOfIncorporation.text.isNullOrEmpty()) {
            Toast.makeText(
                this, "Please Add Your Business Incorporation Date",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.constitutionSpiner.selectedItem.toString()
                .equals("")
        ) {
            Toast.makeText(
                this, "Please Add Your Constitution",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.categorySpinner.selectedItem.toString()
                .equals("")
        ) {
            Toast.makeText(
                this, "Please Add Your Business Category",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.segmentSpinner.selectedItem.toString()
                .equals("")
        ) {
            Toast.makeText(
                this, "Please Add Your Business Segment",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.typeSpinner.selectedItem.toString() == ""
        ) {
            Toast.makeText(
                this, "Please Add Your Business Type",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.tieMonthlyTurnover.text.toString() == ""
        ) {
            Toast.makeText(
                this, "Please Specify Your Business Turnover",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.tieMargin.text.toString() == "") {
            Toast.makeText(
                this, "Please Specify Your Income Margin",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.tieMonthlyIncome.text.toString() == ""
        ) {
            Toast.makeText(
                this, "Please Specify Your Monthly Income",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.tieMonthlyExpense.text.toString()
                .equals("")
        ) {
            Toast.makeText(
                this, "Please  Specify Your Monthly Expense",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.radioGroup.checkedRadioButtonId == -1) {
            Toast.makeText(
                this, "Please Select Your Address Preference",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.radioGroup.checkedRadioButtonId != -1) {
            val radioButtonID = activityUploadBusinessDetailsBinding.radioGroup.checkedRadioButtonId
            val radioButton =
                activityUploadBusinessDetailsBinding.radioGroup.findViewById<View>(radioButtonID) as RadioButton
            val selectedText = radioButton.text as String
            if (selectedText.equals("No")) {
                if (activityUploadBusinessDetailsBinding.tieAddress.text.toString().equals("")) {
                    Toast.makeText(
                        this, "Please Select Your Business Address",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                } else {
                    return true
                }
            }
        }
        return true
    }

    fun hideSoftKeyboard(activity: Activity, view: View) {
        val imm: InputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val sdf = SimpleDateFormat("dd/M/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val selectedDate = "${dayOfMonth}/${month + 1}/${year}"

        val date1: Date? = sdf.parse(currentDate)
        val date2: Date? = sdf.parse(selectedDate)
        val yearDifference = getDiffYears(date1!!, date2!!)

        when {
            yearDifference < 3 -> {
                Toast.makeText(
                    this,
                    "Your Business have to be at least 3 years old",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                activityUploadBusinessDetailsBinding.edtDateOfIncorporation.setText("${dayOfMonth}/${month + 1}/${year}")
            }
        }
    }

    fun getDiffYears(first: Date, last: Date): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        val yearsInBetween: Int = (a.get(Calendar.YEAR) - b.get(Calendar.YEAR))
        return yearsInBetween
    }

    fun getCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.time = date
        return cal
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
//                CROP_REQUEST_CODE_CAMERA, CROP_REQUEST_CODE_GALLERY -> {
//                    val result = CropImage.getActivityResult(data)
//                    val resultUri = result.uri
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
//                    activityUploadBusinessDetailsBinding.businessImage.setImageBitmap(bitmap)
//                    val encodedImageStr = encodeImageString(bitmap)
//                    print("base64 Stirng $encodedImageStr")
//
//                    uploadBusinessCard(encodedImageStr)
//                }
//
//                CROP_REQUEST_CODE_CAMERA_SHOP_OWNER, CROP_REQUEST_CODE_GALLERY_SHOP_OWNER -> {
//                    val result = CropImage.getActivityResult(data)
//                    val resultUri = result.uri
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
//                    activityUploadBusinessDetailsBinding.businessOwnerImage.setImageBitmap(bitmap)
//                    val encodedImageStr = encodeImageString(bitmap)
//                    print("base64 Stirng $encodedImageStr")
//                    uploadBusinessCard(encodedImageStr)
//                }
//
//                CROP_REQUEST_CODE_CAMERA_STOCK, CROP_REQUEST_CODE_GALLERY_STOCK -> {
//                    val result = CropImage.getActivityResult(data)
//                    val resultUri = result.uri
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
//                    activityUploadBusinessDetailsBinding.businessStockImage.setImageBitmap(bitmap)
//                    val encodedImageStr = encodeImageString(bitmap)
//                    print("base64 Stirng $encodedImageStr")
//                    uploadBusinessCard(encodedImageStr)
//                }
//
//                CROP_REQUEST_CODE_CAMERA_SHOP_LOCALITY, CROP_REQUEST_CODE_GALLERY_SHOP_LOCALITY -> {
//                    val result = CropImage.getActivityResult(data)
//                    val resultUri = result.uri
//                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
//                    activityUploadBusinessDetailsBinding.businessLocalityImage.setImageBitmap(bitmap)
//                    val encodedImageStr = encodeImageString(bitmap)
//                    print("base64 Stirng $encodedImageStr")
//                    uploadBusinessCard(encodedImageStr)
//                }
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

    private fun uploadStatement(fileLocation: String) {
        if (mLoanId == null && mCustomerId == null) {
//            mLoanId = loanResponse!!.loanId
            mCustomerId = customerData!!.customerId
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(fileLocation)
                val requestBody: RequestBody =
                    RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                val multiPartBody =
                    MultipartBody.Part.createFormData("file", file.name, requestBody)
                val response =
                    ApiClient().getMasterApiService(this@UploadBusinessDetailsActivity)
                        .uploadStatement(multiPartBody, mLoanId, mCustomerId)

                withContext(Dispatchers.Main) {
                    try {
                        Toast.makeText(
                            this@UploadBusinessDetailsActivity, "Statement Uploaded Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val mDocId = response?.docId ?: "DOC00053300"

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    private fun setImage(encodedImageStr: String?) {
//        Glide.with(this)
//            .asBitmap()
//            .load(encodedImageStr)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: Transition<in Bitmap>?
//                ) {
//                    activityUploadBusinessDetailsBinding.businessImage.setImageBitmap(resource)
//                    val encodedImageStr = encodeImageString(resource)
//
//                    showProgressDialog()
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//
//                }
//            })
//    }

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
        val filepath = data?.extras?.get("FilePath")
        val finalFilePath = "file://${filepath}"
        val fileUri = Uri.parse(finalFilePath)
        val intent = CropImage.activity(fileUri)
            .getIntent(this)
        startActivityForResult(intent, cropRequestCodeCamera)
    }


    private fun uploadBusinessCard(encodedImageStr: String?) {

        val jsonObject = JsonObject()
//        jsonObject.addProperty("loanId", loanResponse!!.loanId)
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("applicantType", customerData!!.applicantType)
        jsonObject.addProperty("idType", "BUSINESS_IMG")
        jsonObject.addProperty("imageBase64", encodedImageStr!!)
        jsonObject.addProperty("applicationType", "CUSTOMER")

        ApiClient().getAuthApiService(this).verifyKYCDocs(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val docResponse = response.body()

                Toast.makeText(
                    this@UploadBusinessDetailsActivity,
                    "Business Card Uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBusinessDetailsActivity,
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

    private fun launchCamera(REQ_CODE: Int) {
        when (REQ_CODE) {
            REQ_CODE_CAMERA -> {
                startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
                    putExtra("doc_type", this@UploadBusinessDetailsActivity.REQ_CODE_CAMERA)
                    val dir = File(
                        this@UploadBusinessDetailsActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "ArthurFinance"
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
                        this@UploadBusinessDetailsActivity.REQ_CODE_CAMERA_SHOP_OWNER
                    )
                    val dir = File(
                        this@UploadBusinessDetailsActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
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
                    putExtra("doc_type", this@UploadBusinessDetailsActivity.REQ_CODE_CAMERA_STOCK)
                    val dir = File(
                        this@UploadBusinessDetailsActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
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
                        this@UploadBusinessDetailsActivity.REQ_CODE_CAMERA_SHOP_LOCALITY
                    )
                    val dir = File(
                        this@UploadBusinessDetailsActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
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

    private fun updateStage() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("stage", "BUSINESS")
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@UploadBusinessDetailsActivity, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                if (response.body()?.apiCode == "200") {

                    val intent = Intent(
                        this@UploadBusinessDetailsActivity,
                        UploadBusinessPhotos::class.java
                    )
                    intent.putExtra("customerData", customerData)
                    startActivity(intent)
                    finish()
                }

            }
        })
    }
}