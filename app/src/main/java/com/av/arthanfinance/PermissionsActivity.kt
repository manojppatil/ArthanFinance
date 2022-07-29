package com.av.arthanfinance


import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.Manifest.permission.*
import android.content.Intent
import android.content.SharedPreferences
import com.av.arthanfinance.databinding.ActivityPermissionsBinding
import com.av.arthanfinance.introductionPager.IntroductionPagerActivity

class PermissionsActivity : AppCompatActivity() {
    private lateinit var activityPermissionsBinding: ActivityPermissionsBinding

    private var REQUEST_PERMISSION_LOCATION = 100
    private var REQUEST_PERMISSION_READ_SMS = 101
    private var REQUEST_PERMISSION_READ_CONTACTS = 103
    private var REQUEST_PERMISSION_READ_PHONE_STATE = 104

    private var locationStatus = ""
    private var smsStatus = ""
    private var contactStatus = ""
    private var phoneStateStatus = ""

    private var mContext: Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPermissionsBinding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(activityPermissionsBinding.root)

        activityPermissionsBinding.btnGrantAccess.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val permissionLocation = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        val permissionReadSms = ContextCompat.checkSelfPermission(this, READ_SMS)
        val permissionReadContacts = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        val permissionReadPhoneState = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE)

        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.requirePermission)

            val permissions = arrayOf("Location", "SMS", "Contact", "Phone State")
            builder.setItems(permissions) { _, _ ->

            }

            builder.setPositiveButton("Yes, I agree") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
            }

            val dialog = builder.create()
            dialog.show()

        }else if (permissionReadSms != PackageManager.PERMISSION_GRANTED){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.readSmsDialogTitle)
            builder.setMessage(R.string.readSmsDialogMessage)
            builder.setIcon(R.drawable.ic_alert_new)

            builder.setPositiveButton("Yes, I agree") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_SMS),
                    REQUEST_PERMISSION_READ_SMS
                )
            }

            builder.setNeutralButton("No") { _, _ ->
                finish()
            }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else if (permissionReadContacts != PackageManager.PERMISSION_GRANTED){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.contactDialogTitle)
            builder.setMessage(R.string.contactDialogMessage)
            builder.setIcon(R.drawable.ic_alert_new)

            builder.setPositiveButton("Yes, I agree") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_CONTACTS),
                    REQUEST_PERMISSION_READ_CONTACTS
                )
            }

            builder.setNeutralButton("No") { _, _ ->
                finish()
            }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }else if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED){
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.readPhoneStateDialogTitle)
            builder.setMessage(R.string.readPhoneStatesDialogMessage)
            builder.setIcon(R.drawable.ic_alert_new)

            builder.setPositiveButton("Yes, I agree") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_PHONE_STATE),
                    REQUEST_PERMISSION_READ_PHONE_STATE
                )
            }

            builder.setNeutralButton("No") { _, _ ->
                finish()
            }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionLocation = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        val permissionReadSms = ContextCompat.checkSelfPermission(this, READ_SMS)
        val permissionReceiveSms = ContextCompat.checkSelfPermission(this, RECEIVE_SMS)
        val permissionReadContacts = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        val permissionGetAccounts = ContextCompat.checkSelfPermission(this, GET_ACCOUNTS)
        val permissionReadPhoneState = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE)

        val showRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)

        if (locationStatus == "") {
            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                locationStatus = "1"
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_SMS),
                    REQUEST_PERMISSION_READ_SMS
                )
                //Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
            } else if (permissionLocation == PackageManager.PERMISSION_DENIED) {
                /*locationStatus = "1"
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_SMS),
                    REQUEST_PERMISSION_READ_SMS
                )*/
                finish()
            }

        } else if (smsStatus == "") {
            if (permissionReadSms == PackageManager.PERMISSION_GRANTED) {
                smsStatus = "1"
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_CONTACTS),
                    REQUEST_PERMISSION_READ_CONTACTS
                )

            } else if (permissionReadSms == PackageManager.PERMISSION_DENIED) {
                //smsStatus = "1"
                /*ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_CONTACTS),
                    REQUEST_PERMISSION_READ_CONTACTS
                )*/
                finish()

            }

        } else if (contactStatus == "") {
            if (permissionReadContacts == PackageManager.PERMISSION_GRANTED) {
                contactStatus = "1"
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_PHONE_STATE),
                    REQUEST_PERMISSION_READ_PHONE_STATE
                )

            } else if (permissionReadContacts == PackageManager.PERMISSION_DENIED) {
                //contactStatus = "1"
                /* ActivityCompat.requestPermissions(
                     this,
                     arrayOf(READ_PHONE_STATE),
                     REQUEST_PERMISSION_READ_PHONE_STATE
                 )*/
                finish()

            }

        } else if (phoneStateStatus == "") {
            if (permissionReadPhoneState == PackageManager.PERMISSION_GRANTED) {
                phoneStateStatus = "1"
                Toast.makeText(this, "Phone state permission granted", Toast.LENGTH_SHORT).show()

                val sharedPref: SharedPreferences = getSharedPreferences("isFirstTime", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isFirstTime", false)
                editor.apply()

                val intent = Intent(this, IntroductionPagerActivity::class.java)
                startActivity(intent)
                finish()

            } else if (permissionReadPhoneState == PackageManager.PERMISSION_DENIED) {
                //phoneStateStatus = "1"
                finish()

            }
        } else {
            val intent = Intent(this, IntroductionPagerActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}

/*val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.receiveSmsDialogTitle)
                    builder.setMessage(R.string.readSmsDialogMessage)
                    builder.setIcon(R.drawable.ic_alert_new)

                    builder.setPositiveButton("Yes") { dialogInterface, which ->
                        checkPermissions()
                    }

                    builder.setNeutralButton("Cancel") { dialogInterface, which ->
                        finish()
                    }

                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()*/

