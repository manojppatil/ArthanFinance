package com.av.arthanfinance.applyLoan

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.TextPaint
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.DigioPanResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.util.ArthanFinConstants
import com.google.gson.JsonObject

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
//, PaymentResultWithDataListener
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
    private var processingFee = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        val shader = LinearGradient(
            0f, 0f, width, tv_loan_proc.textSize, resources.getColor(
                R.color.dark_orange2, activity?.theme
            ), resources.getColor(
                R.color.indigoBlue, activity?.theme
            ), Shader.TileMode.CLAMP
        )
        tv_loan_proc.paint.shader = shader
        tvPayable.paint.shader = shader

        processingFee = loanResponse?.fee.toString()
        val roi = loanResponse?.roi
        var pf = "2.5%"
        val loanTerm = loanResponse?.loanTerm
        val loanAmount = loanResponse?.loanAmt
        val emi = loanResponse?.emi

        if (loanResponse?.pf?.isNotEmpty()!!) {
            pf =
                "A standard loan processing  fees (${loanResponse?.pf!!}) will be applicable to you in order to continue"
        }

        processeingAmount.text =
            "${activity?.applicationContext?.resources?.getString(R.string.Rs)} $processingFee"
        roiText.text = roi
        pfText.text = pf
        loanTermText.text = "$loanTerm Months"
        loanAmttext.text = loanAmount
        emiText.text = emi

        btnProceed.setOnClickListener {
            //createOrder()
            proceedToCongratulations()
        }
        return view
    }

    private fun createOrder() {
        //val amountToPay = processeingAmount.text.toString()

        var amount: Float = processingFee.toFloat()
        amount *= 100
        val jsonObject = JsonObject()
        jsonObject.addProperty("currency", "INR")
        jsonObject.addProperty("receipt", "Afpl123456")
        jsonObject.addProperty("amount", amount)

        val key = ArthanFinConstants.razorPayKey
        val secret = ArthanFinConstants.razorPaySecret

        val base = "$key:$secret"

        val authHeader = "Basic " + Base64.encodeToString(base.toByteArray(), Base64.NO_WRAP)

        val context = activity?.applicationContext!!
        ApiClient().getRazorPayApiService(context).order(authHeader, jsonObject).enqueue(object :
            Callback<DigioPanResponse> {
            override fun onResponse(
                call: Call<DigioPanResponse>,
                response: Response<DigioPanResponse>
            ) {
                val orderResponse = response.body()
                val orderId = orderResponse?.id
                startPayment(amount, orderId)
            }

            override fun onFailure(call: Call<DigioPanResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(
                    context,
                    "Service Failure, Once Network connection is stable, will try to resend again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startPayment(amount: Float, orderId: String?) {
        /*val checkout = Checkout()
        checkout.setKeyID(ArthanFinConstants.razorPayKey)

        checkout.setImage(R.mipmap.ic_launcher)

       // val context = activity?.applicationContext!!
        val activity: Activity = context as ApplyForLoanActivity

        try {
            val options = JSONObject()
            options.put("name", "Arthan Finance")
            options.put("description", "Loan processing fee")
            options.put("currency", "INR")
            options.put("amount", amount) //pass amount in currency subunits
            options.put("order_id", orderId)
            val preFill = JSONObject()
            preFill.put("email", "")
            preFill.put("contact", "7894310911")
            options.put("prefill", preFill)
            checkout.open(activity, options)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/

    }

    private fun proceedToCongratulations() {
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
        )
        transaction?.replace(R.id.container, fragment)
        transaction?.addToBackStack(null)
        transaction?.commit()
        //requireActivity().finish()
    }

    private fun proceedWithPayment() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("loanId", loanResponse?.loanId)
        jsonObject.addProperty("customerId", loanResponse?.customerId)
        jsonObject.addProperty("amount", loanResponse?.fee)

        val context = activity?.applicationContext!!
        ApiClient().getPaytmApiService(context).getPaytmTransanctionUrl(jsonObject).enqueue(object :
            Callback<LoanProcessResponse> {
            override fun onResponse(
                call: Call<LoanProcessResponse>,
                response: Response<LoanProcessResponse>
            ) {
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

    /*override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {


    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {

    }*/

}