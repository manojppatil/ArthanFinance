package com.av.arthanfinance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.core.content.ContextCompat
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.serviceRequest.Getintouch
import com.av.arthanfinance.user_kyc.UploadPanActivity
import com.av.arthanfinance.util.AppLocationProvider
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.example.awesomedialog.*
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar.getInstance


class RegistrationActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {
    private lateinit var btnSubmit: Button
    private lateinit var mobileNoText: EditText
    private lateinit var refIdText: EditText

    private lateinit var emailText: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnTC: CheckBox
    private lateinit var apiClient: ApiClient
    private var lat: String? = null
    private var lng: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null


    override val layoutId: Int
        get() = R.layout.layout_register

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiClient = ApiClient()

        if (supportActionBar != null)
            supportActionBar?.hide()

        mobileNoText = findViewById(R.id.edt_mob_num)
        refIdText = findViewById(R.id.refId)
        emailText = findViewById(R.id.edt_email)
        btnTC = findViewById(R.id.accept_tc)
        btnTC.movementMethod = LinkMovementMethod.getInstance()
        btnSubmit = findViewById(R.id.btn_submit)
        btnBack = findViewById(R.id.img_back)

        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant

        fetchLocation(1)

        hintMobileNo()

        btnSubmit.setOnClickListener {
            if (mobileNoText.text.isNotEmpty()) {

                if (mobileNoText.text.length != 10) {
                    clevertapDefaultInstance?.pushEvent("Invalid register mobile no")//added by CleverTap Assistant
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Please enter VALID Mobile Number.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (lat == null) {
                    clevertapDefaultInstance?.pushEvent("Location is not captured")//added by CleverTap Assistant
                    Toast.makeText(this,
                        "Location is not captured. Please wait fetching your location",
                        Toast.LENGTH_LONG).show()
                    fetchLocation(2)
                    return@setOnClickListener
                }

                if (!btnTC.isChecked) {
                    clevertapDefaultInstance?.pushEvent("Accept Terms conditions")//added by CleverTap Assistant
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Please accept terms and Conditions.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                registerCustomerData()
            } else {
                clevertapDefaultInstance?.pushEvent("Register Fields are missing")//added by CleverTap Assistant
                Toast.makeText(
                    this@RegistrationActivity,
                    "Mandatory fields missing please enter all data.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnBack = findViewById(R.id.img_back)
        btnBack.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Back from Register")//added by CleverTap Assistant
            this.finish()
        }
    }

    private fun fetchLocation(from: Int) {

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            -> {
                // You can use the API that requires the permission.
                AppLocationProvider().getLocation(
                    this,
                    object : AppLocationProvider.LocationCallBack {
                        override fun locationResult(location: Location?) {
                            lat = location?.latitude.toString()
                            lng = location?.longitude.toString()
                            Log.d("latlng", lng.toString())
                            AppLocationProvider().stopLocation()
                            clevertapDefaultInstance?.pushEvent("Register Location fetched")//added by CleverTap Assistant
                            // use location, this might get called in a different thread if a location is a last known location. In that case, you can post location on main thread
                        }

                    })

            }
            else -> {
                val request = permissionsBuilder(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).build()
                request.listeners {
                    onAccepted {
                        fetchLocation(from)
                        clevertapDefaultInstance?.pushEvent("Location Permission Accepted")//added by CleverTap Assistant
                    }
                    onDenied {
                        clevertapDefaultInstance?.pushEvent("Location Permission Denied")//added by CleverTap Assistant
                    }
                    onPermanentlyDenied {
                        clevertapDefaultInstance?.pushEvent("Location Permission Denied Permanently")//added by CleverTap Assistant
                    }
                }
                request.send()
            }
        }
    }

    private fun hintMobileNo() {
        val mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()

        mGoogleApiClient.connect()

        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()

        val intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, 1008, null, 0, 0, 0, null)
            clevertapDefaultInstance?.pushEvent("Hint mobile number")//added by CleverTap Assistant

        } catch (e: SendIntentException) {
            Log.e("", "Could not start hint picker Intent", e)
            clevertapDefaultInstance?.pushEvent("Could not start hint picker")//added by CleverTap Assistant

        }
    }

    private fun isValidPhoneNumber(phoneNumber: CharSequence): Boolean {
        return if (!TextUtils.isEmpty(phoneNumber)) {
            Patterns.PHONE.matcher(phoneNumber).matches()
        } else false
    }

