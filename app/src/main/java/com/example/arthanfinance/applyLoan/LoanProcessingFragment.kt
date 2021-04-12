package com.example.arthanfinance.applyLoan

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.arthanfinance.R
import com.example.arthanfinance.networkService.ApiClient
import com.google.gson.JsonObject
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class LoanProcessingFragment : Fragment() {
    private lateinit var tv_loan_proc: TextView
    private lateinit var tvPayable: TextView
    private lateinit var processeingAmount: TextView
    private lateinit var pfText: TextView
    private lateinit var loanAmttext: TextView
    private lateinit var loanTermText: TextView
    private lateinit var roiText: TextView
    private lateinit var emiText: TextView
    private lateinit var btnProceed: Button
    var loanResponse: LoanProcessResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.layout_loan_processing, container, false)
        tv_loan_proc = view.findViewById(R.id.tv_loan_proc)
        tvPayable = view.findViewById(R.id.tv_payable)
        processeingAmount = view.findViewById(R.id.tv_payable_rs)
        btnProceed = view.findViewById(R.id.btn_proceed)
        pfText = view.findViewById(R.id.tv_standardText)
        loanAmttext = view.findViewById(R.id.tv_loan_amnt_rs)
        loanTermText = view.findViewById(R.id.tv_loan_term_rs)
        roiText = view.findViewById(R.id.tv_interest_rs)
        emiText = view.findViewById(R.id.tv_emi_rs)

        val paint: TextPaint = tv_loan_proc.getPaint()
        val width = paint.measureText("Upload your Aadhaar Card")

        loanResponse = arguments?.getSerializable("loanResponse") as LoanProcessResponse

        val shader = LinearGradient(0f, 0f, width, tv_loan_proc.textSize, resources.getColor(
            R.color.dark_orange2, activity?.theme), resources.getColor(
            R.color.indigoBlue, activity?.theme), Shader.TileMode.CLAMP)
        tv_loan_proc.paint.shader = shader
        tvPayable.paint.shader = shader

        val processingFee = loanResponse?.fee
        val roi = loanResponse?.roi
        var pf = "2%"
        val loanTerm = loanResponse?.loanTerm
        val loanAmount = loanResponse?.loanAmt
        val emi = loanResponse?.emi

        if(loanResponse?.pf?.isNotEmpty()!!){
            pf = "A standard loan processing  fees (${loanResponse?.pf!!}) will be applicable to you in order to continue"
        }
       // val df = DecimalFormat("##,##,##0").format(processingFee);

        processeingAmount.text = "${activity?.applicationContext?.resources?.getString(
            R.string.Rs
        )} ${processingFee}"
        roiText.text = roi
        pfText.text = pf
        loanTermText.text = "${loanTerm} Months"
        loanAmttext.text = loanAmount
        emiText.text = emi

        btnProceed.setOnClickListener{
           // proceedWithPayment()

            val fragment = CongratulationsFragment()
            val fragmentManger = activity?.supportFragmentManager
            val transaction = fragmentManger?.beginTransaction()
            val args = Bundle()
            args.putString("loanId", loanResponse?.loanId)
            fragment.arguments = args
            transaction?.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            );
            transaction?.replace(R.id.container, fragment)
            transaction?.addToBackStack(null)
            transaction?.commit()
        }
        return view
    }

    private fun proceedWithPayment() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", loanResponse?.customerId)
        jsonObject.addProperty("amount", loanResponse?.fee)

        val context = activity?.applicationContext!!
        ApiClient().getPaytmApiService(context).getPaytmTransanctionUrl(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(call: Call<LoanProcessResponse>, response: Response<LoanProcessResponse>) {
                val transactionURLRsponse = response.body() as LoanProcessResponse
                print(transactionURLRsponse)
                val paytmURL = transactionURLRsponse.paytmUrl
                print(paytmURL)

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

}