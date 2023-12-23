package com.av.arthanfinance.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.LoanDetails
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LoanDetailsAdapter(
    private val loansList: ArrayList<LoanDetails>,
    private val listener: LoanItemClickListener,
) : RecyclerView.Adapter<LoanDetailsAdapter.LoanDetailsViewHolder>() {

    class LoanDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val loanTypeText: TextView? = itemView.findViewById(R.id.transaction_name)
        private val loanAmountValue: TextView? = itemView.findViewById(R.id.transaction_amount)
        private val transactionDate: TextView? = itemView.findViewById(R.id.transaction_date)
        private val transactionType: TextView? = itemView.findViewById(R.id.transaction_type)
        private val root: LinearLayout? = itemView.findViewById(R.id.root)

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(loanDetails: LoanDetails, listener: LoanItemClickListener) {
            val rupeeSymbol = itemView.context.resources.getString(R.string.Rs)
            val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat: DateFormat = SimpleDateFormat("dd MMM yyyy")
            val inputDateStr = loanDetails.transactionDateStr
            val date: Date = inputFormat.parse(inputDateStr.toString())!!
            val outputDateStr: String = outputFormat.format(date)
            loanTypeText?.text = "" + loanDetails.transactionName
            if (loanDetails.accountEntryType == "Debit") {
                loanAmountValue?.setTextColor(Color.GREEN)
                loanAmountValue?.text = "+" + loanDetails.amount
            } else if (loanDetails.accountEntryType == "Credit") {
                loanAmountValue?.setTextColor(Color.RED)
                loanAmountValue?.text = "-" + loanDetails.amount
            }
            loanAmountValue?.text = "" + loanDetails.amount
            transactionDate?.text = "" + outputDateStr
            transactionType?.text = "" + loanDetails.accountEntryType
            root?.setOnClickListener {
                listener.onLoanItemClicked(loanDetails)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanDetailsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.loan_details_layout, parent, false)
        return LoanDetailsViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return loansList.size
    }

    override fun onBindViewHolder(holder: LoanDetailsViewHolder, position: Int) {
        val loanDetails: LoanDetails = loansList[position]
        holder.bind(loanDetails, listener)
    }

}

interface LoanItemClickListener {
    fun onLoanItemClicked(loanDetails: LoanDetails)
}