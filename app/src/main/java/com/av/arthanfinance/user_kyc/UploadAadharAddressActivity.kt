package com.av.arthanfinance.user_kyc

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.models.CustomerHomeTabResponse
import com.av.arthanfinance.MapLocationActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.CustomerCameraActivity
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.databinding.ActivityUploadAadharAddressBinding
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.AppLocationProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.theartofdev.edmodo.cropper.CropImage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class UploadAadharAddressActivity : BaseActivity() {
    private lateinit var activityUploadAadharAddressBinding: ActivityUploadAadharAddressBinding
    private var kycCompleteStatus = "40"
    private var titleList = arrayOf("Select Title", "Mr.", "Ms.")
    private var qualificationList =
        arrayOf(
            "Select Qualification",
            "10th Pass",
            "Under Graduate",
            "Graduate",
            "Post Graduate"
        )
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
        "Maharashtra"
    )
    private var genderList = arrayOf("Select Gender", "Male", "Female", "Other")
    private var addressState = ""
    private var a = 0
    private var b = 0
    private var addrFlag = true
    private var fatherName = ""
    private var name = ""
    private var dob = ""
    var gender = ""
    var aadharImage = ""
    private var addressLine1 = ""
    private var addressLine2 = ""
    private var addressLine3 = ""
    var state = ""
    var pincode = ""
    private var landMark = ""
    private var city = ""
    private var currentLandMark = ""
    var currentCity = ""
    var currentPincode = ""
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

    private var lat: String? = null
    private var lng: String? = null

    private var MY_CAMERA_PERMISSION_CODE = 100
    private var REQ_CODE_PHOTO_ID = 321
    private var REQ_CODE = 33
    private var CROP_REQUEST_CODE_CAMERA = 103
    private var isPhotoUploaded: Boolean = false


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityUploadAadharAddressBinding =
            ActivityUploadAadharAddressBinding.inflate(layoutInflater)
        setContentView(activityUploadAadharAddressBinding.root)

        setSupportActionBar(activityUploadAadharAddressBinding.tbUploadAddress)
        (supportActionBar)?.setDisplayHomeAsUpEnabled(false)
        (this as AppCompatActivity).supportActionBar!!.title = "Let us know you more and better"

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

//        val titleAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, titleList) }
//        activityUploadAadharAddressBinding.actTitle.setAdapter(titleAdapter)

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

//        val statesAdapter = this.let { ArrayAdapter(it, R.layout.emi_options, stateList) }
//        activityUploadAadharAddressBinding.stateDropDown.adapter = statesAdapter

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

        activityUploadAadharAddressBinding.ivUserImage.setOnClickListener {
            if (requestPermission()) {
                launchCamera(REQ_CODE)
            }
        }

        fetchLocation(1)

