package com.av.arthanfinance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import `in`.digio.sdk.kyc.nativeflow.DigioTaskRequest
import `in`.digio.sdk.kyc.nativeflow.DigioIDCardType
import `in`.digio.sdk.kyc.nativeflow.DigioTaskType
import `in`.digio.sdk.kyc.DigioStateLessSession

import `in`.digio.sdk.kyc.DigioEnvironment

import `in`.digio.sdk.kyc.DigioKycConfig
import android.graphics.Color
import java.lang.Exception

import `in`.digio.sdk.kyc.DigioTaskResponse
import `in`.digio.sdk.kyc.callback.DigioResponseListener
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException


class DemoActivity : AppCompatActivity(), DigioResponseListener {
    var digioTaskList: ArrayList<DigioTaskRequest> = ArrayList()
    var idCardList: ArrayList<DigioIDCardType> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        val kycRequest = DigioTaskRequest()
        kycRequest.taskType = DigioTaskType.E_AADHAAR// For Aadhaar card
        kycRequest.isFaceMatch = false // Optional,  In case business required selfie and face match with Aadhaar
        digioTaskList.add(kycRequest)

        val config = DigioKycConfig()
        config.setEnvironment(DigioEnvironment.SANDBOX)
        config.setPrimaryColor(Color.parseColor("#17c39b"))
        config.setSecondaryColor(Color.parseColor("#B4E9D8"))

        val clientID  = encodeString("SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X")
        val clientSecret  = encodeString("SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB")

        try {
            val digioStateLessSession = DigioStateLessSession()
            digioStateLessSession.init(this, config,"SKW8OI861BHP5Q3V9KM28E4O2QXIFT4X", "SA9HJEPCV83EFY3XHH6Q1CE168O8JWNB")
            digioStateLessSession.startStateLessSession(digioTaskList, this)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun encodeString(str: String): String? {
        val data: ByteArray
        var base64 = ""
        try {
            data = str.toByteArray(charset("UTF-8"))
            base64 = Base64.encodeToString(data, Base64.NO_WRAP)

        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return base64
    }

    override fun onDigioEventTracker(eventTracker: JSONObject) {


    }

    override fun onDigioResponseFailure(failure: List<DigioTaskResponse>) {
        for (digiTaskResponse in failure){
            val value = digiTaskResponse.isSuccess
            Toast.makeText(this, "${value}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDigioResponseSuccess(taskResponseList: List<DigioTaskResponse>) {
        for (digiTaskResponse in taskResponseList) {
            val mainResponse = digiTaskResponse.getResponse() // offline_kyc or idCard analysis response
            val digioTaskRequest = digiTaskResponse.getTask()
            val value = digiTaskResponse.isSuccess
            val personalInfo = mainResponse.getJSONObject("personal_information")
            val name = personalInfo.getString("name")

            Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
        }
    }
}



