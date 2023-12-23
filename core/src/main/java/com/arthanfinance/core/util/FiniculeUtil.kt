package com.arthanfinance.core.util

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.InputStream

object FiniculeUtil {

    const val LOCATION_CODE = 2001

    fun expand(v: View) {
        val matchParentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec((v.parent as View).width, View.MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = 1
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                v.layoutParams.height =
                    if (interpolatedTime == 1f) WindowManager.LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Expansion speed of 1dp/ms
        a.duration = ((targetHeight / v.context.resources.displayMetrics.density).toLong())
        v.startAnimation(a)
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Collapse speed of 1dp/ms
        a.duration = ((initialHeight / v.context.resources.displayMetrics.density).toLong())
        v.startAnimation(a)
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationMode: Int = try {
            Settings.Secure
                .getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: SettingNotFoundException) {
            return false
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }


    /**
     * Function to request permission from the user
     */
    fun requestAccessFineLocationPermission(activity: AppCompatActivity, requestId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestId
        )
    }

    /**
     * Function to check if the location permissions are granted or not
     */
    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Function to show the "enable GPS" Dialog box
     */
    fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Enable Location service")
            .setMessage("Please Enable Location Service")
            .setCancelable(false)
            .setPositiveButton("Enable Now") { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

    fun getBytes(inputStream: InputStream): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

//    /**
//     * Pojo class convert to string format * @param object model class
//     *
//     * @return string format
//     * @throws Exception parameter not matching
//     */
//    fun pojoToJson(`object`: Any?): String? {
//        var jsonString = ""
//        try {
//            val gson = Gson()
//            jsonString = gson.toJson(`object`)
//        } catch (e: Exception) {
//        }
//        return jsonString
//    }
//
//    /**
//     * String convert to pojo class format * * @param json string to parse
//     *
//     * @param pojoClass class of object in which json will be parsed
//     * @param <T> generic parameter for tClass
//     * @return mapped T class instance
//     * @throws Exception parameter not matching
//    </T> */
//    fun <T> jsonToPojo(json: String?, pojoClass: Class<T>?): T? {
//        try {
//            val gson = Gson()
//            return gson.fromJson(json, pojoClass)
//        } catch (e: Exception) {
//        }
//        return null
//    }
//
//    /**
//     * String convert to pojo class format * * @param json string to parse
//     *
//     * @param pojoClass class of object in which json will be parsed
//     * @param <T> generic parameter for tClass
//     * @return mapped T class instance
//     * @throws Exception parameter not matching
//    </T> */
//    fun <T> jsonToPojoWithSerializeNulls(json: String?, pojoClass: Class<T>?): T? {
//        try {
//            val gson = GsonBuilder().serializeNulls().create()
//            return gson.fromJson(json, pojoClass)
//        } catch (e: Exception) {
//        }
//        return null
//    }
//
//    /**
//     * Pojo class convert to string format * @param object model class
//     *
//     * @return string format
//     * @throws Exception parameter not matching
//     */
//    fun pojoToJsonWithExcludeOption(`object`: Any?): String? {
//        var jsonString = ""
//        try {
//            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
//            jsonString = gson.toJson(`object`)
//        } catch (e: Exception) {
//        }
//        return jsonString
//    }
}