package com.example.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UploadKycDetailsActivity : AppCompatActivity(), UploadAdharCardFragment.UpdateLoanResponseData {
    private lateinit var tvPersonalDetails: TextView
    private lateinit var imgBack: ImageView
    private lateinit var progressRegistrationBar: ProgressBar
    private lateinit var registrationProgressPercent: TextView
    private lateinit var tab_viewpager: ViewPager
    var loanResponse: LoanProcessResponse? = null
    val panCardFragment = UploadPanCardFragment()
    val photoFragment = UploadPhotoFragment()
    val aadharPicFragment = UploadAdharCardFragment()
    val aadharDetailsFragment = AadharDetailsFragment()

    val businessDetailsFragment = BusinessDetailsFragment()
    val uploadDocsFragment = UploadDocsFragment()
    val referenceDetailsFragment = ReferenceDetailsFragment()
    var coAppCustId: String = ""
    var isForCoApplicant = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager)

        if (supportActionBar != null)
            supportActionBar?.hide()

        tab_viewpager = findViewById(R.id.tab_viewpager)
        var tab_tablayout = findViewById<TabLayout>(R.id.tab_tablayout)
        imgBack = findViewById(R.id.img_back)
        progressRegistrationBar = findViewById(R.id.progress_registration)
        registrationProgressPercent = findViewById(R.id.tv_progresspercent)


        val percentCompleted = 23 //to be achieved from BE
        progressRegistrationBar.max = 100
        ObjectAnimator.ofInt(progressRegistrationBar, "progress", percentCompleted).setDuration(1000).start()

        registrationProgressPercent.text = "${percentCompleted}%"

        tvPersonalDetails = findViewById(R.id.tv_per_details)
        val paint: TextPaint = tvPersonalDetails.getPaint()
        val width = paint.measureText("Personal Details")

        val shader = LinearGradient(0f, 0f, width, tvPersonalDetails.textSize, resources.getColor(
            R.color.dark_orange2, theme), resources.getColor(
            R.color.white, theme), Shader.TileMode.CLAMP)
        tvPersonalDetails.paint.shader = shader

        loanResponse = intent.getSerializableExtra("loanResponse") as LoanProcessResponse

        setupViewPager(tab_viewpager)

        tab_tablayout.setupWithViewPager(tab_viewpager)

        val tabStrip = tab_tablayout.getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    return true
                }
            })
        }
        getCategoryAndSegmentData()

        imgBack.setOnClickListener{
            finish()
        }
    }

    public fun selectIndex(newIndex: Int, custId: String? = "") {
        tab_viewpager.currentItem = newIndex
        if (custId != null && custId.isNotEmpty()) {
            coAppCustId = custId
            isForCoApplicant = true
        }
    }

    override fun onBackPressed() {
        val currentPosition: Int = tab_viewpager.currentItem
        if (currentPosition != 0) {
            tab_viewpager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

    // This function is used to add items in arraylist and assign
    // the adapter to view pager
    private fun setupViewPager(viewpager: ViewPager) {
        var adapter: ViewPagerAdapter =
            ViewPagerAdapter(
                supportFragmentManager
            )

        val bundle = Bundle()
        bundle.putSerializable("loanResponse", loanResponse)

        panCardFragment.arguments = bundle
        photoFragment.arguments = bundle
        aadharPicFragment.arguments = bundle
        aadharDetailsFragment.arguments = bundle

        businessDetailsFragment.arguments = bundle
        uploadDocsFragment.arguments = bundle
        referenceDetailsFragment.arguments = bundle



        adapter.addFragment(panCardFragment, "Your KYC Details")
        adapter.addFragment(photoFragment, "Upload Photo")
        adapter.addFragment(aadharPicFragment, "Upload Aadhaar")
        adapter.addFragment(aadharDetailsFragment, "Address")


        adapter.addFragment(businessDetailsFragment, "Business Details")
        adapter.addFragment(uploadDocsFragment, "Upload Documents")
        adapter.addFragment(referenceDetailsFragment, "Reference Details")

        // setting adapter to view pager.
        viewpager.adapter = adapter
    }

    class ViewPagerAdapter : FragmentPagerAdapter {

        private final var fragmentList1: ArrayList<Fragment> = ArrayList()
        private final var fragmentTitleList1: ArrayList<String> = ArrayList()

        public constructor(supportFragmentManager: FragmentManager)
                : super(supportFragmentManager)

        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }

        @Nullable
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
        }

        override fun getCount(): Int {
            return fragmentList1.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)
        }
    }

    override fun sendData(loanProcessResponse: LoanProcessResponse) {
        loanResponse?.let { aadharDetailsFragment.loadAddressData(it) }
    }

    private fun getCategoryAndSegmentData() {
        ApiClient().getAuthApiService(this@UploadKycDetailsActivity).getCategorySegment().enqueue(object :
            Callback<Categories> {
            override fun onFailure(call: Call<Categories>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<Categories>,
                response: Response<Categories>
            ) {
                val custData = response.body()
                val categoriesList = custData?.categories

                val sharedPref: SharedPreferences? = getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
                val prefsEditor = sharedPref?.edit()
                val gson = Gson()
                val json: String = gson.toJson(custData)
                prefsEditor?.putString("categoriesList", json)
                prefsEditor?.apply()

            }
        })
    }
}