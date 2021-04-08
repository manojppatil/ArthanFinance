package com.example.arthanfinance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.arthanfinance.applyLoan.UploadKycDetailsActivity
import com.nttdata.mobillitysecurityframework.biometric.BiometricAuthentication
import com.nttdata.mobillitysecurityframework.msfcomponents.MSFCallback

class FingerPrintLoginActivity : AppCompatActivity() {
    private lateinit var btnFingerPrint: Button
    private lateinit var imgFingerPrint: ImageView
    private lateinit var btnMpinLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login_fingerprint)

        if (supportActionBar != null)
            supportActionBar?.hide()

        btnFingerPrint = findViewById(R.id.btn_fingerprint)
        imgFingerPrint = findViewById(R.id.img_fingerprint)
        btnMpinLogin = findViewById(R.id.btn_login_mpin)
        btnFingerPrint.setOnClickListener{
            val intent = Intent(this@FingerPrintLoginActivity, FaceLoginActivity::class.java)
            startActivity(intent)
        }
        imgFingerPrint.setOnClickListener{
            BiometricAuthentication.startScanning(this, "Fingerprint Login",
                "",
                "",
                object :
                    MSFCallback {
                    override fun onSuccess(message: String) {

                        runOnUiThread {
                            Toast.makeText(this@FingerPrintLoginActivity, message, Toast.LENGTH_LONG).show()
                            val intent = Intent(this@FingerPrintLoginActivity, UploadKycDetailsActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onFailure(errorString: String) {
                        runOnUiThread {
                            Toast.makeText(this@FingerPrintLoginActivity, errorString, Toast.LENGTH_LONG).show()
                        }
                    }

                })
        }
        btnMpinLogin.setOnClickListener{
            val intent = Intent(this@FingerPrintLoginActivity, MPINLoginActivity::class.java)
            startActivity(intent)
        }
    }
}