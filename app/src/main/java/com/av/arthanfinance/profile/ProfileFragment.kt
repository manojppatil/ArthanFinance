package com.av.arthanfinance.profile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.AuthenticationResponse
import com.av.arthanfinance.applyLoan.LoanProcessResponse
import com.av.arthanfinance.applyLoan.UploadKycDetailsActivity
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class ProfileFragment : Fragment(), DatePickerDialog.OnDateSetListener {
    var customerData: CustomerHomeTabResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        getProfileData()

        view.findViewById<ImageView>(R.id.iv_update)?.setOnClickListener {
            //updateProfileData()
        }

        /*view.findViewById<Button>(R.id.btn_update)?.setOnClickListener {
            updateProfileData()
        }
        view.findViewById<EditText>(R.id.edt_dob)?.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog =
                DatePickerDialog(requireContext(), this, calendar.get(
                    Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }*/
        return view
    }

    /*private fun updateProfileData() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", view?.findViewById<EditText>(R.id.edt_name)?.text.toString())
        jsonObject.addProperty("panNo", view?.findViewById<EditText>(R.id.edt_pan)?.text.toString())
        jsonObject.addProperty("dob", view?.findViewById<EditText>(R.id.edt_dob)?.text.toString())
        jsonObject.addProperty("emailId", view?.findViewById<EditText>(R.id.edt_email)?.text.toString())
        jsonObject.addProperty("mobileNo", view?.findViewById<EditText>(R.id.edt_mob_num)?.text.toString())
        jsonObject.addProperty("resiAddress", view?.findViewById<EditText>(R.id.edt_residency)?.text.toString())
        jsonObject.addProperty("ofcAddress", view?.findViewById<EditText>(R.id.edt_office)?.text.toString())
        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).updateProfile(jsonObject).enqueue(object :
            Callback<AuthenticationResponse> {
            override fun onResponse(
                call: Call<AuthenticationResponse>,
                response: Response<AuthenticationResponse>
            ) {
                (activity as HomeDashboardActivity).hideProgressDialog()
                val updateRespoonse = response.body() as AuthenticationResponse
                if (updateRespoonse.apiCode == 200.toString()) {
                    Toast.makeText(
                        context,
                        "Update success",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    Toast.makeText(
                        context,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                (activity as HomeDashboardActivity).hideProgressDialog()
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }*/

    override fun onResume() {
        super.onResume()
        getProfileData()
    }

    private fun getProfileData() {
        (activity as HomeDashboardActivity).showProgressDialog()
        val context = getContext()
        if (context != null) {
            var mobileNo = ""
            customerData?.mobNo?.let {
                mobileNo = it
            }
            ApiClient().getAuthApiService(context).getProfile(mobileNo).enqueue(object :
                Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    val userData = response.body()

                    view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.GONE
                    view?.findViewById<NestedScrollView>(R.id.nsv_data_layout)?.visibility = View.VISIBLE


                    if (userData != null){
                        userData.name?.let {
                            view?.findViewById<TextView>(R.id.user_name)?.setText(it)
                        }
                        userData.mobileNo?.let {
                            view?.findViewById<TextView>(R.id.user_mobile)?.setText(it)
                        }
                        userData.dob?.let {
                            view?.findViewById<TextView>(R.id.user_dob)?.setText(it)
                        }
                        userData.emailId?.let {
                            view?.findViewById<TextView>(R.id.user_email)?.setText(it)
                        }
                        userData.panNo?.let {
                            view?.findViewById<TextView>(R.id.user_pan)?.setText(it)
                        }
                        userData.resiAddress?.let {
                            view?.findViewById<TextView>(R.id.user_residence_address)?.setText(it)
                        }
                        userData.ofcAddress?.let {
                            view?.findViewById<TextView>(R.id.user_office_address)?.setText(it)
                        }
                    }else{
                        view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                        view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    (activity as HomeDashboardActivity).hideProgressDialog()
                    t.printStackTrace()
                    view?.findViewById<CardView>(R.id.cv_progress)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.tv_no_data)?.visibility = View.VISIBLE
                    Toast.makeText(context, "A server error occurred. Please try after some time", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        /*val text =  view?.findViewById<EditText>(R.id.edt_dob)
        val date = "${dayOfMonth}/${month + 1}/${year}"
        text?.setText(date)*/
    }


}