//        val statePosition = stateList.indexOf(state)
//        a = Integer.parseInt(statePosition.toString())
//        activityUploadAadharAddressBinding.stateDropDown.setSelection(a, true)
//
//        val genderPosition = genderList.indexOf(gender)
//        b = Integer.parseInt(genderPosition.toString())
//        activityUploadAadharAddressBinding.stateDropDown.setSelection(b, true)
//
//        addressState = activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()
//
//        activityUploadAadharAddressBinding.stateDropDown.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                addressState =
//                    activityUploadAadharAddressBinding.stateDropDown.selectedItem.toString()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // write code to perform some action
//            }
//        }

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
        ObjectAnimator.ofInt(activityUploadAadharAddressBinding.pbKycAddress, "progress", 40)
            .setDuration(1000).start()
        activityUploadAadharAddressBinding.tvPercent.text = "${kycCompleteStatus}%"

        activityUploadAadharAddressBinding.btnAadharDetails.setOnClickListener {
            if (hasValidAadharDetailsData()) {
                savePersonalDetails()
            }
        }
    }

    private fun launchCamera(reqCode: Int) {
        startActivityForResult(Intent(this, CustomerCameraActivity::class.java).apply {
            putExtra("doc_type", REQ_CODE_PHOTO_ID)
            val dir = File(
                this@UploadAadharAddressActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "ArthanFinance"
            )
            // if (!dir.exists())
            dir.mkdirs()
            val time = System.currentTimeMillis().toString()
            putExtra("FilePath", "${dir.absolutePath}/IMG_PHOTO_ID_${time}.jpg")
            putExtra("is_front", false)
        }, REQ_CODE_PHOTO_ID)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
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

    private fun getCustomerDetails() {
        showProgressDialog()
        val customerId = customerData!!.customerId.toString()
        Log.e("TAG::", customerId)
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerId)

        ApiClient().getAuthApiService(this).getCustomerDetails(jsonObject).enqueue(object :
            Callback<UserDetailsResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<UserDetailsResponse>,
                response: Response<UserDetailsResponse>
            ) {
                hideProgressDialog()
                val apiCode = response.body()!!.apiCode

                if (response.body() != null) {
                    name = response.body()!!.customerName
                    fatherName = response.body()!!.fatherName
                    addressLine1 = response.body()!!.addressLine1.toString()
                    addressLine2 = response.body()!!.addressLine2.toString()
                    addressLine3 = response.body()!!.addressLine3.toString()
                    state = response.body()!!.state.toString()
                    pincode = response.body()!!.pinCode.toString()

                    val decodedString: ByteArray = Base64.decode(aadharImage, Base64.NO_WRAP)
                    val decodedByte =
                        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

//                    activityUploadAadharAddressBinding.ivUserImage.setImageBitmap(decodedByte)
                    activityUploadAadharAddressBinding.actTitle.text = "Namaste, $name"
                    activityUploadAadharAddressBinding.tieFatherName.setText(fatherName)
                    activityUploadAadharAddressBinding.tieAddress1.setText(addressLine1)
                    activityUploadAadharAddressBinding.tieAddress2.setText(addressLine2)
                    activityUploadAadharAddressBinding.tieAddress3.setText(addressLine3)
                    activityUploadAadharAddressBinding.tiePincode.setText(pincode)
                    activityUploadAadharAddressBinding.tieState.setText(state)

                    /* val sharedPref: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
                         val prefsEditor = sharedPref?.edit()
                         val gson = Gson()
                         val json: String = gson.toJson(docResponse)
                         prefsEditor?.putString("customerData", json)
                         prefsEditor?.apply()
                         customerData = gson.fromJson(json, CustomerHomeTabResponse::class.java)*/

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
            addressLine1
        )
        jsonObject.addProperty(
            "addressLine2",
            addressLine2
        )
        jsonObject.addProperty(
            "addressLine3",
            addressLine3
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
            pincode
        )
        jsonObject.addProperty(
            "state",
            pincode
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
                activityUploadAadharAddressBinding.tieState.text.toString()
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
                    finish()
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
//                        Toast.makeText(
//                            this@UploadAadharAddressActivity,
//                            e.message,
//                            Toast.LENGTH_LONG
//                        ).show()
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
//        val title = activityUploadAadharAddressBinding.actTitle.text.toString()
        val qualification = activityUploadAadharAddressBinding.actEducation.text.toString()
        val maritalStatus = activityUploadAadharAddressBinding.actMaritalStatus.text.toString()
//        val religion = activityUploadAadharAddressBinding.actReligion.text.toString()
//        val category = activityUploadAadharAddressBinding.actCategory.text.toString()
//        val gender = activityUploadAadharAddressBinding.actGender.text.toString()
//        val currentState = activityUploadAadharAddressBinding.stateDropDown2.selectedItem.toString()
//        val pinCode = activityUploadAadharAddressBinding.tiePincode.text.toString()
        val currentPinCode = activityUploadAadharAddressBinding.tiePincode2.text.toString()
        val locationAddress = activityUploadAadharAddressBinding.tvLocationAddress.text.toString()

//        if (title.equals("") || title.equals("Select Title")) {
//            Toast.makeText(this, "Please select your title", Toast.LENGTH_SHORT).show()
//            return false
//        } else
        if (activityUploadAadharAddressBinding.tieFatherName.text.isNullOrEmpty()) {
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
        } else if (!isPhotoUploaded) {
            Toast.makeText(
                applicationContext,
                "Please Upload your Profile Photo",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (qualification == "" || qualification == "Select Qualification") {
            Toast.makeText(
                applicationContext,
                "Please select your qualification",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (maritalStatus == "" || maritalStatus == "Select Marital Status") {
            Toast.makeText(this, "Please select your marital status", Toast.LENGTH_SHORT).show()
            return false
        }
//            else if (religion.equals("") || religion.equals("Select Religion")) {
//            Toast.makeText(applicationContext, "Please select your religion", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        }
//        else if (category.equals("") || category.equals("Select Category")) {
//            Toast.makeText(applicationContext, "Please select your category", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        }
//        else if (activityUploadAadharAddressBinding.tieAddress1.text.isNullOrEmpty()) {
//            Toast.makeText(applicationContext, "Please fill address line 1", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieAddress2.text.isNullOrEmpty()) {
//            Toast.makeText(applicationContext, "Please fill address line 2", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieAddress3.text.isNullOrEmpty()) {
//            Toast.makeText(applicationContext, "Please fill address line 3", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieCity.text.isNullOrEmpty()) {
//            Toast.makeText(applicationContext, "Please provide your city name", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieDistrict.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please provide your district name",
//                Toast.LENGTH_SHORT
//            ).show()
//            return false
//        } else if (pinCode == "" || pinCode.length < 6) {
//            Toast.makeText(applicationContext, "Please provide your pincode", Toast.LENGTH_SHORT)
//                .show()
//            return false
//        }
//        else if (addressState.equals("") || addressState.equals("Select State")) {
//            Toast.makeText(applicationContext, "Please Select State", Toast.LENGTH_SHORT).show()
//            return false
//        }
//        else if (activityUploadAadharAddressBinding.checkbox.isChecked) {
//            return true
//        } else if (activityUploadAadharAddressBinding.tieCurrentAddress1.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please fill current address line 1",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieCurrentAddress2.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please fill current address line 2",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieCurrentAddress3.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please fill current address line 3",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieCity2.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please provide your current city name",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return false
//        } else if (activityUploadAadharAddressBinding.tieDistrict2.text.isNullOrEmpty()) {
//            Toast.makeText(
//                applicationContext,
//                "Please provide your current district name",
//                Toast.LENGTH_SHORT
//            ).show()
//            return false
//        } else if (currentPinCode == "" || currentPinCode.length < 6) {
//            Toast.makeText(
//                applicationContext,
//                "Please provide your current pincode",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//            return false
//        } else if (locationAddress.equals("") || currentState.equals("Your Location")) {
//            Toast.makeText(
//                applicationContext,
//                "Please Provide Your Current Location",
//                Toast.LENGTH_SHORT
//            ).show()
//            return false
//        }
//        else if (currentState.equals("") || currentState.equals("Select State")) {
//            Toast.makeText(applicationContext, "Please Select State", Toast.LENGTH_SHORT).show()
//            return false
//        }
        else {
            return true
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                REQ_CODE_PHOTO_ID -> {
                    val filepath = data?.extras?.get("FilePath")
                    val finalFilePath = "file://${filepath}"
                    val fileUri = Uri.parse(finalFilePath)
                    data?.data?.let { uri ->
                        compressImage(uri.toString())
                    }
                    val intent = CropImage.activity(fileUri).getIntent(this)
                    startActivityForResult(intent, CROP_REQUEST_CODE_CAMERA)
                }

                CROP_REQUEST_CODE_CAMERA -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    val bitmapImage = BitmapFactory.decodeFile(resultUri.path)
                    val nh = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
                    val scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true)
                    activityUploadAadharAddressBinding.ivUserImage.setImageBitmap(scaled)
                    val encodedImageStr = encodeImageString(scaled)
                    print("base64 Stirng $encodedImageStr")
                    showProgressDialog()
                    uploadPhotoImage(encodedImageStr)

                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadPhotoImage(encodedImageStr: String?) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId.toString())
        jsonObject.addProperty("applicantType", "PA")
        jsonObject.addProperty("customerImage", encodedImageStr)
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)

        Log.e("TAGREQ", jsonObject.toString())

        ApiClient().getAuthApiService(this).uploadCustomerImage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {

                val docResponse = response.body() as AuthenticationResponse
                val apiCode = docResponse.apiCode
                //val message = docResponse.message

                if (apiCode.equals("200")) {
                    Toast.makeText(
                        this@UploadAadharAddressActivity,
                        "Photo uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    isPhotoUploaded = true
                    hideProgressDialog()
                } else {
                    hideProgressDialog()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            this@UploadAadharAddressActivity,
                            jObjError.getJSONObject("error").getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(
                            this@UploadAadharAddressActivity,
                            e.message,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    Toast.makeText(
                        this@UploadAadharAddressActivity,
                        "PAN Details Upload Failed. Please Try After Sometime",
                        Toast.LENGTH_SHORT
                    ).show()
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@UploadAadharAddressActivity,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
                hideProgressDialog()
            }
        })
    }

    private fun encodeImageString(bm: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun compressImage(imageUri: String): String {
        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp: Bitmap = BitmapFactory.decodeFile(filePath, options)
        var actualHeight: Int = options.outHeight
        var actualWidth: Int = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap =
                Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270F)
                Log.d("EXIF", "Exif: $orientation")
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        val filename = getFilename()
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
    }

    private fun getFilename(): String {
        val file = File(
            Environment.getExternalStorageDirectory().path,
            "MyFolder/Images"
        )
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
    }

    @SuppressLint("Recycle")
    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor: Cursor? = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    private fun fetchLocation(from: Int) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                AppLocationProvider().getLocation(
                    this,
                    object : AppLocationProvider.LocationCallBack {
                        override fun locationResult(location: Location?) {

                            lat = location?.latitude.toString()
                            lng = location?.longitude.toString()
                            Handler(Looper.getMainLooper()).post(kotlinx.coroutines.Runnable {
                            })
                            AppLocationProvider().stopLocation()
                        }

                    })
            }
            else -> {
//                val request = permissionsBuilder(
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ).build()
//                request.listeners {
//                    onAccepted {
//                        fetchLocation(from)
//
//                    }
//                    onDenied {
//                    }
//                    onPermanentlyDenied {
//                    }
//                }
//                request.send()
            }
        }
    }

}