package com.example.arthanfinance.applyLoan

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.arthanfinance.OTPActivity
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Matcher
import java.util.regex.Pattern

class AadharDetailsFragment : Fragment() {
    private lateinit var btnNext: Button
    private lateinit var stateDropDown: Spinner
    private lateinit var cityDropDown: Spinner
    private lateinit var address1: EditText
    private lateinit var address2: EditText
    private lateinit var address3: EditText
    private lateinit var pincodeText: EditText
    private var stateList = arrayOf("Telangana", "Andhra Pradesh", "Tamil Nadu","Karnataka", "Maharastra")
    private var cityList = arrayOf("Hyderabad", "Vizag", "Chennai","Bangalore", "Mumbai")

    var loanResponse: LoanProcessResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_aadhar_details, container, false)
        btnNext = view.findViewById(R.id.btn_next_in_aadhar_details)
        stateDropDown = view.findViewById(R.id.stateDropDown)
        cityDropDown = view.findViewById(R.id.cityDropDown)
        address1 = view.findViewById(R.id.address_line1_text)
        address2 = view.findViewById(R.id.address_line2_text)
        address3 = view.findViewById(R.id.address_line3_text)
        pincodeText = view.findViewById(R.id.pincode_text)
        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val statesAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, stateList) }
        stateDropDown.adapter = statesAdapter

        stateDropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        val cityAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, cityList) }
        cityDropDown.adapter = cityAdapter

        cityDropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {

            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        pincodeText.addTextChangedListener(pinCodeTextWatcher)

        btnNext.setOnClickListener{
            uploadAadharDetailsData()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    fun loadAddressData(loanResponse: LoanProcessResponse) {
        address1.setText(loanResponse.addressLine1)
        address2.setText(loanResponse.addressLine2)
        address3.setText(loanResponse.addressLine3)
        pincodeText.setText(loanResponse.pincode)
    }

    private val pinCodeTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (start >= 6) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Maximum pincode length exceeded. Please enter valid pincod.",
                    Toast.LENGTH_SHORT
                ).show()
                pincodeText.setText("")
            }
        }
    }

    fun isValidPinCode(pinCode: String?): Boolean {
        val regex = "^[1-9]{1}[0-9]{2}\\s{0,1}[0-9]{3}$"
        // Compile the ReGex
        val p: Pattern = Pattern.compile(regex)
        if (pinCode == null) {
            return false
        }
        val m: Matcher = p.matcher(pinCode)
        return m.matches()
    }

    private fun uploadAadharDetailsData() {
        val pincode = pincodeText.text.toString()
        if (!isValidPinCode(pincode)) {
            Toast.makeText(
                activity?.applicationContext,
                "Please enter VALID PINCODE.",
                Toast.LENGTH_SHORT
            ).show()
            pincodeText.setText("")
            pincodeText.requestFocus()
            return
        }
        val applicantType = if((activity as UploadKycDetailsActivity?)?.isForCoApplicant!!) "CA" else "PA"
        var custId = loanResponse?.customerId
        if((activity as UploadKycDetailsActivity?)?.coAppCustId!!.isNotEmpty()) {
            custId = (activity as UploadKycDetailsActivity?)?.coAppCustId
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", custId)
        jsonObject.addProperty("addressLine1", address1.text.toString())
        jsonObject.addProperty("addressLine2", address2.text.toString())
        jsonObject.addProperty("applicantType", applicantType)
        jsonObject.addProperty("addressLine3", address3.text.toString())
        jsonObject.addProperty("city", cityDropDown.selectedItem.toString())
        jsonObject.addProperty("state", stateDropDown.selectedItem.toString())
        jsonObject.addProperty("pincode", pincode)

        val context = activity?.applicationContext!!
        ApiClient().getApiService(context).updateAadharAddress(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                if(applicantType == "CA") {
                    (activity as UploadKycDetailsActivity?)?.selectIndex(4)
                } else {
                    val dialogBuilder = AlertDialog.Builder(activity!!)
                    dialogBuilder.setMessage("Do you want to add Co-Applicant")
                        // if the dialog is cancelable
                        .setCancelable(false)
                        .setPositiveButton("Yes", DialogInterface.OnClickListener {
                                dialog, id ->
                            dialog.dismiss()
                            addCoApplicantData()
                        }).setNegativeButton("No",DialogInterface.OnClickListener {
                                dialog, id ->
                            dialog.dismiss()
                            (activity as UploadKycDetailsActivity?)?.selectIndex(4)
                        })

                    val alert = dialogBuilder.create()
                    alert.setTitle("Co-Applicant Data")
                    alert.show()
                }
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addCoApplicantData() {
        val context = activity?.applicationContext!!
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        ApiClient().getApiService(context).getCustomerId(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Not able to add coApplicant, proceeding further",
                    Toast.LENGTH_SHORT
                ).show()
                (activity as UploadKycDetailsActivity?)?.selectIndex(4)
            }

            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val coApplicantResponse = response.body() as LoanProcessResponse
                val custID = coApplicantResponse.customerId
                (activity as UploadKycDetailsActivity?)?.selectIndex(0, custID)
            }
        })
    }
}