package com.av.arthanfinance.serviceRequest

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.av.arthanfinance.R
import kotlinx.android.synthetic.main.activity_transaction_details.*

class TransactionDetails : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        account_id_dt.text = intent.getStringExtra("accountId")
        transaction_name_dt.text = intent.getStringExtra("transactionName")
        transaction_date_dt.text = intent.getStringExtra("transactionDateStr")
        account_entry_type_dt.text = intent.getStringExtra("accountEntryType")
        transaction_amount_dt.text =
            applicationContext.getString(R.string.Rs) + " " + intent.getStringExtra("amount")
        description_dt.text = intent.getStringExtra("description")
        transaction_id_dt.text = intent.getStringExtra("transactionId")
    }
}