package com.av.arthanfinance.applyLoan

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent.Builder
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_SYSTEM
import com.av.arthanfinance.R

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val url = intent.getStringExtra("smsUrl")
//        val url = "https://app.digio.in/#s/DGO2306102245119536Y"
        if (url==null){
            finish()
        }
        println(url)

        val builder = Builder()
        builder.setColorScheme(COLOR_SCHEME_SYSTEM)
        builder.setStartAnimations(this@WebViewActivity,
            R.anim.enter_from_left,
            R.anim.exit_to_left)

        val customTabsIntent = builder.build()

        // Launch the URL in a Chrome Custom Tab
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }
}
