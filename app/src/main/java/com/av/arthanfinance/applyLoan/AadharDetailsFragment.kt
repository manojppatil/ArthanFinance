package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.Toast.makeText
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.av.arthanfinance.R
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.manager.DataManager
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
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
    private var stateList = arrayOf("Select State", "Telangana", "Andhra Pradesh", "Tamil Nadu","Karnataka", "Maharastra")
    private var cityList = arrayOf("Hyderabad", "Vizag", "Chennai","Bangalore", "Mumbai")
    private var addressState = ""
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
        //cityDropDown = view.findViewById(R.id.cityDropDown)
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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                addressState = stateDropDown.selectedItem.toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        /*val cityAdapter = activity?.let { ArrayAdapter(it,
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
        }*/
        pincodeText.addTextChangedListener(pinCodeTextWatcher)

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW,false)
        isCreateFlow?.let {
            if (!it){
                getPRAddressDetails()
                disableEditFeature(view)
            }
        }
        btnNext.setOnClickListener{
            if (!isCreateFlow!!){
                (activity as UploadKycDetailsActivity?)?.selectIndex(4)
            }else
                if (hasValidAadharDetailsData())
                  uploadAadharDetailsData()
        }

        return view
    }

    private fun disableEditFeature(view: View) {
        stateDropDown.isEnabled = false
        //cityDropDown.isEnabled = false
        address1.isEnabled = false
        address2.isEnabled = false
        address3.isEnabled = false
        pincodeText.isEnabled = false
    }

    private fun hasValidAadharDetailsData(): Boolean {
        if (address1.text.isNullOrEmpty()){
            makeText(activity?.applicationContext,"Address1 is Empty",Toast.LENGTH_SHORT).show()
            return false
        }else if (address2.text.isNullOrEmpty()){
            makeText(activity?.applicationContext,"Address2 is Empty",Toast.LENGTH_SHORT).show()
            return false
        }else if (address3.text.isNullOrEmpty()){
            makeText(activity?.applicationContext,"Address3 is Empty",Toast.LENGTH_SHORT).show()
            return false
        }else if (pincodeText.text.isNullOrEmpty()){
            makeText(activity?.applicationContext,"Pincode is Empty",Toast.LENGTH_SHORT).show()
            return false
        }else if (addressState.equals("") || addressState.equals("Select State")){
            makeText(activity?.applicationContext,"Please Select State",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    fun loadAddressData(loanResponse: LoanProcessResponse) {
        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW,false)
        if (!isCreateFlow!!){
            return
        }
        val fullAddress = loanResponse.addressLine1
        val data = fullAddress?.split(",", 2.toString())?.toTypedArray()
        val add1 = data!![0] + "," + data[1] + "," + data[2] + "," + data[3]
        val add2 =  data[4] + "," + data[5]
        val add3 =  data[6] + "," + data[7]

        address1.setText(add1)
        address2.setText(add2)
        address3.setText(add3)
        pincodeText.setText(loanResponse.pincode)
        addressState = loanResponse.state.toString()
    }

    private val pinCodeTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (start >= 6) {
                makeText(
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
            makeText(
                activity?.applicationContext,
                "Please enter VALID PINCODE.",
                Toast.LENGTH_SHORT
            ).show()
            pincodeText.setText("")
            pincodeText.requestFocus()
            return
        }
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val applicantType = (activity as UploadKycDetailsActivity?)?.loanResponse!!.applicantType
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
        jsonObject.addProperty("city", "Bhubaneswar")
        jsonObject.addProperty("state", addressState)
        jsonObject.addProperty("pincode", pincode)

        val context = activity?.applicationContext!!
        ApiClient().getAuthApiService(context).updateAadharAddress(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                if (response.code() != 200){
                    makeText(
                        context,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                val data = response.body()
                data?.customerId?.let {
                    (activity as UploadKycDetailsActivity?)?.coAppCustId =it
                }
//                (activity as UploadKycDetailsActivity).loanResponse = response.body()
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage("Do you want to add Co-Applicant")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    .setPositiveButton("Guarantor", DialogInterface.OnClickListener {
                            dialog, id ->
                        dialog.dismiss()
                        addGuarantorData()
                    }).setNegativeButton("Skip",DialogInterface.OnClickListener {
                            dialog, id ->
                        dialog.dismiss()
                        (activity as UploadKycDetailsActivity?)?.selectIndex(4)
                    }).setNeutralButton("Co-Applicant",DialogInterface.OnClickListener {
                            dialog, id ->
                        dialog.dismiss()
                        addCoApplicantData()
                    })
                val alert = dialogBuilder.create()
                alert.setTitle("Co-Applicant Data")
                alert.show()
            }

            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addGuarantorData() {
        val custID = loanResponse?.customerId
        DataManager.applicantType = "G"
        (activity as UploadKycDetailsActivity).loanResponse!!.applicantType = "G"
        (activity as UploadKycDetailsActivity?)?.selectIndex(0, custID)
    }

    private fun addCoApplicantData() {
        val context = activity?.applicationContext!!
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        ApiClient().getApiService(context).getCustomerId(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                t.printStackTrace()
                makeText(
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
                (activity as UploadKycDetailsActivity).hideProgressDialog()
                val coApplicantResponse = response.body() as LoanProcessResponse
                val custID = coApplicantResponse.customerId
                DataManager.applicantType = "CA"
                (activity as UploadKycDetailsActivity).loanResponse!!.applicantType = "CA"
                (activity as UploadKycDetailsActivity?)?.selectIndex(0, custID)
            }
        })
    }

    private fun getPRAddressDetails() {
        val context = activity?.applicationContext!!

        ApiClient().getAuthApiService(context).getPRAddressDetails(loanResponse!!.loanId!!,loanResponse!!.applicantType).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
                val adharResponse = response.body() as LoanProcessResponse

                val fullAddress = adharResponse.addressLine1
                val data = fullAddress?.split(",", 2.toString())?.toTypedArray()
                val add1 = data!![0] + "," + data[1] + "," + data[2] + "," + data[3]
                val add2 =  data[4] + "," + data[5]
                val add3 =  data[6] + "," + data[7]

                address1.setText(add1)
                address2.setText(add2)
                address3.setText(add3)

                pincodeText.setText(adharResponse.pincode)

                /*adharResponse.city?.let {
                    for (index in cityList.indices){
                        if (cityList[index].equals(it,true)){
                            cityDropDown.setSelection(index,false)
                            break
                        }
                    }
                }*/

                adharResponse.state?.let {
                    for (index in stateList.indices){
                        if (stateList[index].equals(it,true)){
                            stateDropDown.setSelection(index,false)
                            break
                        }
                    }
                }
            }
        })

    }


}


