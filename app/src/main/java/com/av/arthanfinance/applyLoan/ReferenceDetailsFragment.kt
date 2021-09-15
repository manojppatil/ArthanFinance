package com.av.arthanfinance.applyLoan

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.CustomerReferenceResponse
import com.av.arthanfinance.homeTabs.HomeDashboardActivity
import com.av.arthanfinance.models.CustomerBankDetailsResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReferenceDetailsFragment : Fragment() {
    private lateinit var tvReferenceDetails: TextView
    private lateinit var btnNext: Button

    private lateinit var neighborName: EditText
    private lateinit var neighborMobileNo: EditText

    private lateinit var supplierName: EditText
    private lateinit var supplierMobileNo: EditText

    private lateinit var apiClient: ApiClient

    private var loanResponse: LoanProcessResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.layout_reference_details, container, false)
        tvReferenceDetails = view.findViewById(R.id.tv_ref_details)
        btnNext = view.findViewById(R.id.btn_next)
        neighborName = view.findViewById(R.id.edt_name)
        neighborMobileNo = view.findViewById(R.id.edt_mob_num)

        supplierName = view.findViewById(R.id.edt_name_sup)
        supplierMobileNo = view.findViewById(R.id.edt_mob_num_sup)

        apiClient = ApiClient()
        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse
        val paint: TextPaint = tvReferenceDetails.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        val shader = LinearGradient(0f, 0f, width, tvReferenceDetails.textSize, resources.getColor(
            R.color.dark_orange2, activity?.theme), resources.getColor(
            R.color.indigoBlue, activity?.theme), Shader.TileMode.CLAMP)
        tvReferenceDetails.paint.shader = shader
        neighborMobileNo.addTextChangedListener(neighborTextWatcher)
        supplierMobileNo.addTextChangedListener(supplierTextWatcher)

        val isCreateFlow = arguments?.getBoolean(ArthanFinConstants.IS_CREATE_FLOW,false)
        isCreateFlow?.let {
            if (!it){
                getPRReferenceDetails()
                disableEditFeature()
            }
        }

        btnNext.setOnClickListener{
            if (isCreateFlow!!)
                saveCustomerReferenceData()
            else
                (activity as UploadKycDetailsActivity).moveToDashboardScreen()
        }
        return view
    }

    private val neighborTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (start >= 12) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Maximum mobile length exceeded. Please enter valid mobile number.",
                    Toast.LENGTH_SHORT
                ).show()
                neighborMobileNo.setText("")
            }
        }
    }


    private fun disableEditFeature() {
       neighborName.isEnabled = false
        neighborMobileNo.isEnabled = false
        supplierMobileNo.isEnabled = false
        supplierName.isEnabled = false
    }

    private val supplierTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (start >= 12) {
                Toast.makeText(
                    activity?.applicationContext,
                    "Maximum mobile length exceeded. Please enter valid mobile number.",
                    Toast.LENGTH_SHORT
                ).show()
                supplierMobileNo.setText("")
            }
        }
    }

    fun isValidPhoneNumber(phoneNumber: CharSequence?): Boolean {
        return if (phoneNumber == null || phoneNumber.length < 10) {
            false
        } else {
            Patterns.PHONE.matcher(phoneNumber).matches()
        }
    }

    private fun saveCustomerReferenceData() {
        val neightbourNameText = neighborName.text.toString()
        val neighbourMobileText = neighborMobileNo.text.toString()
        val supplierNameText = supplierName.text.toString()
        val supplierMobileText = supplierMobileNo.text.toString()

        if (neightbourNameText.isEmpty() || neighbourMobileText.isEmpty() || supplierNameText.isEmpty() || supplierMobileText.isEmpty()) {
            Toast.makeText(
                activity?.applicationContext,
                "Enter all required fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isValidPhoneNumber(neighbourMobileText)) {
            Toast.makeText(
                activity?.applicationContext,
                "Please enter VALID Neightbour Mobile Number.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (!isValidPhoneNumber(supplierMobileText)) {
            Toast.makeText(
                activity?.applicationContext,
                "Please enter VALID Supplier Mobile Number.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", loanResponse?.customerId)

        val jsonArray = JsonArray()

        val referenceJson = JsonObject()
        referenceJson.addProperty("refName", neightbourNameText)
        referenceJson.addProperty("refMobNo", neighbourMobileText)
        referenceJson.addProperty("refType", "customer")

        val referenceJson1 = JsonObject()
        referenceJson1.addProperty("refName", supplierNameText)
        referenceJson1.addProperty("refMobNo", supplierMobileText)
        referenceJson1.addProperty("refType", "Supplier")

        jsonArray.add(referenceJson1)
        jsonArray.add(referenceJson)
        jsonObject.add("refDetails", jsonArray)
        (activity as UploadKycDetailsActivity).showProgressDialog()
        val context = activity?.applicationContext
        if (context != null) {
            apiClient.getApiService(context).saveCustReference(jsonObject).enqueue(object :
                Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>
                ) {
                    (activity as UploadKycDetailsActivity).hideProgressDialog()

                    val processingResponse = response.body() as LoanProcessResponse
                    loanResponse?.fee = processingResponse.fee
                    loanResponse?.pf = processingResponse.pf
                    loanResponse?.roi = processingResponse.roi
                    loanResponse?.loanTerm = processingResponse.loanTerm
                    loanResponse?.emi = processingResponse.emi
                    loanResponse?.loanAmt = processingResponse.loanAmt
                    val intent = Intent(activity?.applicationContext, ApplyForLoanActivity::class.java)
                    intent.putExtra("loanResponse", loanResponse)
                    startActivity(intent)
                    // (activity as UploadKycDetailsActivity?)?.selectIndex(7)

                }

                override fun onFailure(call: Call<LoanProcessResponse>, t: Throwable) {
                    t.printStackTrace()
                    (activity as UploadKycDetailsActivity).hideProgressDialog()
                    Toast.makeText(
                        context,
                        "Service Failure, Once Network connection is stable, will try to resend again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun getPRReferenceDetails() {
        val context = activity?.applicationContext!!
        (activity as UploadKycDetailsActivity).showProgressDialog()
        ApiClient().getAuthApiService(context).getPRReferenceDetails(loanResponse!!.loanId!!).enqueue(
            object :
                Callback<CustomerReferenceResponse> {
                override fun onFailure(call: Call<CustomerReferenceResponse>, t: Throwable) {
                    t.printStackTrace()
                    (activity as UploadKycDetailsActivity).hideProgressDialog()

                }

                override fun onResponse(
                    call: Call<CustomerReferenceResponse>,
                    response: Response<CustomerReferenceResponse>
                ) {
                    (activity as UploadKycDetailsActivity).hideProgressDialog()
                    val customerReferenceResponse = response.body()
                    customerReferenceResponse?.customerReferences?.let {
                        if (it.size == 2){
                            val custRef = it.get(0)
                            neighborName.setText(custRef.refName)
                            neighborMobileNo.setText(custRef.refMobNo)

                            val custRef1 = it.get(1)
                            supplierName.setText(custRef1.refName)
                            supplierMobileNo.setText(custRef1.refMobNo)
                            neighborName.isEnabled =false
                            neighborMobileNo.isEnabled =false
                            supplierName.isEnabled =false
                            supplierMobileNo.isEnabled =false
                        }
                    }
                }
            })
    }


}