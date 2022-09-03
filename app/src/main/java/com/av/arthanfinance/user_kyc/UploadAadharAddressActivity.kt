package com.av.arthanfinance.user_kyc

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.MapLocationActivity
import com.av.arthanfinance.MapsActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.UserDetailsResponse
import com.av.arthanfinance.databinding.ActivityUploadAadharAddressBinding
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.layout_adhar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadAadharAddressActivity : BaseActivity() {
    private lateinit var activityUploadAadharAddressBinding: ActivityUploadAadharAddressBinding
    private var kycCompleteStatus = "40"
    private var titleList = arrayOf("Select Title", "Mr.", "Ms.")
    private var qualificationList =
        arrayOf("Select Qualification", "Graduate", "Post Graduate", "Under Graduate", "10th Pass")
    private var maritalStatusList =
        arrayOf("Select Marital Status", "Single", "Married", "Separated", "Divorced")
    private var religionList = arrayOf("Select Religion", "Hindu", "Muslim", "Christian", "Other")
    private var categoryList = arrayOf("Select Category", "General", "OBC", "SC", "ST")
    private var stateList = arrayOf(
        "Select State",
        "Odisha",
        "Telangana",
        "Andhra Pradesh",
        "Tamil Nadu",
        "Karnataka",
        "Maharastra"
    )
    private var genderList = arrayOf("Select Gender", "Male", "Female", "Other")
    private var addressState = ""
    private var a = 0
    private var b = 0
    private var addrFlag = true
    private var fatherName = ""
    private var name = "";
    private var dob = "";
    var gender = "";
    var aadharImage = ""
    private var addressLine1 = "";
    private var addressLine2 = "";
    private var addressLine3 = "";
    var state = "";
    var pincode = ""
    private var landMark = "";
    private var city = "";
    private var currentLandMark = "";
    var currentCity = "";
    var currentPincode = "";
    var currentState = ""
    private var customerData: CustomerHomeTabResponse? = null
    override val layoutId: Int
        get() = R.layout.activity_upload_aadhar_address

    private lateinit var locationAddress: String
    private lateinit var subLocality: String
    private lateinit var locationCity: String
    private lateinit var locationState: String
    private lateinit var locationCountry: String
    private lateinit var locationPostalCode: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadAadharAddressBinding =
            ActivityUploadAadharAddressBinding.inflate(layoutInflater)
        setContentView(activityUploadAadharAddressBinding.root)

        setSupportActionBar(activityUploadAadharAddressBinding.tbUploadAddress)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Provide Address Details"

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
            getCustomerDetails()
        }

