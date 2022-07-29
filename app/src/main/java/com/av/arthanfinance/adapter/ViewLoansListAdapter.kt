package com.av.arthanfinance.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.LoanDetails
import com.av.arthanfinance.serviceRequest.GenerateLoanStatementActivity
import com.av.arthanfinance.serviceRequest.LoanDetailsActivity
import com.av.arthanfinance.serviceRequest.PaymentDuesActivity
import java.util.ArrayList

class ViewLoansListAdapter(
    private val loansList: ArrayList<LoanDetails>?,
    private val context: Context,
    private val status: String
) : RecyclerView.Adapter<ViewLoansListAdapter.MyViewHolder>(){

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val loanAmountValue: TextView? = itemView.findViewById(R.id.loanAmountValue)
        val loanAmountText: TextView? = itemView.findViewById(R.id.loanAmountText)
        val loanAccountNo: TextView? = itemView.findViewById(R.id.loanAccountNo)
        val loanTypeText: TextView? = itemView.findViewById(R.id.loanTypeText)
        val loanIconType: ImageView? = itemView.findViewById(R.id.loanIconType)
        val loanProgressBar: ProgressBar = itemView.findViewById(R.id.loanProgressBar)
        val root: LinearLayout? = itemView.findViewById(R.id.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.loan_details_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val loanDetails: LoanDetails = loansList!![position]
        holder.loanAmountValue?.text = loanDetails.loanAmount
        val rupeeSymbol = context.resources.getString(R.string.Rs)
        holder.loanAmountText?.text = "Loan Amount(${rupeeSymbol})"
        holder.loanTypeText?.text = loanDetails.loanType
        holder.loanAccountNo?.text = loanDetails.loanAccountNum
        if (loanDetails.loanType == "Business Loan") {
            holder.loanIconType?.setBackgroundResource(R.drawable.ic_business_loan)
        } else {
            holder.loanIconType?.setBackgroundResource(R.drawable.ic_personal_loan)
        }

        ObjectAnimator.ofInt(holder.loanProgressBar, "progress", 100).setDuration(1000).start()
        holder.root?.setOnClickListener {
            if (status.equals("1")){
                val intent = Intent(context, LoanDetailsActivity::class.java)
                context.startActivity(intent)
            }else if (status.equals("2")){
                val intent = Intent(context, GenerateLoanStatementActivity::class.java)
                context.startActivity(intent)
            } else if (status.equals("3")) {
                val intent = Intent(context, PaymentDuesActivity::class.java)
                context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int {
        return loansList!!.size
    }

}