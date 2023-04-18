package com.av.arthanfinance.serviceRequest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.R
import kotlinx.android.synthetic.main.activity_getintouch.*

class Getintouch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_getintouch)

        img_back.setOnClickListener {
            finish()
        }

        iv_call_icon.setOnClickListener {
            val i = Intent(Intent.ACTION_DIAL)
            val p = "tel:" + getString(R.string.phone_number)
            i.data = Uri.parse(p)
            startActivity(i)
        }

        wp_button.setOnClickListener {

            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    Uri.parse("whatsapp://send?phone=+91 8007339339\"text=The text message goes here")
                startActivity(i)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Whatsapp not installed!", Toast.LENGTH_LONG)
                    .show()
            }
        }

        lyt_facebook.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/arthanfinance"))
            startActivity(i)
        }

        lyt_linkedin.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/company/arthanfinance/"))
            startActivity(i)
        }

        lyt_youtube.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@arthanfinance"))
            startActivity(i)
        }

        lyt_instagram.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/arthanfinance/"))
            startActivity(i)
        }
    }


}