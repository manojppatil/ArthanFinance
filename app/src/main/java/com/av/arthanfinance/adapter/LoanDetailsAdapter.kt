package com.av.arthanfinance.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.model.LoanDetails

class LoanDetailsAdapter(
    private val loansList: ArrayList<LoanDetails>,
    private val listener: LoanItemClickListener,
) : RecyclerView.Adapter<LoanDetailsAdapter.LoanDetailsViewHolder>() {

    class LoanDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val loanAmountValue: TextView? = itemView.findViewById(R.id.loanAmountValue)
        private val loanAmountText: TextView? = itemView.findViewById(R.id.loanAmountText)
        private val loanAccountNo: TextView? = itemView.findViewById(R.id.loanAccountNo)
        private val loanTypeText: TextView? = itemView.findViewById(R.id.loanTypeText)
        private val loanIconType: ImageView? = itemView.findViewById(R.id.loanIconType)
        private val loanProgressBar: ProgressBar = itemView.findViewById(R.id.loanProgressBar)
        private val payBtn: Button = itemView.findViewById(R.id.payBtn)
        private val root: LinearLayout? = itemView.findViewById(R.id.root)

        @SuppressLint("SetTextI18n")
        fun bind(loanDetails: LoanDetails, listener: LoanItemClickListener) {
            loanAmountValue?.text = "-" + loanDetails.loanAmount
            val rupeeSymbol = itemView.context.resources.getString(R.string.Rs)
            loanAmountText?.text = "Amount(${rupeeSymbol})"
            loanTypeText?.text = "DEBIT(${rupeeSymbol})"
            loanAccountNo?.text = loanDetails.loanAccountNum
            if (loanDetails.loanType == "Business Loan") {
                loanIconType?.setBackgroundResource(R.drawable.ic_business_loan)
            } else {
                loanIconType?.setBackgroundResource(R.drawable.ic_personal_loan)
            }

//            val percentCompleted = Integer.parseInt(loanDetails?.percentCompleted) //to be achieved from BE
            loanProgressBar.max = 100
            ObjectAnimator.ofInt(loanProgressBar, "progress", 100).setDuration(1000).start()
            payBtn.setOnClickListener {
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