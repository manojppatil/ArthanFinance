package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.R
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraUtils
import com.otaliastudios.cameraview.Flash
import kotlinx.android.synthetic.main.activity_customer_camera.*
import java.io.File


class CustomerCameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_camera)
        if (supportActionBar != null)
            supportActionBar?.hide()

        camera.clearCameraListeners()
        camera.setLifecycleOwner(this)
        camera.addCameraListener(object : CameraListener() {

            @SuppressLint("CheckResult")
            override fun onPictureTaken(picture: ByteArray?) {
                super.onPictureTaken(picture)
                CameraUtils.decodeBitmap(picture) { it ->
                    //Here comes your bitmap
                    BitmapUtils.saveBitmapToFile(it, getOutputMediaFile())
                        .subscribe({
                            run {
                                val resultIntent = Intent()
                                resultIntent.putExtra("FilePath", getOutputMediaFile().absolutePath)
                                resultIntent.putExtra("doc_type", intent.getIntExtra("doc_type", 0))
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        }, { error -> error.message?.let { Log.e("ERROR::", it) } })
                }
            }
        })

        btn_capture.setOnClickListener {
            camera.capturePicture()
        }

        btn_front_camera.setOnClickListener {
            camera.toggleFacing()
        }

        btn_flash_off.setOnClickListener {
            camera.flash = Flash.ON
            btn_flash_off.visibility = View.GONE
            btn_flash_on.visibility = View.VISIBLE
        }

        btn_flash_on.setOnClickListener {
            camera.flash = Flash.OFF
            btn_flash_off.visibility = View.VISIBLE
            btn_flash_on.visibility = View.GONE
        }

        if (intent.getBooleanExtra("is_front", false)) {
            camera.toggleFacing()
        }
    }

    private fun getOutputMediaFile(): File {
        return File(intent.getStringExtra("FilePath") ?: "")
    }

    override fun onResume() {
        super.onResume()
        try {
            camera.start()
        } catch (e: Exception) {
            camera.start()
        }
    }


    override fun onPause() {
        super.onPause()
        camera.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.destroy()
    }
}