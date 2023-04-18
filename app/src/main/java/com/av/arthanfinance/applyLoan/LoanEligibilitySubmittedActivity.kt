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

    override val layoutId: Int
        get() = R.layout.activity_loan_eligibility_submitted


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoanEligibilitySubmitted =
            ActivityLoanEligibilitySubmittedBinding.inflate(layoutInflater)
        setContentView(activityLoanEligibilitySubmitted.root)

        val mPrefs: SharedPreferences? = getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)

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

        if (supportActionBar != null)
            supportActionBar?.hide()
        btnDone = findViewById(R.id.btn_dashboard)

        btnDone.setOnClickListener {
            updateStage(ArthanFinConstants.eligibility)
        }
        object : CountDownTimer(15000, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {
                val minute = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                activityLoanEligibilitySubmitted.clock.text = "$minute:$seconds"
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                activityLoanEligibilitySubmitted.animationView.visibility = View.VISIBLE
                activityLoanEligibilitySubmitted.clock.visibility = View.GONE
                activityLoanEligibilitySubmitted.btnDashboard.visibility = View.VISIBLE
            }
        }.start()

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
                response: Response<AuthenticationResponse>
            ) {
                hideProgressDialog()
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
        }
    }
}