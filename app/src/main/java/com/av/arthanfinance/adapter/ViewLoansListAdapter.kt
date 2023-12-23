package com.av.arthanfinance.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import com.av.arthanfinance.applyLoan.model.LoanDetails
import com.av.arthanfinance.serviceRequest.GenerateLoanStatementActivity
import com.av.arthanfinance.serviceRequest.LoanDetailsActivity
import com.av.arthanfinance.serviceRequest.PaymentDuesActivity
import java.util.ArrayList

class ViewLoansListAdapter(
    private val loansList: ArrayList<LoanDetails>?,
    private val context: Context,
    private val status: String,
) : RecyclerView.Adapter<ViewLoansListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val loanTypeText: TextView? = itemView.findViewById(R.id.transaction_name)
        val loanAmountValue: TextView? = itemView.findViewById(R.id.transaction_amount)
        val transactionDate: TextView? = itemView.findViewById(R.id.transaction_date)
        val transactionType: TextView? = itemView.findViewById(R.id.transaction_type)
        val loanIconType: ImageView? = itemView.findViewById(R.id.loanIconType)
        val root: LinearLayout? = itemView.findViewById(R.id.root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.loan_details_layout, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val loanDetails: LoanDetails = loansList!![position]
        val rupeeSymbol = context.resources.getString(R.string.Rs)
        holder.loanAmountValue?.text = loanDetails.amount
        holder.loanTypeText?.text = loanDetails.transactionName
        holder.transactionDate?.text = "" + loanDetails.transactionDateStr
        holder.transactionType?.text = "" + loanDetails.accountEntryType

        holder.root?.setOnClickListener {
            when (status) {
                "1" -> {
                    val intent = Intent(context, LoanDetailsActivity::class.java)
                    context.startActivity(intent)
                }
                "2" -> {
                    val intent = Intent(context, GenerateLoanStatementActivity::class.java)
                    context.startActivity(intent)
                }
                "3" -> {
                    val intent = Intent(context, PaymentDuesActivity::class.java)
                    context.startActivity(intent)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return loansList!!.size
    }

}