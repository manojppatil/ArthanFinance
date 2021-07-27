package com.arthanfinance.core.base

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthanfinance.core.R
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
abstract class BaseActivity : AppCompatActivity() {


    var currentLocation: Location? = null

    @get:LayoutRes
    abstract val layoutId: Int

    private var progressView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
    }

    fun showSnackbarMessage(message: String,isSuccess:Boolean = false) {
        val rootLayout = findViewById<FrameLayout>(android.R.id.content)
        val snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG);
        val view: View = snackbar.getView()
        val textView = view.findViewById<TextView>(R.id.snackbar_text)
        textView.compoundDrawablePadding = 8
        textView.setTextSize(16f)
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        snackbar.show()
    }

    /**
     * Shows progress in an activity
     */
    fun showProgressDialog(message: String = "Loading...") {
        if (progressView == null) {
            val rootLayout = findViewById<FrameLayout>(android.R.id.content)
            val inflater = LayoutInflater.from(this)
            progressView = inflater.inflate(R.layout.progress_layout, null, true)
            progressView?.isEnabled = false
            progressView?.setOnClickListener { v: View? -> }
            progressView?.findViewById<TextView>(R.id.txtMessage)?.text = message
            rootLayout.addView(progressView)
            progressView?.bringToFront()
        }
    }

    /**
     * Hides progress in an activity
     */
    fun hideProgressDialog() {
        progressView?.let {
            progressView?.setVisibility(View.GONE)
            progressView!!.findViewById<TextView>(R.id.txtMessage).text = ""
            val vg = progressView?.getParent() as ViewGroup
            vg.removeView(progressView)
            progressView = null
        }
    }

    fun setUpLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                500
            )
            return
        }

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    currentLocation = location
                    locationUpdateDone()
                    fusedLocationProviderClient.removeLocationUpdates(this)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper())

    }

     fun locationUpdateDone(){

     }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }


}
