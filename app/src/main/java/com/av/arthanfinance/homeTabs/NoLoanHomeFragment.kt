package com.av.arthanfinance.homeTabs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.SetNewMpinActivity
import com.av.arthanfinance.applyLoan.CalculateKfs
import com.av.arthanfinance.applyLoan.InitiateApplyLoanActivity2
import com.av.arthanfinance.applyLoan.LoanEligibilityFailed
import com.av.arthanfinance.applyLoan.UpiMandate
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.LoanProcessResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.profile.MyAccount
import com.av.arthanfinance.serviceRequest.Getintouch
import com.av.arthanfinance.user_kyc.UploadAadharActivity
import com.av.arthanfinance.user_kyc.UploadBusinessPhotos
import com.bumptech.glide.Glide
import com.ebanx.swipebtn.SwipeButton
import com.example.awesomedialog.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_home_tab.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NoLoanHomeFragment : Fragment() {

    var customerData: UserDetailsResponse? = null
    private var loanResponse: LoanProcessResponse? = null
    private lateinit var customerName: TextView
    private lateinit var customerLastLogin: TextView
    private lateinit var borrowAmtLayout: LinearLayout
    private lateinit var availedAmtLayout: LinearLayout
    private lateinit var borrowedAmt: TextView
    private lateinit var availedAmt: TextView
    private lateinit var username: TextView
    private lateinit var card_number: TextView
    private lateinit var circleImg: CircleImageView

    //    private lateinit var rewardsButton: Button
    private lateinit var kyc_unchecked: CheckBox
    private lateinit var nach_unchecked: CheckBox
    private lateinit var agreement_unchecked: CheckBox
    private lateinit var withdraw_unchecked: CheckBox
    private lateinit var kyc_checked: CheckBox
    private lateinit var nach_checked: CheckBox
    private lateinit var agreement_checked: CheckBox
    private lateinit var withdraw_checked: CheckBox
    private lateinit var iv_logout: ImageButton
    private lateinit var step_one_layout: LinearLayout
    private lateinit var step_two_layout: LinearLayout
    private lateinit var step_three_layout: LinearLayout
    private lateinit var step_four_layout: LinearLayout
    private lateinit var account_setup_layout: LinearLayout
    private lateinit var swipeButton: SwipeButton
    private lateinit var refreshButton: ImageView
    private var sampleImages: IntArray = intArrayOf(
        R.drawable.slide_one
    )
    private var mCustomerId: String? = null
    private var mLoanAmount: String? = null
    private var nachStatus: String? = null
    private var kycStatus: String? = null
    private var profileStatus: String? = null
    private var agreementStatus: String? = null
    private var mpinStatus: String? = null
    private var canWithdraw: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imagesList = arrayListOf<ImageData>()
        val posters = resources.obtainTypedArray(R.array.posters)

        for (i in 0 until posters.length()) {
            imagesList.add(
                ImageData(
                    posters.getResourceId(i, -1),
                    "str1"
                )
            )
        }

        try {
            val custName = customerData?.customerName
            customerName.text = custName
            customerLastLogin.text = customerData!!.lastLogin
            card_number.text = customerData!!.applicantId
            availedAmt.text = customerData!!.eligibilityAmount
            borrowedAmt.text = customerData!!.borrowedAmount
            username.text = custName
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }

        lyt_genarate_stmt.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_LONG).show()
        }
        lyt_service_request.setOnClickListener {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_LONG).show()
        }
        lyt_customer_service.setOnClickListener {
            val intent = Intent(context, Getintouch::class.java)
            startActivity(intent)
        }

        posters.recycle()
