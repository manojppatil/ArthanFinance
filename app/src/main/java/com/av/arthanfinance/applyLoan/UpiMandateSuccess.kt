package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.util.Sms
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.CustomTabMainActivity.EXTRA_URL
import kotlinx.android.synthetic.main.activity_upi_mandate_success.*
import java.util.*
import java.util.regex.Pattern

class UpiMandateSuccess : AppCompatActivity() {
    private lateinit var btnDone: Button
    private lateinit var clockNach: AppCompatTextView
    private lateinit var clockMsgs: AppCompatTextView
    var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null
    var clevertapDefaultInstance: CleverTapAPI? = null

    private val SENDER_ID = "TX-eDIGIO" // Trusted sender ID

    val INBOX = "content://sms/inbox"
    val SENT = "content://sms/sent"
    val DRAFT = "content://sms/draft"
    val REQUEST_CODE_ASK_PERMISSIONS = 123
    var smsMsgs: ArrayList<Sms> = ArrayList<Sms>()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upi_mandate_success)
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)//added by CleverTap Assistant
        val mPrefs: SharedPreferences? =
            getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)

        ActivityCompat.requestPermissions(this@UpiMandateSuccess,
            arrayOf("android.permission.READ_SMS"),
            REQUEST_CODE_ASK_PERMISSIONS)

        btnDone = findViewById(R.id.btn_done)
        clockNach = findViewById(R.id.clock_nach)
        clockMsgs = findViewById(R.id.clock_msgs)

        if (intent.getStringExtra("from").equals("agreement")) {
            txt_title.text = "E-Agreement Initiated Successfully"
            txt_subtitle.text = "Thank you for selecting E-Agreement registration"
            clevertapDefaultInstance?.pushEvent("Agreement SMS sent")//added by CleverTap Assistant

        } else if (intent.getStringExtra("from").equals("nach")) {
            txt_title.text = "E-NACH Mandate Initiated"
            txt_subtitle.text = "Thank you for selecting E-NACH Mandate registration"
            clevertapDefaultInstance?.pushEvent("Nach mandate SMS sent")//added by CleverTap Assistant
        }

        object : CountDownTimer(15000, 1000) {

            // Callback function, fired on regular interval
            override fun onTick(millisUntilFinished: Long) {
                val minute = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                clock_nach.text = "$minute:$seconds"
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                getAllSmse(this@UpiMandateSuccess)
                smsMsgs = ArrayList()
                clockNach.visibility = View.GONE
                clockMsgs.text = "Please click on the refresh button to retrieve your SMS again."
                btnDone.visibility = View.VISIBLE
            }
        }.start()

        btnDone.setOnClickListener {
            getAllSmse(this@UpiMandateSuccess)
            smsMsgs = ArrayList()
//            val intent = Intent(
//                this@UpiMandateSuccess,
//                HomeDashboardActivity::class.java
//            )
//            startActivity(intent)
//            finish()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun getAllSmse(context: Context) {
        Log.e("TAGjjj", "************")
        ActivityCompat.requestPermissions(this@UpiMandateSuccess,
            arrayOf("android.permission.READ_SMS"),
            REQUEST_CODE_ASK_PERMISSIONS)
        if (ContextCompat.checkSelfPermission(baseContext,
                "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED
        ) {
            val cr = context.contentResolver
            Log.e("TAG0", "************")
            val sortOrder = "${Telephony.Sms.Inbox.DATE} DESC LIMIT 1"
            val c = cr.query(Uri.parse("content://sms/inbox"), null, null, null, sortOrder)
            Log.e("TAG3", "************")
            var totalSMS = 0
            if (c != null) {
                totalSMS = c.count
                Log.e("TAGTOTAL", totalSMS.toString() + "")
                if (c.moveToFirst()) {
                    val smsMsg = Sms()
                    for (j in 0 until totalSMS) {
                        val smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        val number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                        val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                        var type: String? = null
                        smsMsg.setTime(smsDate)
                        smsMsg.setAddress(number)
                        smsMsg.setMsg(body)
                        smsMsg.setReadState(type)
                        smsMsg.setId(j.toString() + "")
                        smsMsgs.add(smsMsg)
                        Log.e("TAGSMSE", smsMsgs[0].getAddress().toString())
                        if (smsMsgs[0].getAddress().toString() == SENDER_ID) {
                            val smsbody: String = smsMsgs[0].getMsg().toString()
                            Log.e("TAGBODY", smsbody)
                            val url = extractUrlFromSms(smsbody)
                            println(url)
                            if (url != null) {
                                openWebView(url)
                            }
                        }
                        c.moveToNext()
                    }
                }
                c.close()
            } else {
                Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to extract URL from a string using regular expressions
    fun extractUrlFromSms(smsBody: String): String? {
        val pattern =
            Pattern.compile("\\b((?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])")
        val matcher = pattern.matcher(smsBody)

        if (matcher.find()) {
            return matcher.group()
        }

        return null
    }

    private fun openWebView(url: String?) {
        if (url != null) {
            val builder = CustomTabsIntent.Builder()
            builder.setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
            builder.setToolbarColor(Color.parseColor("#003FB2"))
            builder.setStartAnimations(this@UpiMandateSuccess,
                R.anim.enter_from_left,
                R.anim.exit_to_left)

            val customTabsIntent = builder.build()

            // Launch the URL in a Chrome Custom Tab
            customTabsIntent.launchUrl(this, Uri.parse(url))

        } else {
            getAllSmse(this@UpiMandateSuccess)
            smsMsgs = ArrayList()
        }

    }

}


