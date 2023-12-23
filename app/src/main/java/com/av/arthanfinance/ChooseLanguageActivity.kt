package com.av.arthanfinance

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.av.arthanfinance.introductionPager.IntroductionPagerActivity

class ChooseLanguageActivity : AppCompatActivity() {
    private lateinit var btnEnglish: Button
    private lateinit var btnHindi: Button
    private lateinit var btnUrdu: Button

    private lateinit var btnContinue: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_choose_language)

        if (supportActionBar != null)
            supportActionBar?.hide()

        btnContinue = findViewById(R.id.btn_continue)
        btnEnglish = findViewById(R.id.btn_english)
        btnHindi = findViewById(R.id.btn_hindi)
        btnUrdu = findViewById(R.id.btn_urdu)
        btnContinue.setOnClickListener{
            val intent = Intent(this@ChooseLanguageActivity, IntroductionPagerActivity::class.java)
            startActivity(intent)
        }
        btnEnglish.setOnClickListener{

            btnEnglish.setBackgroundResource(R.drawable.bg_language_rect)
            btnHindi.setBackgroundResource(R.drawable.bg_rect_gains_boro)
            btnUrdu.setBackgroundResource(R.drawable.bg_rect_gains_boro)

            btnEnglish.setTextColor(resources.getColor(R.color.dark_orange1, theme))
            btnHindi.setTextColor(resources.getColor(R.color.gains_boro, theme))
            btnUrdu.setTextColor(resources.getColor(R.color.gains_boro, theme))
        }
        btnHindi.setOnClickListener{

            btnHindi.setBackgroundResource(R.drawable.bg_language_rect)
            btnEnglish.setBackgroundResource(R.drawable.bg_rect_gains_boro)
            btnUrdu.setBackgroundResource(R.drawable.bg_rect_gains_boro)

            btnEnglish.setTextColor(resources.getColor(R.color.gains_boro, theme))
            btnHindi.setTextColor(resources.getColor(R.color.dark_orange1, theme))
            btnUrdu.setTextColor(resources.getColor(R.color.gains_boro, theme))
        }
        btnUrdu.setOnClickListener{

            btnUrdu.setBackgroundResource(R.drawable.bg_language_rect)
            btnHindi.setBackgroundResource(R.drawable.bg_rect_gains_boro)
            btnEnglish.setBackgroundResource(R.drawable.bg_rect_gains_boro)

            btnEnglish.setTextColor(resources.getColor(R.color.gains_boro, theme))
            btnHindi.setTextColor(resources.getColor(R.color.gains_boro, theme))
            btnUrdu.setTextColor(resources.getColor(R.color.dark_orange1, theme))
        }

    }
}