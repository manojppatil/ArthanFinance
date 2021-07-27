package com.av.arthanfinance.applyLoan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.R

public class StringAdapter(private val uploadDocsList: ArrayList<String>) : RecyclerView.Adapter<StringAdapter.StringViewHolder>() {

    class StringViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val descriptionText: TextView? = itemView.findViewById<TextView>(R.id.fileNameText)

        fun bind(fileName: String) {
            descriptionText?.text = fileName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.string_layout, parent, false)
        return StringViewHolder(view)
    }

    override fun getItemCount(): Int {
        return uploadDocsList.size
    }

    override fun onBindViewHolder(holder: StringViewHolder, position: Int) {
        holder.bind(uploadDocsList[position])
    }

}