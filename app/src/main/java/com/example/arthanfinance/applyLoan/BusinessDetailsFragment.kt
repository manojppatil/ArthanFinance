package com.example.arthanfinance.applyLoan

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BusinessDetailsFragment : Fragment() {
    private lateinit var btnNext: Button
    private lateinit var apiClient: ApiClient
    private lateinit var businessName: EditText
    private lateinit var constitutionSpiner: Spinner
    private lateinit var typeSpinner: Spinner

    private lateinit var categorySpiner: Spinner
    private lateinit var segmentSpinner: Spinner
    private lateinit var dateOfIncorporation: EditText
    private lateinit var panNum: EditText
    private lateinit var udhyogAadhar: EditText
    private lateinit var ssiNum: EditText
    private lateinit var gstNum: EditText
    private lateinit var turnOver: EditText
    private lateinit var income: EditText
    private lateinit var expense: EditText
    private lateinit var address: EditText
    private lateinit var bankName: EditText
    private lateinit var accountNo: EditText
    private lateinit var ifscCode: EditText

    private var loanResponse: LoanProcessResponse? = null
    private var constitutionList = arrayOf("Individual", "Sole Proprietorship", "Partnership","Others")
    private var typeList = arrayOf("Manufacturing", "wholesaler", "Retailer","Service")
    private var categoryList = ArrayList<String>()
    private var segmentList = ArrayList<String>()
    private var categories = ArrayList<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_business_details, container, false)
        btnNext = view.findViewById(R.id.btn_next_in_businessDetails)
        constitutionSpiner = view.findViewById(R.id.constitutionSpiner)
        typeSpinner = view.findViewById(R.id.typeSpinner)
        categorySpiner = view.findViewById(R.id.categorySpinner)
        segmentSpinner = view.findViewById(R.id.segmentSpinner)
        dateOfIncorporation = view.findViewById(R.id.edt_dateOfIncorporation)
        businessName = view.findViewById(R.id.edt_bus_name)
        panNum = view.findViewById(R.id.edt_bus_pan_num)
        udhyogAadhar = view.findViewById(R.id.edt_udhyog)
        ssiNum = view.findViewById(R.id.edt_ssi_num)
        gstNum = view.findViewById(R.id.edt_gst_num)
        turnOver = view.findViewById(R.id.edt_turnover)
        income = view.findViewById(R.id.edt_income)
        expense = view.findViewById(R.id.edt_expenses)
        address = view.findViewById(R.id.edt_address)
        bankName = view.findViewById(R.id.edt_bank_name)
        accountNo = view.findViewById(R.id.edt_acc_no)
        ifscCode = view.findViewById(R.id.edt_ifsc)

        apiClient = ApiClient()

        val mPrefs: SharedPreferences? = activity!!.getSharedPreferences("categoriesList", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs?.getString("categoriesList", null)
        if(json != null) {//ArrayList<Category>
            val obj: Categories? = gson.fromJson(json, Categories::class.java)

            categories = obj?.categories!!
            for(category: Category in categories) {
                categoryList.add(category.categoryDesc)
            }
        }
        val constitutionAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, constitutionList) }
        constitutionSpiner.adapter = constitutionAdapter

        constitutionSpiner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val typesAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, typeList) }
        typeSpinner.adapter = typesAdapter

        typeSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val categoryAdapter = activity?.let { ArrayAdapter(it,
            R.layout.emi_options, categoryList) }
        categorySpiner.adapter = categoryAdapter
        categorySpiner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View?, position: Int, id: Long) {
                val category = categories[position]//categoryList[position]
                segmentList = category.segments as ArrayList<String>

                val segmentAdapter = activity?.let { ArrayAdapter(it,
                    R.layout.emi_options, segmentList) }
                segmentSpinner.adapter = segmentAdapter
                segmentSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View?, position: Int, id: Long) {

                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        dateOfIncorporation.setOnClickListener {
            view.clearFocus()
            hideSoftKeyboard(activity!!,it)
            val picker: DialogFragment = DatePickerFragment()
            picker.show(fragmentManager!!, "datePicker")
        }

        btnNext.setOnClickListener {
            saveCustomerBusinessData()
        }
        return view
    }

    fun hideSoftKeyboard(activity: Activity, view: View) {
        val imm: InputMethodManager =activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    private fun saveCustomerBusinessData() {
        val jsonObject = JsonObject()

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

//        val mPrefs: SharedPreferences? = activity?.getSharedPreferences("loanResponse", Context.MODE_PRIVATE)
//        val gson = Gson()
//        val json: String? = mPrefs?.getString("loanResponse", null)
//        if(json != null) {
//            val obj: LoanDetailsResponse = gson.fromJson(json, LoanDetailsResponse::class.java)
//            loanResponse = obj
            jsonObject.addProperty("loanId", loanResponse?.loanId)
            jsonObject.addProperty("customerId", loanResponse?.customerId)
            jsonObject.addProperty("businessName", businessName.text.toString())
            jsonObject.addProperty("constitution", constitutionSpiner.selectedItem.toString())
            jsonObject.addProperty("category", categorySpiner.selectedItem.toString())
            jsonObject.addProperty("segment", segmentSpinner.selectedItem.toString())
            jsonObject.addProperty("type", typeSpinner.selectedItem.toString())
            jsonObject.addProperty("businessPan", panNum.text.toString())
            jsonObject.addProperty("udhyogAadhar", udhyogAadhar.text.toString())
            jsonObject.addProperty("ssiNo", ssiNum.text.toString())
            jsonObject.addProperty("gstNo", gstNum.text.toString())
            jsonObject.addProperty("turnover", turnOver.text.toString())
            jsonObject.addProperty("income", income.text.toString())
            jsonObject.addProperty("expenses", expense.text.toString())
            jsonObject.addProperty("businessAddress", address.text.toString())
            jsonObject.addProperty("bankName", bankName.text.toString())
            jsonObject.addProperty("accountNo", accountNo.text.toString())
            jsonObject.addProperty("ifscCode", ifscCode.text.toString())

            val context = activity?.applicationContext
            if (context != null) {
                ApiClient().getApiService(context).saveCustBusiness(jsonObject).enqueue(object :
                    Callback<AuthenticationResponse> {
                    override fun onResponse(
                        call: Call<AuthenticationResponse>,
                        response: Response<AuthenticationResponse>
                    ) {
                        (activity as UploadKycDetailsActivity?)?.selectIndex(5)
//                        val fragment =
//                            UploadDocsFragment()
//                        val fragmentManger = activity?.supportFragmentManager
//                        val transaction = fragmentManger?.beginTransaction()
//                        val args = Bundle()
//                        args.putString("loanId", loanResponse?.loanId)
//                        args.putString("customerId", loanResponse?.customerId)
//                        fragment.setArguments(args)
//
//                        transaction?.setCustomAnimations(
//                            R.anim.enter_from_right,
//                            R.anim.exit_to_left,
//                            R.anim.enter_from_left,
//                            R.anim.exit_to_right
//                        )
//                        transaction?.replace(R.id.container, fragment)
//                        transaction?.addToBackStack(null)
//                        transaction?.commit()
                    }

                    override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
                        t.printStackTrace()
                        Toast.makeText(
                            context,
                            "Service Failure, Once Network connection is stable, will try to resend again",
                            LENGTH_SHORT
                        ).show()
                    }
                })
            }
//        }
    }

}

open class DatePickerFragment : DialogFragment(),
    OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
// Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

// Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity!!, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c[year, month] = day
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate: String = sdf.format(c.time)
        (activity!!.findViewById(R.id.edt_dateOfIncorporation) as EditText).setText(formattedDate)

    }
}