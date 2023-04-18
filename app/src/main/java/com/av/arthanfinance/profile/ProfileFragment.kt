package com.av.arthanfinance.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.av.arthanfinance.*
import com.av.arthanfinance.R
import com.av.arthanfinance.user_kyc.UploadBusinessPhotos
import com.av.arthanfinance.applyLoan.model.AuthenticationResponse
import com.av.arthanfinance.applyLoan.model.UserDetailsResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.introductionPager.IntroductionPagerActivity
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.serviceRequest.Getintouch
import com.bumptech.glide.Glide
import com.example.awesomedialog.*
import com.google.gson.JsonObject
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {
    var customerData: UserDetailsResponse? = null
    private var mCustomerId: String? = null
    private lateinit var circleImg: CircleImageView
    private lateinit var iv_logout: ImageButton
    private var mpinStatus: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val mPrefs: SharedPreferences? =
            context?.getSharedPreferences("customerData", Context.MODE_PRIVATE)
        mCustomerId = mPrefs?.getString("customerId", null)
        mpinStatus = mPrefs?.getString("mpinStatus", null)
        iv_logout = view.findViewById(R.id.iv_logout)
        circleImg = view.findViewById(R.id.iv_profile_image)
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

        getProfileData()

        return view
    }


    override fun onResume() {
        super.onResume()
        getProfileData()
    }

    private fun getProfileData() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val context = context
        if (context != null) {
            val customerId = mCustomerId.toString()

            ApiClient().getAuthApiService(context).getProfile(customerId).enqueue(object :
                Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    try {
                        (activity as HomeDashboardActivity).hideProgressDialog()
                        val userData = response.body()

                        view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                        view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.GONE
                        view?.findViewById<NestedScrollView>(R.id.nsv_data_layout)?.visibility =
                            View.VISIBLE

                        if (userData != null) {
                            userData.name?.let {
                                view?.findViewById<TextView>(R.id.user_name)
                                    ?.setText(it)
                            }
                            userData.customerId?.let {
                                view?.findViewById<TextView>(R.id.customer_id)?.setText(it)
                            }
                            userData.mobileNo?.let {
                                view?.findViewById<TextView>(R.id.user_mobile)?.setText(it)
                            }
                            userData.panNo?.let {
                                view?.findViewById<TextView>(R.id.user_pan)?.setText(it)
                            }
                            userData.cibil_score?.let {
                                view?.findViewById<TextView>(R.id.credit_score)?.setText("$it/900")
                            }
                            if (response.body()?.customerImg.equals("") || response.body()?.customerImg == null) {
                                Glide
                                    .with(activity as HomeDashboardActivity)
                                    .load(R.drawable.ic_accounts).fitCenter()
                                    .placeholder(R.drawable.ic_accounts)
                                    .into(circleImg)

                            } else {
                                response.body()?.customerImg?.let {
                                    Glide
                                        .with(activity as HomeDashboardActivity)
                                        .load(it).fitCenter()
                                        .placeholder(R.drawable.ic_accounts)
                                        .into(circleImg)
                                }
                            }
                        } else {
                            view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                            view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.VISIBLE
                        }
                    } catch (ex: NullPointerException) {
                        ex.printStackTrace()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    t.printStackTrace()
                    view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.VISIBLE
                    Toast.makeText(
                        context,
                        "A server error occurred. Please try after some time",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        lyt_credit.setOnClickListener {
//            val intent = Intent(context, Credit_score::class.java)
//            startActivity(intent)
//        }

        lyt_account.setOnClickListener {
            val intent = Intent(context, MyAccount::class.java)
            startActivity(intent)
        }

        lyt_business_pic.setOnClickListener {
            val intent = Intent(context, UploadBusinessPhotos::class.java)
            startActivity(intent)
        }
        lyt_help.setOnClickListener {
            val intent = Intent(context, Getintouch::class.java)
            startActivity(intent)
        }

        lyt_refer.setOnClickListener {
            val intent = Intent(context, RewardsActivity::class.java)
            startActivity(intent)
        }

        lyt_settings.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }

        lyt_about.setOnClickListener {
            val intent = Intent(context, IntroductionPagerActivity::class.java)
            startActivity(intent)
        }

        lyt_Tnc.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://arthan.finance/Termsandconditions"))
            startActivity(browserIntent)
        }

        lyt_privacy_policy.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://arthan.finance/Privacypolicy"))
            startActivity(browserIntent)
        }
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
                    response: Response<AuthenticationResponse>
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