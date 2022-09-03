package com.av.arthanfinance.user_kyc

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.GstValidationActivity
import com.av.arthanfinance.MapLocationActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.*
import com.av.arthanfinance.databinding.ActivityUploadBusinessDetailsBinding
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class UploadBusinessDetailsActivity : BaseActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var activityUploadBusinessDetailsBinding: ActivityUploadBusinessDetailsBinding
    private var constitutionList =
        arrayOf("Select", "Individual", "Sole Proprietorship", "Partnership")
    private var typeList = arrayOf("Select", "Manufacturing", "wholesaler", "Retailer", "Service")

    private var categoryList = ArrayList<String>()
    private var segmentList = ArrayList<String>()
    private var categories = ArrayList<Category>()
    private var kycCompleteStatus = "70"

    var loanResponse: LoanProcessResponse? = null
    var customerData: CustomerHomeTabResponse? = null

    private lateinit var locationAddress: String
    private lateinit var subLocality: String
    private lateinit var locationCity: String
    private lateinit var locationState: String
    private lateinit var locationCountry: String
    private lateinit var locationPostalCode: String

    private var mCustomerId: String? = null

    override val layoutId: Int
        get() = R.layout.activity_upload_business_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadBusinessDetailsBinding =
            ActivityUploadBusinessDetailsBinding.inflate(layoutInflater)
        setContentView(activityUploadBusinessDetailsBinding.root)

        if (intent.hasExtra("customerData")) {
            customerData = intent.getSerializableExtra("customerData") as CustomerHomeTabResponse
            mCustomerId = customerData!!.customerId
        }

        activityUploadBusinessDetailsBinding.tvPercent.text = "${kycCompleteStatus}%"
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

        val constitutionAdapter = ArrayAdapter(
            this,
            R.layout.emi_options, constitutionList
        )
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

        val typesAdapter = ArrayAdapter(
            this,
            R.layout.emi_options, typeList
        )
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

        activityUploadBusinessDetailsBinding.categorySpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                val category = categories[position]
                segmentList = category.segments as ArrayList<String>

                val segmentAdapter = ArrayAdapter(
                    this@UploadBusinessDetailsActivity,
                    R.layout.emi_options, segmentList
                )
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
            val url = "https://udyamregistration.gov.in/UdyamRegistration.aspx"
            if (url.startsWith("https://") || url.startsWith("http://")) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid Url", Toast.LENGTH_SHORT).show()
            }
        }
        val mapActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == 101) {
                val data = result.data
                if (data != null) {

                    locationAddress = data!!.getStringExtra("address").toString()
                    subLocality = data!!.getStringExtra("sub_locality").toString()
                    locationCity = data!!.getStringExtra("city").toString()
                    locationState = data!!.getStringExtra("state").toString()
                    locationCountry = data!!.getStringExtra("country").toString()
                    locationPostalCode = data!!.getStringExtra("postalCode").toString()

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

        activityUploadBusinessDetailsBinding.btnVerifyUdyam.setOnClickListener {
            val jsonObject = JsonObject()
            showProgressDialog()

            jsonObject.addProperty("consent", "Y")
            jsonObject.addProperty(
                "udyamRegistrationNo",
                activityUploadBusinessDetailsBinding.tieUdhyogAadharNo.text.toString()
            )
            jsonObject.addProperty("isPDFRequired", "N")

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

    private fun loadAdapter(categoryList: ArrayList<String>) {
        val categoryAdapter = ArrayAdapter(
            this,
            R.layout.emi_options, categoryList
        )

        activityUploadBusinessDetailsBinding.categorySpinner.adapter = categoryAdapter
    }

    private fun saveCustomerBusinessData() {

        val jsonObject = JsonObject()
        showProgressDialog()

        jsonObject.addProperty("customerId", customerData?.customerId)
        jsonObject.addProperty(
            "businessName",
            activityUploadBusinessDetailsBinding.tieBusinessName.text.toString()
        )
        jsonObject.addProperty(
            "constitution",
            activityUploadBusinessDetailsBinding.tieConstitution.text.toString()
        )
        jsonObject.addProperty(
            "category",
            activityUploadBusinessDetailsBinding.tieCategory.text.toString()
        )
        jsonObject.addProperty(
            "segment",
            activityUploadBusinessDetailsBinding.tieSegment.text.toString()
        )
        jsonObject.addProperty(
            "type",
            activityUploadBusinessDetailsBinding.tieType.text.toString()
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
        } else if (activityUploadBusinessDetailsBinding.constitutionSpiner.selectedItem.toString() == ""
        ) {
            Toast.makeText(
                this, "Please Add Your Constitution",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.categorySpinner.selectedItem.toString() == ""
        ) {
            Toast.makeText(
                this, "Please Add Your Business Category",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadBusinessDetailsBinding.segmentSpinner.selectedItem.toString() == ""
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
        } else if (activityUploadBusinessDetailsBinding.tieMonthlyExpense.text.toString() == ""
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
            if (selectedText == "No") {
                return if (activityUploadBusinessDetailsBinding.tieAddress.text.toString() == "") {
                    Toast.makeText(
                        this, "Please Select Your Business Address",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    true
                }
            }
        }
        return true
    }

    private fun hideSoftKeyboard(activity: Activity, view: View) {
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

    private fun getDiffYears(first: Date, last: Date): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        return (a.get(Calendar.YEAR) - b.get(Calendar.YEAR))
    }

    private fun getCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.time = date
        return cal
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