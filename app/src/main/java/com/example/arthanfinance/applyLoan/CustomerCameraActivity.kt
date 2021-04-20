package com.example.arthanfinance.applyLoan

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.arthanfinance.R
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraUtils
import kotlinx.android.synthetic.main.activity_customer_camera.*
import java.io.File


class CustomerCameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_camera)
        if (supportActionBar != null)
            supportActionBar?.hide()

        camera.clearCameraListeners()
        camera.addCameraListener(object : CameraListener() {

            override fun onPictureTaken(picture: ByteArray?) {
                super.onPictureTaken(picture)
                CameraUtils.decodeBitmap(picture) {
                    //Here comes your bitmap
                    BitmapUtils.saveBitmapToFile(it, getOutputMediaFile())
                        .subscribe({ filePath ->
                            run {
                                val resultIntent = Intent()
                                resultIntent.putExtra("FilePath", getOutputMediaFile().absolutePath)
                                resultIntent.putExtra("doc_type", intent.getIntExtra("doc_type", 0))
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        }, {
                                error -> error.message?.let { Log.e("ERROR::", it) } })
                }
            }
        })

        btn_capture.setOnClickListener {
            camera.capturePicture()
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