package com.av.arthanfinance

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.av.arthanfinance.databinding.ActivityMapLocationBinding
import com.av.arthanfinance.user_kyc.UploadAadharAddressActivity
import com.av.arthanfinance.util.GpsUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import java.util.*

class MapLocationActivity : FragmentActivity(), OnMapReadyCallback {
    private lateinit var activityMapLocationBinding: ActivityMapLocationBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map: GoogleMap
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private val isContinue = false
    private var isGPS = false
    private val REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2
    private val gpsUtils = GpsUtil()
    private var onGpsListener: GpsUtil.onGpsListener? = null

    private var address: String = ""
    private var subLocality: String = ""
    private var city: String = ""
    private var state: String = ""
    private var country: String = ""
    private var postalCode: String = ""
    private lateinit var knownName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapLocationBinding = ActivityMapLocationBinding.inflate(layoutInflater)
        setContentView(activityMapLocationBinding.root)

        locationRequest = LocationRequest.create()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = (10 * 1000).toLong() // 10 seconds
        locationRequest!!.fastestInterval = (5 * 1000).toLong()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        gpsUtils.GpsUtil(this)
        onGpsListener?.let { gpsUtils.turnGPSOn(it) }


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.equals(null)) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude

                        placeMarker(wayLatitude, wayLongitude)
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient!!.removeLocationUpdates(locationCallback!!)
                        }
                    }
                }
            }
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        activityMapLocationBinding.cvProceed.setOnClickListener {
            val intent = Intent(this, UploadAadharAddressActivity::class.java)
            intent.putExtra("address", address)
            intent.putExtra("sub_locality", subLocality)
            intent.putExtra("city", city)
            intent.putExtra("state", state)
            intent.putExtra("postalCode", postalCode)
            setResult(101, intent)
            finish()
        }
    }

    private fun getLocation() {
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (permissionLocation == PackageManager.PERMISSION_DENIED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (listPermissionsNeeded.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toTypedArray(),
                    REQUEST_ID_MULTIPLE_PERMISSIONS
                )

            } else {
                Toast.makeText(this, "Permission access failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            getLatLong()
            activityMapLocationBinding.cvLoader.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val listPermissionsNeeded: List<String> = ArrayList()
        val permissionLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getLatLong()
        } else {
            getLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isRotateGesturesEnabled = true

        getLocation()

    }

    private fun placeMarker(wayLatitude: Double, wayLongitude: Double) {
        val userLocation = LatLng(wayLatitude, wayLongitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(wayLatitude, wayLongitude),
                18f
            )
        )

        map.addMarker(
            MarkerOptions()
                .position(userLocation)
                .title("Marker in Sydney")
        )

        getAddress(wayLatitude, wayLongitude)
        map.setOnMapClickListener {
            map.clear()
            map.addMarker(MarkerOptions().position(it))
            val latitude = it.latitude
            val longitude = it.longitude

            getAddress(latitude, longitude)
        }
    }

    private fun getAddress(wayLatitude: Double, wayLongitude: Double) {
        val addresses: List<Address>
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(
                wayLatitude,
                wayLongitude,
                1
            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address =
                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val subAdminArea: String = addresses[0].subAdminArea
            //subLocality = addresses[0].subLocality
            city = addresses[0].locality
            state = addresses[0].adminArea
            country = addresses[0].countryName
            postalCode = addresses[0].postalCode
            val knownName: String = addresses[0].featureName

            activityMapLocationBinding.cvLoader.visibility = View.GONE
            activityMapLocationBinding.tvAddress.text = address
            activityMapLocationBinding.cvProceed.visibility = View.VISIBLE
            Toast.makeText(this, postalCode, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showEnableLocationSetting() {
        this.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                if (states!!.isLocationPresent) {
                    //Do something
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(
                            it,
                            LOCATION_SETTING_REQUEST
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }

    companion object {
        const val LOCATION_SETTING_REQUEST = 999
    }

    private fun getLatLong() {
        if (isContinue) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            showEnableLocationSetting()
            mFusedLocationClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                Looper.myLooper()!!
            )

        } else {
            mFusedLocationClient!!.lastLocation.addOnSuccessListener(this,
                OnSuccessListener { location ->
                    if (location != null) {
                        wayLatitude = location.latitude
                        wayLongitude = location.longitude
                        placeMarker(wayLatitude, wayLongitude)
                    } else {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@OnSuccessListener
                        }
                        showEnableLocationSetting()
                        mFusedLocationClient!!.requestLocationUpdates(
                            locationRequest!!,
                            locationCallback!!, Looper.myLooper()!!
                        )
                    }
                })
        }
    }
}