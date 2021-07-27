package com.av.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextPaint
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.av.arthanfinance.R

class ApplyForLoanActivity : AppCompatActivity() {

    private lateinit var loanPercentProgressBar: ProgressBar
    private lateinit var loanPercentText: TextView
    private lateinit var btnImgBack: ImageView
    private lateinit var loanApplication: TextView
    private var categoryList = ArrayList<String>()

    var loanResponse: LoanProcessResponse? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_for_apply)
        if (supportActionBar != null)
            supportActionBar?.hide()
        loanPercentProgressBar = findViewById(R.id.progress_loanPercent)
        loanPercentText = findViewById(R.id.tv_progresspercent)
        btnImgBack = findViewById(R.id.img_back)
        loanApplication = findViewById(R.id.tv_loan_application)
        loanResponse = intent.extras?.get("loanResponse") as LoanProcessResponse?

        val paint: TextPaint = loanApplication.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(0f, 0f, width, loanApplication.textSize, resources.getColor(
            R.color.dark_orange2, theme), resources.getColor(
            R.color.white, theme), Shader.TileMode.CLAMP)
        loanApplication.paint.shader = shader

        val percentCompleted = 23 //to be achieved from BE
        loanPercentProgressBar.max = 100
        ObjectAnimator.ofInt(loanPercentProgressBar, "progress", percentCompleted).setDuration(1000).start()

        loanPercentText.text = "${percentCompleted}%"

        val fragment = LoanProcessingFragment()
        val fragmentManger = this.supportFragmentManager
        val transaction = fragmentManger.beginTransaction()
        val args = Bundle()
        args.putSerializable("loanResponse", loanResponse)
        fragment.arguments = args
        transaction.setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        );
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

        btnImgBack.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack();
        }else {
            super.onBackPressed();
        }
    }
}