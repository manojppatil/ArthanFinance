package com.av.arthanfinance.user_kyc

import `in`.digio.sdk.kyc.DigioEnvironment
import `in`.digio.sdk.kyc.DigioKycConfig
import `in`.digio.sdk.kyc.DigioKycResponseListener
import `in`.digio.sdk.kyc.DigioSession
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.DigilockerTokenResponse
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_video_kyc.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoKyc : AppCompatActivity(), DigioKycResponseListener {
    var customerData: CustomerHomeTabResponse? = null
    private lateinit var tokenId: String
    private lateinit var kId: String
    private var MY_CAMERA_PERMISSION_CODE = 100
    private var kycCompleteStatus = "60"
    private var progressView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_kyc)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("customerData", null)
        if (json != null) {
            val obj: CustomerHomeTabResponse =
                gson.fromJson(json, CustomerHomeTabResponse::class.java)
            customerData = obj
        }
        tvPercent.text = "${kycCompleteStatus}%"

        btn_captureDigioVkyc.setOnClickListener {
            getKid()
        }
        skipVideoKyc.setOnClickListener {
            updateStage("VKYC_SKIP")
        }
    }

    private fun getKid() {
        val jsonObject = JsonObject()
        val jsonObject1 = JsonObject()
        val jsonObject2 = JsonObject()
        val actionArray = JsonArray()
        val subActionArray = JsonArray()
        showProgressDialog()
        jsonObject.addProperty("customer_identifier", customerData!!.mobNo)
        jsonObject.addProperty("notify_customer", false)
        jsonObject.addProperty("generate_access_token", true)

        jsonObject1.addProperty("type", "VIDEO")
        jsonObject1.addProperty("method", "OTP_NONE")
        jsonObject1.addProperty("title", "Self Video KYC")
        jsonObject1.addProperty("description", "Please follow the instructions and record a video")
        jsonObject1.addProperty("allow_ocr_data_update", true)
        jsonObject1.addProperty("face_match_obj_type", "MATCH_REQUIRED")

        jsonObject2.addProperty("type", "USER_INSTRUCTION")
        jsonObject2.addProperty("title", "Documents preparation")
        jsonObject2.addProperty(
            "description",
            "Please keep your Pan and Aadhar card ready to show in the video"
        )

        subActionArray.add(jsonObject2)
        jsonObject1.add("sub_actions", subActionArray)

        actionArray.add(jsonObject1)
        jsonObject.add("actions", actionArray)

        //SANDBOX CREDS
//        val clientId = "AI52KOUVC2PQTONW1ZKB92RU22UL8491"
//        val clientSecret = "B6DXG4SV4YJJC2VDA54WTLY6CTJKEUZH"

        //PRD CREDS
        val clientId = "AIZ1SHB77YJBZ6HFAGYR4BTUI84A6DOF"
        val clientSecret = "ZLYKT9FT7UUAIZGVVUPIWSFN3N62Y99O"

        val base = "$clientId:$clientSecret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        ApiClient().getBankDetailsApiService(this).createRequestDigilocker(authHeader, jsonObject)
            .enqueue(object : Callback<DigilockerTokenResponse> {
                override fun onResponse(
                    call: Call<DigilockerTokenResponse>,
                    response: Response<DigilockerTokenResponse>
                ) {
                    hideProgressDialog()
                    val accessToken = response.body()!!.accessToken1
                    tokenId = accessToken.token
                    kId = accessToken.kId

                    val permissionCamera = ContextCompat.checkSelfPermission(
                        this@VideoKyc,
                        Manifest.permission.CAMERA
                    )
                    val permissionRecordAudio = ContextCompat.checkSelfPermission(
                        this@VideoKyc,
                        Manifest.permission.RECORD_AUDIO
                    )
                    val permissionLocation = ContextCompat.checkSelfPermission(
                        this@VideoKyc,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    val permissionRead = ContextCompat.checkSelfPermission(
                        this@VideoKyc,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionRecordAudio == PackageManager.PERMISSION_GRANTED && permissionLocation == PackageManager.PERMISSION_GRANTED && permissionRead == PackageManager.PERMISSION_GRANTED) {
                        customerData!!.mobNo?.let { startVideoKyc(tokenId, kId, it) }
                    } else {
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ), MY_CAMERA_PERMISSION_CODE
                        )
                    }

                    //startVideoKyc(tokenId, kId, "7609963809")

                }

                override fun onFailure(call: Call<DigilockerTokenResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(
                        this@VideoKyc,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun requestPermission(): Boolean {

        if (this.checkSelfPermission(Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED
            && this.checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
            && this.checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ), MY_CAMERA_PERMISSION_CODE
            )
        } else {
            return true
        }

        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val permissionCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val permissionRecordAudio = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionCamera == PackageManager.PERMISSION_GRANTED && permissionRecordAudio == PackageManager.PERMISSION_GRANTED && permissionLocation == PackageManager.PERMISSION_GRANTED && permissionRead == PackageManager.PERMISSION_GRANTED) {
            startVideoKyc(tokenId, kId, customerData!!.mobNo.toString())
        } else {
            requestPermission()
            // Toast.makeText(this, "Permission denied by user. Could not able to record video.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVideoKyc(tokenId: String, kId: String, mobNo: String) {
        if (requestPermission()) {
            try {
                val config = DigioKycConfig()
                config.setEnvironment(DigioEnvironment.PRODUCTION)
                val digioSession = DigioSession()
                digioSession.init(this, config)
                digioSession.startSession(
                    kId,
                    mobNo,
                    tokenId,
                    this
                )
            } catch (e: Exception) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDigioKycFailure(requestId: String?, response: String?) {
        Toast.makeText(this, "Video KYC failed, Please Try again", Toast.LENGTH_SHORT).show()
//        val intent = Intent(
//            this@VideoKyc,
//            UploadBusinessDetailsActivity::class.java
//        )
//        intent.putExtra("customerData", customerData)
//        startActivity(intent)
//        finish()

    }

    override fun onDigioKycSuccess(requestId: String?, response: String?) {
        updateStage("VKYC_PA")
    }

    fun showProgressDialog(message: String = "Loading...") {
        if (progressView == null) {
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            val inflater = LayoutInflater.from(this)
            progressView =
                inflater.inflate(com.arthanfinance.core.R.layout.progress_layout, null, true)
            progressView?.isEnabled = false
            progressView?.setOnClickListener { v: View? -> }
            progressView?.findViewById<TextView>(com.arthanfinance.core.R.id.txtMessage)?.text =
                message
            rootLayout.addView(progressView)
            progressView?.bringToFront()
        }
    }

    fun hideProgressDialog() {
        progressView?.let {
            progressView?.setVisibility(View.GONE)
            progressView!!.findViewById<TextView>(com.arthanfinance.core.R.id.txtMessage).text = ""
            val vg = progressView?.getParent() as ViewGroup
            vg.removeView(progressView)
            progressView = null
        }
    }

    private fun updateStage(stage: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData!!.customerId)
        jsonObject.addProperty("stage", stage)
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@VideoKyc, "Current Stage not updated.",
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
                        this@VideoKyc,
                        UploadBusinessDetailsActivity::class.java
                    )
                    intent.putExtra("customerData", customerData)
                    startActivity(intent)
                    finish()
                }

            }
        })
    }
}