//        if (intent.hasExtra("name")) {
//            name = intent.getStringExtra("name")!!
//            fatherName = intent.getStringExtra("fatherName")!!
//            dob = intent.getStringExtra("dob")!!
//            gender = intent.getStringExtra("gender")!!
//            aadharImage = intent.getStringExtra("image1")!!
//            addressLine1 = intent.getStringExtra("addressLine1")!!
//            addressLine2 = intent.getStringExtra("addressLine2")!!
//            addressLine3 = intent.getStringExtra("addressLine3")!!
//            state = intent.getStringExtra("state")!!
//            pincode = intent.getStringExtra("pincode")!!
//        } else {
//            val sharedPref: SharedPreferences =
//                getSharedPreferences("father_name", Context.MODE_PRIVATE)
//            fatherName = sharedPref.getString("father_name", "").toString()
//            getCustomerDetails()
//        }
//
//        val decodedString: ByteArray = Base64.decode(aadharImage, Base64.NO_WRAP)
//        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//
//        //activityUploadAadharAddressBinding.ivUserImage.setImageBitmap(decodedByte)
//        activityUploadAadharAddressBinding.tieFatherName.setText(fatherName)
//        activityUploadAadharAddressBinding.tieAddress1.setText(addressLine1)
//        activityUploadAadharAddressBinding.tieAddress2.setText(addressLine2)
//        activityUploadAadharAddressBinding.tieAddress3.setText(addressLine3)
//        activityUploadAadharAddressBinding.tiePincode.setText(pincode)

        val titleAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, titleList) }
        activityUploadAadharAddressBinding.actTitle.setAdapter(titleAdapter)

        val qualificationAdapter =
            this.let { ArrayAdapter(it, R.layout.emi_options, qualificationList) }
        activityUploadAadharAddressBinding.actEducation.setAdapter(qualificationAdapter)

        val maritalStatusAdapter =
            this.let { ArrayAdapter(it, R.layout.emi_options, maritalStatusList) }
        activityUploadAadharAddressBinding.actMaritalStatus.setAdapter(maritalStatusAdapter)

        val religionAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, religionList) }
        activityUploadAadharAddressBinding.actReligion.setAdapter(religionAdapter)

        val categoryAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, categoryList) }
        activityUploadAadharAddressBinding.actCategory.setAdapter(categoryAdapter)

        val genderAdapter = ArrayAdapter(this, R.layout.emi_options, genderList)
        activityUploadAadharAddressBinding.actGender.setAdapter(genderAdapter)

        val statesAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, stateList) }
        activityUploadAadharAddressBinding.stateDropDown.adapter = statesAdapter

        val statesAdapter2 = this.let { ArrayAdapter(it, R.layout.emi_options, stateList) }
        activityUploadAadharAddressBinding.stateDropDown2.adapter = statesAdapter2

        activityUploadAadharAddressBinding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!activityUploadAadharAddressBinding.checkbox.isChecked) {
                activityUploadAadharAddressBinding.currentAddressLayout.visibility = View.VISIBLE
            } else {
                activityUploadAadharAddressBinding.currentAddressLayout.visibility = View.GONE
                activityUploadAadharAddressBinding.tieCurrentAddress1.visibility = View.GONE

            }
        }

        val statePosition = stateList.indexOf(state)
        a = Integer.parseInt(statePosition.toString())
        activityUploadAadharAddressBinding.stateDropDown.setSelection(a, true)

        val genderPosition = genderList.indexOf(gender)
        b = Integer.parseInt(genderPosition.toString())
        activityUploadAadharAddressBinding.stateDropDown.setSelection(b, true)

        addressState = activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()

        activityUploadAadharAddressBinding.stateDropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                addressState =
                    activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        val mapActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == 101) {
                val data = result.data
                if (data != null) {

                    locationAddress = data.getStringExtra("address").toString()
                    subLocality = data.getStringExtra("sub_locality").toString()
                    locationCity = data.getStringExtra("city").toString()
                    locationState = data.getStringExtra("state").toString()
                    locationPostalCode = data.getStringExtra("postalCode").toString()

                    activityUploadAadharAddressBinding.tvLocationAddress.text = locationAddress
                }
            }
        }

        activityUploadAadharAddressBinding.clLocation.setOnClickListener {
            val intent = Intent(this, MapLocationActivity::class.java)
            mapActivityResultLauncher.launch(intent)
        }

        activityUploadAadharAddressBinding.pbKycAddress.max = 100
        ObjectAnimator.ofInt(activityUploadAadharAddressBinding.pbKycAddress, "progress", 70)
            .setDuration(1000).start()
        activityUploadAadharAddressBinding.tvPercent.text = "${kycCompleteStatus}%"

        activityUploadAadharAddressBinding.btnAadharDetails.setOnClickListener {
            if (hasValidAadharDetailsData()) {
                savePersonalDetails()
            }
        }
    }

    private fun getCustomerDetails() {
        showProgressDialog()
        val customerId = customerData!!.customerId.toString()
        Log.e("TAG::", customerId)
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)

        ApiClient().getAuthApiService(this).getCustomerDetails(jsonObject).enqueue(object :
            Callback<UserDetailsResponse> {
            override fun onResponse(
                call: Call<UserDetailsResponse>,
                response: Response<UserDetailsResponse>
            ) {
                hideProgressDialog()
                val apiCode = response.body()!!.apiCode

                if (response.body() != null) {
                    name = response.body()!!.customerName
                    fatherName = response.body()!!.fatherName.toString()
                    addressLine1 = response.body()!!.addressLine1.toString()
                    addressLine2 = response.body()!!.addressLine2.toString()
                    addressLine3 = response.body()!!.addressLine3.toString()
                    state = response.body()!!.state.toString()
                    pincode = response.body()!!.pinCode.toString()

                    val decodedString: ByteArray = Base64.decode(aadharImage, Base64.NO_WRAP)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

//                    activityUploadAadharAddressBinding.ivUserImage.setImageBitmap(decodedByte)
                    activityUploadAadharAddressBinding.tieFatherName.setText(fatherName)
                    activityUploadAadharAddressBinding.tieAddress1.setText(addressLine1)
                    activityUploadAadharAddressBinding.tieAddress2.setText(addressLine2)
                    activityUploadAadharAddressBinding.tieAddress3.setText(addressLine3)
                    activityUploadAadharAddressBinding.tiePincode.setText(pincode)

                } else {
                    Toast.makeText(
                        this@UploadAadharAddressActivity,
                        "Service Failure, Once server connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharAddressActivity,
                    "Service Failure, Once network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    private fun savePersonalDetails() {
        val jsonObject = JsonObject()
        val customerId = customerData!!.customerId.toString()
        val contactNumber = customerData!!.mobNo.toString()
        val customerEmail = customerData!!.emailId.toString()
        addrFlag = activityUploadAadharAddressBinding.checkbox.isChecked
        jsonObject.addProperty("addrFlag", addrFlag)
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("fullName", name)
        jsonObject.addProperty(
            "fatherOrSpousename",
            activityUploadAadharAddressBinding.tieFatherName.text.toString()
        )
        jsonObject.addProperty(
            "motherName",
            activityUploadAadharAddressBinding.tieMotherName.text.toString()
        )
        jsonObject.addProperty("customerId", customerId)
        jsonObject.addProperty("gender", gender)
        jsonObject.addProperty("dob", dob)
        jsonObject.addProperty("contactNo", contactNumber)
        jsonObject.addProperty("email", customerEmail)
        jsonObject.addProperty("nationality", "Indian")
        jsonObject.addProperty("relationship", "NA")
        jsonObject.addProperty(
            "addressLine1",
            activityUploadAadharAddressBinding.tieAddress1.text.toString()
        )
        jsonObject.addProperty(
            "addressLine2",
            activityUploadAadharAddressBinding.tieAddress2.text.toString()
        )
        jsonObject.addProperty(
            "addressLine3",
            activityUploadAadharAddressBinding.tieAddress3.text.toString()
        )
        jsonObject.addProperty(
            "landmark",
            activityUploadAadharAddressBinding.tieLandmark.text.toString()
        )
        jsonObject.addProperty("city", activityUploadAadharAddressBinding.tieCity.text.toString())
        jsonObject.addProperty(
            "district",
            activityUploadAadharAddressBinding.tieDistrict.text.toString()
        )
        jsonObject.addProperty(
            "pincode",
            activityUploadAadharAddressBinding.tiePincode.text.toString()
        )
        jsonObject.addProperty(
            "state",
            activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()
        )

        if (addrFlag) {
            jsonObject.addProperty(
                "addressLine1p",
                activityUploadAadharAddressBinding.tieAddress1.text.toString()
            )
            jsonObject.addProperty(
                "addressLine2p",
                activityUploadAadharAddressBinding.tieAddress2.text.toString()
            )
            jsonObject.addProperty(
                "addressLine3p",
                activityUploadAadharAddressBinding.tieAddress3.text.toString()
            )
            jsonObject.addProperty(
                "landmarkp",
                activityUploadAadharAddressBinding.tieLandmark.text.toString()
            )
            jsonObject.addProperty(
                "cityp",
                activityUploadAadharAddressBinding.tieCity.text.toString()
            )
            jsonObject.addProperty(
                "districtp",
                activityUploadAadharAddressBinding.tieDistrict.text.toString()
            )
            jsonObject.addProperty(
                "pinCodep",
                activityUploadAadharAddressBinding.tiePincode.text.toString()
            )
            jsonObject.addProperty(
                "statep",
                activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()
            )
        } else {
            jsonObject.addProperty(
                "addressLine1",
                activityUploadAadharAddressBinding.tieCurrentAddress1.text.toString()
            )
            jsonObject.addProperty(
                "addressLine2",
                activityUploadAadharAddressBinding.tieCurrentAddress2.text.toString()
            )
            jsonObject.addProperty(
                "addressLine3",
                activityUploadAadharAddressBinding.tieCurrentAddress3.text.toString()
            )
            jsonObject.addProperty(
                "landmarkp",
                activityUploadAadharAddressBinding.tieLandmark2.text.toString()
            )
            jsonObject.addProperty(
                "cityp",
                activityUploadAadharAddressBinding.tieCity2.text.toString()
            )
            jsonObject.addProperty(
                "districtp",
                activityUploadAadharAddressBinding.tieDistrict2.text.toString()
            )
            jsonObject.addProperty(
                "pinCodep",
                activityUploadAadharAddressBinding.tiePincode2.text.toString()
            )
            jsonObject.addProperty(
                "statep",
                activityUploadAadharAddressBinding.stateDropDown2.selectedItem.toString()
            )
        }

        ApiClient().getAuthApiService(this).saveCustomerDetails(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
                val docResponse = response.body() as AuthenticationResponse
                val apiCode = docResponse.apiCode

                if (apiCode.equals("200")) {
                    Toast.makeText(
                        this@UploadAadharAddressActivity,
                        "Personal details uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent1 = Intent(
                        this@UploadAadharAddressActivity,
                        UploadBankDetailsActivity::class.java
                    )
                    startActivity(intent1)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadAadharAddressActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@UploadAadharAddressActivity,
                            e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Toast.makeText(
                        this@UploadAadharAddressActivity,
                        "Personal details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    this@UploadAadharAddressActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    private fun hasValidAadharDetailsData(): Boolean {
        val title = activityUploadAadharAddressBinding.actTitle.text.toString()
        val qualification = activityUploadAadharAddressBinding.actEducation.text.toString()
        val maritalStatus = activityUploadAadharAddressBinding.actMaritalStatus.text.toString()
        val religion = activityUploadAadharAddressBinding.actReligion.text.toString()
        val category = activityUploadAadharAddressBinding.actCategory.text.toString()
        val gender = activityUploadAadharAddressBinding.actGender.text.toString()
        val currentState = activityUploadAadharAddressBinding.stateDropDown2.selectedItem.toString()
        val pinCode = activityUploadAadharAddressBinding.tiePincode.text.toString()
        val currentPinCode = activityUploadAadharAddressBinding.tiePincode2.text.toString()
        val locationAddress = activityUploadAadharAddressBinding.tvLocationAddress.text.toString()

        if (title.equals("") || title.equals("Select Title")) {
            Toast.makeText(this, "Please select your title", Toast.LENGTH_SHORT).show()
            return false
        } else if (activityUploadAadharAddressBinding.tieFatherName.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please provide your father's or spouse's name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (activityUploadAadharAddressBinding.tieMotherName.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please provide your mother's name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (gender.equals("") || qualification.equals("Select Gender")) {
            Toast.makeText(
                applicationContext,
                "Please select your gender",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (qualification.equals("") || qualification.equals("Select Qualification")) {
            Toast.makeText(
                applicationContext,
                "Please select your qualification",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (maritalStatus.equals("") || maritalStatus.equals("Select Marital Status")) {
            Toast.makeText(this, "Please select your marital status", Toast.LENGTH_SHORT).show()
            return false
        } else if (religion.equals("") || religion.equals("Select Religion")) {
            Toast.makeText(applicationContext, "Please select your religion", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (category.equals("") || category.equals("Select Category")) {
            Toast.makeText(applicationContext, "Please select your category", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieAddress1.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Please fill address line 1", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieAddress2.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Please fill address line 2", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieAddress3.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Please fill address line 3", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieCity.text.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "Please provide your city name", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieDistrict.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please provide your district name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (pinCode == "" || pinCode.length < 6) {
            Toast.makeText(applicationContext, "Please provide your pincode", Toast.LENGTH_SHORT)
                .show()
            return false
        } else if (addressState.equals("") || addressState.equals("Select State")) {
            Toast.makeText(applicationContext, "Please Select State", Toast.LENGTH_SHORT).show()
            return false
        } else if (activityUploadAadharAddressBinding.checkbox.isChecked) {
            return true
        } else if (activityUploadAadharAddressBinding.tieCurrentAddress1.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill current address line 1",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieCurrentAddress2.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill current address line 2",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieCurrentAddress3.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please fill current address line 3",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieCity2.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please provide your current city name",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        } else if (activityUploadAadharAddressBinding.tieDistrict2.text.isNullOrEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please provide your current district name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (currentPinCode == "" || currentPinCode.length < 6) {
            Toast.makeText(
                applicationContext,
                "Please provide your current pincode",
                Toast.LENGTH_SHORT
            )
                .show()
            return false
        } else if (locationAddress.equals("") || currentState.equals("Your Location")) {
            Toast.makeText(
                applicationContext,
                "Please Provide Your Current Location",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (currentState.equals("") || currentState.equals("Select State")) {
            Toast.makeText(applicationContext, "Please Select State", Toast.LENGTH_SHORT).show()
            return false
        } else {
            return true
        }

    }

    override fun onBackPressed() {
        finish()
    }
}