//        cardSliderViewPager.adapter = customerData?.let { CarouselImageAdapter(imagesList, it) }

        carouselView.pageCount = sampleImages.size
        carouselView.setIndicatorVisibility(View.INVISIBLE)
        carouselView.setImageListener { position, imageView ->
            imageView.setImageResource(sampleImages[position])
        }
    }

    private fun calculateKfs() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        jsonObject.addProperty("loanAmount", mLoanAmount)

        ApiClient().getAuthApiService(activity as HomeDashboardActivity).calculatekfs(jsonObject)
            .enqueue(object : Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>,
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    loanResponse = response.body()
                    Log.e("TAG", loanResponse.toString())
                    if (loanResponse != null) {
                        val intent = Intent(
                            activity,
                            CalculateKfs::class.java
                        )
                        val loanId = loanResponse!!.applicationId
                        Log.e("TAGid", loanResponse!!.applicationId.toString())
                        intent.putExtra("loanId", loanId)
                        intent.putExtra("loanResponse", loanResponse)
                        intent.putExtra("loanAmount", mLoanAmount)
                        intent.putExtra("netDisbursedAmt", loanResponse!!.loanAmount)
                        intent.putExtra("repaymentDate", loanResponse!!.repaymentDate)
                        intent.putExtra("tenure", loanResponse!!.tenure)
                        intent.putExtra("totalInterest", loanResponse!!.totalInterest)
                        intent.putExtra("payableAmt", loanResponse!!.payableAmt)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this@NoLoanHomeFragment.activity,
                            "Service Failure, Please try after sometime",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                    t.printStackTrace()
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    Toast.makeText(
                        this@NoLoanHomeFragment.activity,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_tab, container, false)

        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        Log.e("TAG::", mCustomerId.toString())
        customerName = view.findViewById(R.id.customerName)
        customerLastLogin = view.findViewById(R.id.customerLastLogin)
        circleImg = view.findViewById(R.id.circle_img)
//        rewardsButton = view.findViewById(R.id.rewardsButton)
        borrowAmtLayout = view.findViewById(R.id.borrowAmtLayout)
        availedAmtLayout = view.findViewById(R.id.availAmtLayout)
        borrowedAmt = view.findViewById(R.id.borrowedAmt)
        availedAmt = view.findViewById(R.id.availableAmt)
        username = view.findViewById(R.id.user_name)
        card_number = view.findViewById(R.id.card_number)
        kyc_unchecked = view.findViewById(R.id.kyc_unchecked)
        nach_unchecked = view.findViewById(R.id.nach_unchecked)
        agreement_unchecked = view.findViewById(R.id.agreement_unchecked)
        withdraw_unchecked = view.findViewById(R.id.withdraw_unchecked)
        kyc_checked = view.findViewById(R.id.kyc_checked)
        nach_checked = view.findViewById(R.id.nach_checked)
        agreement_checked = view.findViewById(R.id.agreement_checked)
        withdraw_checked = view.findViewById(R.id.withdraw_checked)
        iv_logout = view.findViewById(R.id.iv_logout)
        step_one_layout = view.findViewById(R.id.lyt_step_one)
        step_two_layout = view.findViewById(R.id.lyt_step_two)
        step_three_layout = view.findViewById(R.id.lyt_step_three)
        step_four_layout = view.findViewById(R.id.lyt_step_four)
        step_four_layout.isEnabled = false
        account_setup_layout = view.findViewById(R.id.lyt_account_setup)
        swipeButton = view.findViewById(R.id.swipe_btn)
        refreshButton = view.findViewById(R.id.refresh_home)

//        rewardsButton.setOnClickListener {
//            val intent1 =
//                Intent(context, RewardsActivity::class.java)
//            startActivity(intent1)
//        }

        step_one_layout.setOnClickListener {
            val intent1 =
                Intent(context, UploadAadharActivity::class.java)
            startActivity(intent1)
        }

        refreshButton.setOnClickListener {
            getDashboardDetails()
        }
        getDashboardDetails()
        return view
    }

    private fun getDashboardDetails() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val customerId = mCustomerId.toString()
        Log.e("TAG::", customerId)
        ApiClient().getAuthApiService(activity as HomeDashboardActivity)
            .getCustomerDashboard(customerId)
            .enqueue(object :
                Callback<UserDetailsResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<UserDetailsResponse>,
                    response: Response<UserDetailsResponse>,
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    if (response.body() != null) {
                        try {
                            Log.e("TAG", response.body().toString())
                            if (response.body()!!.eligibilityStatus == "N") {
                                val intent = Intent(
                                    activity,
                                    LoanEligibilityFailed::class.java
                                )
                                intent.putExtra("from", "home")
                                startActivity(intent)
                                (activity as HomeDashboardActivity).finish()
                            } else {
                                mpinStatus = response.body()!!.mpinStatus
                                iv_logout.setOnClickListener {
                                    if (mpinStatus == "Complete") {
                                        logOut()
                                    } else {
                                        AwesomeDialog.build(activity as HomeDashboardActivity)
                                            .title("Warning")
                                            .body("Please set your MPIN before Logging out")
                                            .icon(R.drawable.ic_info_icon)
                                            .onPositive("Set MPIN now") {
                                                val intent = Intent(
                                                    activity,
                                                    SetNewMpinActivity::class.java
                                                )
                                                startActivity(intent)
                                            }
                                    }
                                }
                                customerName.text = response.body()!!.customerName
                                customerLastLogin.text =
                                    "Last Login: " + response.body()!!.lastLogin
                                mLoanAmount = response.body()!!.eligibilityAmount
                                availedAmt.text = response.body()!!.availableAmount
                                username.text = response.body()!!.customerName
                                if (response.body()!!.borrowedAmount == "") {
                                    borrowedAmt.text = "0"
                                } else {
                                    borrowedAmt.text = response.body()!!.borrowedAmount
                                }
                                val random = response.body()!!.applicantId
                                val one = random.substring(0, 4)
                                val two = random.substring(4, 8)
                                val three = random.substring(8, 12)
                                card_number.text = "$one $two $three"
                                if (response.body()!!.isApplied.equals("N")) {
                                    kycStatus = response.body()!!.kycStatus
                                    nachStatus = response.body()!!.nachStatus
                                    agreementStatus = response.body()!!.agreementStatus
                                    profileStatus = response.body()!!.profileStatus
                                    canWithdraw = response.body()!!.canWithdraw

                                    if (kycStatus == "Complete") {
                                        kyc_unchecked.visibility = View.GONE
                                        kyc_checked.visibility = View.VISIBLE
                                        step_one_layout.setBackgroundResource(R.color.white_smoke)
                                        step_two_layout.setBackgroundResource(R.color.white)
                                        step_three_layout.setBackgroundResource(R.color.white_smoke)
                                        step_four_layout.setBackgroundResource(R.color.white_smoke)
                                        step_one_layout.isClickable = false
                                        step_four_layout.isEnabled = false
                                    }
                                    if (nachStatus == "NACH Registration Successful") {
                                        nach_unchecked.visibility = View.GONE
                                        nach_checked.visibility = View.VISIBLE
                                        step_one_layout.setBackgroundResource(R.color.white_smoke)
                                        step_two_layout.setBackgroundResource(R.color.white_smoke)
                                        step_three_layout.setBackgroundResource(R.color.white)
                                        step_four_layout.setBackgroundResource(R.color.white_smoke)
                                        step_one_layout.isClickable = false
                                        step_four_layout.isClickable = false
                                    }
                                    if (agreementStatus == "Partial" || agreementStatus == "Complete") {
                                        agreement_unchecked.visibility = View.GONE
                                        agreement_checked.visibility = View.VISIBLE
                                        step_one_layout.setBackgroundResource(R.color.white_smoke)
                                        step_two_layout.setBackgroundResource(R.color.white_smoke)
                                        step_three_layout.setBackgroundResource(R.color.white_smoke)
                                        step_four_layout.setBackgroundResource(R.color.white)
                                        step_three_layout.isClickable = false
                                        step_four_layout.isEnabled = true
                                    }

                                    step_two_layout.setOnClickListener {
                                        if (kycStatus == "Complete") {
                                            val intent1 =
                                                Intent(context, UpiMandate::class.java)
                                            startActivity(intent1)
                                        } else {
                                            AwesomeDialog.build(activity as HomeDashboardActivity)
                                                .title("Warning")
                                                .body("Please complete your KYC and business details")
                                                .icon(R.drawable.ic_info_icon)
                                                .onPositive("Aadhaar KYC") {
                                                    val intent1 =
                                                        Intent(
                                                            context,
                                                            UploadAadharActivity::class.java
                                                        )
                                                    startActivity(intent1)
                                                }
                                                .onNegative("Business Details") {
                                                    val intent = Intent(
                                                        context,
                                                        UploadBusinessPhotos::class.java
                                                    )
                                                    startActivity(intent)
                                                }
                                        }
                                    }

                                    step_three_layout.setOnClickListener {
                                        if (profileStatus == "Complete") {
                                            when (nachStatus) {
                                                "NACH Registration Successful" -> {
                                                    calculateKfs()
                                                }
                                                "partial" -> {
                                                    Toast.makeText(
                                                        this@NoLoanHomeFragment.activity,
                                                        "Please check your SMS to complete E-NACH mandate",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                "pending" -> {
                                                    Toast.makeText(
                                                        this@NoLoanHomeFragment.activity,
                                                        "Kindly register for E-NACH Mandate",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                "auth_failed" -> {
                                                    Toast.makeText(
                                                        this@NoLoanHomeFragment.activity,
                                                        "Nach authentication failed, Please try after some time",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            AwesomeDialog.build(activity as HomeDashboardActivity)
                                                .title("Warning")
                                                .body("Kindly complete your account details with your profile photo")
                                                .icon(R.drawable.ic_info_icon)
                                                .onPositive("Go To My Account") {
                                                    val intent =
                                                        Intent(context, MyAccount::class.java)
                                                    startActivity(intent)
                                                }
                                        }
                                    }

                                    step_four_layout.setOnClickListener {
                                        if (canWithdraw == "Y") {
                                            val intent =
                                                Intent(
                                                    activity?.applicationContext,
                                                    InitiateApplyLoanActivity2::class.java
                                                )
                                            intent.putExtra("amount", mLoanAmount)
                                            startActivity(intent)
                                        } else {
                                            AwesomeDialog.build(activity as HomeDashboardActivity)
                                                .title("Thank You")
                                                .body("We are currently verifying your application, please wait for some time")
                                                .icon(R.drawable.ic_congrts)
                                        }
                                    }

                                } else if (response.body()!!.isApplied.equals("Y")) {
                                    account_setup_layout.visibility = View.GONE
                                    swipeButton.visibility = View.VISIBLE
                                    swipeButton.setOnStateChangeListener {
                                        val intent =
                                            Intent(
                                                activity?.applicationContext,
                                                InitiateApplyLoanActivity2::class.java
                                            )
                                        intent.putExtra("amount", response.body()!!.availableAmount)
                                        startActivity(intent)
                                    }
                                }

                                val sharedPref: SharedPreferences? =
                                    context?.getSharedPreferences(
                                        "customerData",
                                        Context.MODE_PRIVATE
                                    )
                                val prefsEditor = sharedPref?.edit()
                                val gson = Gson()
                                val json: String = gson.toJson(response.body())
                                prefsEditor?.putString("customerData", json)
                                prefsEditor?.apply()
                                //ImageLoad
                                if (response.body()?.customerImg.equals("") || response.body()?.customerImg == null) {
                                    Glide
                                        .with(this@NoLoanHomeFragment)
                                        .load(R.drawable.ic_accounts).fitCenter()
                                        .placeholder(R.drawable.ic_accounts)
                                        .into(circleImg)

                                } else {
                                    response.body()?.customerImg?.let {
                                        Glide
                                            .with(this@NoLoanHomeFragment)
                                            .load(it).fitCenter()
                                            .placeholder(R.drawable.ic_accounts)
                                            .into(circleImg)
                                    }
                                }
                            }

                        } catch (ex: NullPointerException) {
                            ex.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Service Failure, Once server connection is stable, will try to resend again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        context,
                        "Service Failure, Once network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity as HomeDashboardActivity).hideProgressDialog()
                }
            })
    }

    private fun logOut() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", mCustomerId)
        (activity as HomeDashboardActivity).showProgressDialog("Logging out...")
        if (context != null) {
            ApiClient().getAuthApiService(requireContext()).logOut(jsonObject).enqueue(object :
                Callback<AuthenticationResponse> {
                override fun onResponse(
                    call: Call<AuthenticationResponse>,
                    response: Response<AuthenticationResponse>,
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    val custData = response.body()
                    val sharedPref: SharedPreferences =
                        (activity as HomeDashboardActivity).getSharedPreferences(
                            "customerData",
                            Context.MODE_PRIVATE
                        )
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()

                    if (custData != null && custData.message.trim() == "Success") {
                        val intent =
                            Intent(activity, MPINLoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        (activity as HomeDashboardActivity).finish()
                    }
                }

                override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    t.printStackTrace()
                    Toast.makeText(activity, "LogOut Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
}