    @SuppressLint("SimpleDateFormat")
    private fun registerCustomerData() {
        val calendar = getInstance()
        val mdformat = SimpleDateFormat("HH:mm:ss")
        val strDate = mdformat.format(calendar.time)
        Log.e("registerTime", strDate)
        val mobile = mobileNoText.text.toString()
        val refId = refIdText.text.toString()

        showProgressDialog()
        //Need to create GSON JsonObject rather than creating a new class object and sending the data in the API Calls
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobileNo", mobile)
        jsonObject.addProperty("aaConsent", "Y")
        jsonObject.addProperty("lat", lat)
        jsonObject.addProperty("lng", lng)
        jsonObject.addProperty("refId", refId)
        jsonObject.addProperty("product", "arthik")
        ApiClient().getAuthApiService(this).registerCustomer(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                t.printStackTrace()
                hideProgressDialog()
                Toast.makeText(
                    this@RegistrationActivity,
                    "Registration failure. Try after some time",
                    Toast.LENGTH_SHORT
                ).show()
                clevertapDefaultInstance?.pushEvent("Registration failure")//added by CleverTap Assistant
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                val custData = response.body()
                if (custData != null) {
                    when (custData.apiCode) {
                        "200" -> {
                            val sharedPref: SharedPreferences? =
                                getSharedPreferences("customerData", Context.MODE_PRIVATE)
                            val prefsEditor = sharedPref?.edit()
                            val gson = Gson()
                            val json: String = gson.toJson(custData)
                            prefsEditor?.putString("customerData", json)
                            prefsEditor?.putString("mobNo", mobile)
                            prefsEditor?.putString("customerId", custData.customerId)
                            prefsEditor?.putString("leadId", custData.leadId!!.toString())
                            prefsEditor?.apply()
                            updateStage(ArthanFinConstants.register, custData.customerId!!)
                            //cleverTap
                            val ProfilePush = HashMap<String, Any>()//added by CleverTap Assistant
                            ProfilePush["Phone"] = mobile//added by CleverTap Assistant
                            ProfilePush["CustomerId"] = custData.customerId!!
                            ProfilePush["Latitude"] = lat.toString()
                            ProfilePush["Longitude"] = lng.toString()
                            clevertapDefaultInstance?.pushProfile(ProfilePush)//added by CleverTap Assistant
                            clevertapDefaultInstance?.pushEvent("User Registered", ProfilePush)

                            AwesomeDialog.build(this@RegistrationActivity)
                                .title("Congratulations")
                                .body("Your account has been created")
                                .icon(R.drawable.ic_congrts)
                                .onPositive("Let's Get Started") {
                                    val intent = Intent(
                                        this@RegistrationActivity,
                                        CheckEligibilityActivity::class.java
                                    )
//                                    intent.putExtra("registerTime", strDate)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                        "400" -> {
                            Log.e("Error", custData.message + "")
                            val ProfilePush = HashMap<String, Any>()//added by CleverTap Assistant
                            ProfilePush["Phone"] = mobile//added by CleverTap Assistant
                            ProfilePush["Latitude"] = lat.toString()
                            ProfilePush["Longitude"] = lng.toString()
                            clevertapDefaultInstance?.pushProfile(ProfilePush)//added by CleverTap Assistant
                            clevertapDefaultInstance?.pushEvent("Location Not Serve",
                                ProfilePush)
                            AwesomeDialog.build(this@RegistrationActivity)
                                .title("We’re Coming Soon")
                                .body("Sorry, we don't serve this location yet.")
                                .icon(R.drawable.no_location)
                                .onPositive("Connect with us") {
                                    val intent =
                                        Intent(this@RegistrationActivity, Getintouch::class.java)
                                    startActivity(intent)
                                }
                        }
                        "401" -> {
                            Log.e("Error", custData.message + "")
                            clevertapDefaultInstance?.pushEvent("Invalid Reference ID")
                            AwesomeDialog.build(this@RegistrationActivity)
                                .title("Invalid Reference ID")
                                .body(custData.message)
                                .icon(R.drawable.ic_info_icon)
                        }
                        "402" -> {
                            Log.e("Error", custData.message + "")
                            clevertapDefaultInstance?.pushEvent("User Already Exists")
                            AwesomeDialog.build(this@RegistrationActivity)
                                .title("User already exists")
                                .body(custData.message)
                                .icon(R.drawable.ic_info_icon)
                                .onPositive("Login now") {
                                    val intent =
                                        Intent(this@RegistrationActivity,
                                            MPINLoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                }
            }
        })
    }

    private fun updateStage(stage: String, custId: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("stage", stage)
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1008) {
            if (resultCode == RESULT_OK) {
                val credential: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                credential?.apply {
                    Log.e("cred.getId", credential.id)
                    val userMob = credential.id
                    val mobNo = userMob.replace("+91", "")
                    mobileNoText.setText(mobNo)
                }
            }
        }
    }


    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}