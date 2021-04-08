package com.example.arthanfinance.applyLoan

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arthanfinance.R

class LoanDetailsAdapter(private val loansList: ArrayList<LoanDetails>) : RecyclerView.Adapter<LoanDetailsAdapter.LoanDetailsViewHolder>() {

    class LoanDetailsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val loanAmountValue: TextView? = itemView.findViewById<TextView>(R.id.loanAmountValue)
        private val loanAmountText: TextView? = itemView.findViewById<TextView>(R.id.loanAmountText)
        private val loanAccountNo: TextView? = itemView.findViewById<TextView>(R.id.loanAccountNo)
        private val loanTypeText: TextView? = itemView.findViewById<TextView>(R.id.loanTypeText)
        private val loanIconType: ImageView? = itemView.findViewById<ImageView>(R.id.loanIconType)
        private val loanProgressBar: ProgressBar = itemView.findViewById<ProgressBar>(R.id.loanProgressBar)

        fun bind(loanDetails: LoanDetails) {
            loanAmountValue?.text = loanDetails.loanAmount
            val rupeeSymbol = itemView.context.resources.getString(R.string.Rs)
            loanAmountText?.text = "Loan Amount(${rupeeSymbol})"
            loanTypeText?.text = loanDetails.loanType
            loanAccountNo?.text = loanDetails.loanAccountNum
            if (loanDetails.loanType == "Business Loan") {
                loanIconType?.setBackgroundResource(R.drawable.ic_business_loan)
            } else {
                loanIconType?.setBackgroundResource(R.drawable.ic_personal_loan)
            }

            val percentCompleted = Integer.parseInt(loanDetails?.percentCompleted) //to be achieved from BE
            loanProgressBar.max = 100
            ObjectAnimator.ofInt(loanProgressBar, "progress", percentCompleted).setDuration(1000).start()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanDetailsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.loan_details_layout, parent, false)
        return LoanDetailsViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return loansList.size
    }

    override fun onBindViewHolder(holder: LoanDetailsViewHolder, position: Int) {
        val loanDetails: LoanDetails = loansList[position]
        holder.bind(loanDetails)
    }

}