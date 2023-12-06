package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.databinding.ActivityLoanEligibilitySubmittedBinding
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class LoanEligibilitySubmittedActivity : BaseActivity() {
    private lateinit var activityLoanEligibilitySubmitted: ActivityLoanEligibilitySubmittedBinding
    private lateinit var btnDone: Button
    private var mCustomerId: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null
    private var done_by: String = ""

    override val layoutId: Int
        get() = R.layout.activity_loan_eligibility_submitted

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanEligibilitySubmitted =
            ActivityLoanEligibilitySubmittedBinding.inflate(layoutInflater)
        setContentView(activityLoanEligibilitySubmitted.root)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)

        if (supportActionBar != null)
            supportActionBar?.hide()
        btnDone = findViewById(R.id.btn_dashboard)

        btnDone.setOnClickListener {
            updateStage(ArthanFinConstants.eligibility)
        }

        done_by = intent.getStringExtra("done_by").toString()

        if (done_by == "aggregator") {
            activityLoanEligibilitySubmitted.submitTitle.text = "Congratulations!!!"
            activityLoanEligibilitySubmitted.submitSubTitle.text =
                "Your Arthik Account has been created"
            activityLoanEligibilitySubmitted.submitDesc.text =
                "You're on your way to becoming a successful businessman."
            btnDone.visibility = View.VISIBLE
            activityLoanEligibilitySubmitted.animationView.visibility = View.VISIBLE
        } else {
            activityLoanEligibilitySubmitted.submitTitle.text = "We're Opening your Arthik Account!"
            activityLoanEligibilitySubmitted.submitSubTitle.text = "This can upto 3 minutes"
            activityLoanEligibilitySubmitted.submitDesc.text =
                "You're on your way to becoming a successful businessman."
            object : CountDownTimer(180000, 1000) {

                // Callback function, fired on regular interval
                override fun onTick(millisUntilFinished: Long) {
                    val minute = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    activityLoanEligibilitySubmitted.clock.text = "$minute:$seconds"
                }

                // Callback function, fired
                // when the time is up
                override fun onFinish() {
                    activityLoanEligibilitySubmitted.clock.visibility = View.GONE
                    getCustomerEligibility()
                }
            }.start()
        }

//        val registerTime = intent.getStringExtra("registerTime")
//        val calendar = Calendar.getInstance()
//        val mdformat = SimpleDateFormat("HH:mm:ss")
//        val currentTime = mdformat.format(calendar.time)
//        Log.e("currentTime", currentTime)
//
//        Log.e("TAGTime", "$registerTime//$currentTime")
//
//        val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
//
//        val date1 = simpleDateFormat.parse(registerTime!!)
//        val date2 = simpleDateFormat.parse(currentTime.toString())
//        Log.e("TAGDate", "$date1//$date2")
//
//        val difference: Long = date2!!.time - date1!!.time
//        val mills: Long = Math.abs(difference)
//
//        val Hours = (mills / (1000 * 60 * 60)).toInt()
//        val Mins = (mills / (1000 * 60)).toInt() % 60
//        val Secs = ((mills / 1000).toInt() % 60).toLong()
//        Log.i("======= Hours", " :: $Hours:$Mins:$Secs")
//
//        activityLoanEligibilitySubmitted.totalTime.text =
//            "You just finished your loan application in a speedy $Mins Minutes and $Secs Seconds"

    }

    @SuppressLint("SetTextI18n")
    private fun getCustomerEligibility() {
        ApiClient().getAuthApiService(this).getCustomerEligibilityV2(mCustomerId.toString())
            .enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(
                        this@LoanEligibilitySubmittedActivity, "Current Stage not updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }


                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>,
                ) {
//                if (response.body()?.apiCode == "200") {
                    clevertapDefaultInstance?.pushEvent("Eligibility Status fetched")//added by CleverTap Assistant
                    if (response.body()!!.eligibilityStatus == "Yes") {
                        activityLoanEligibilitySubmitted.submitTitle.text = "Congratulations!!!"
                        activityLoanEligibilitySubmitted.submitSubTitle.text =
                            "Your Arthik Account is ready"
                        btnDone.visibility = View.VISIBLE
                        activityLoanEligibilitySubmitted.animationView.visibility = View.VISIBLE
                    } else if (response.body()!!.eligibilityStatus == "Banking data not avaialble") {
                        val intent = Intent(
                            this@LoanEligibilitySubmittedActivity,
                            LoanEligibilityFailed::class.java
                        )
                        intent.putExtra("from", "banking")
                        startActivity(intent)
                        finish()
                    }
//                }
                }
            })
    }

    private fun updateStage(stage: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("stage", stage)
        showProgressDialog()
        ApiClient().getAuthApiService(this).updateStage(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    this@LoanEligibilitySubmittedActivity, "Current Stage not updated.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>,
            ) {
                hideProgressDialog()
                clevertapDefaultInstance?.pushEvent("Eligibility set")//added by CleverTap Assistant
                if (response.body()?.apiCode == "200") {
                    val intent = Intent(
                        this@LoanEligibilitySubmittedActivity,
                        HomeDashboardActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                }
            }
        })
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            clevertapDefaultInstance?.pushEvent("Back from eligibility")//added by CleverTap Assistant

        }
    }
}