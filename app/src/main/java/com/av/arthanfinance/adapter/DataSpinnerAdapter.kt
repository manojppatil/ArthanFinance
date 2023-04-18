package com.av.arthanfinance.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.av.arthanfinance.applyLoan.model.Data

class DataSpinnerAdapter(context: Context, val list: MutableList<Data>) :
    ArrayAdapter<Data>(context, android.R.layout.simple_spinner_item, android.R.id.text1) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rootView = super.getView(position, convertView, parent)
        return createView(position, rootView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val rootView = super.getDropDownView(position, convertView, parent)
        return createView(position, rootView, parent)
    }

    override fun getItem(position: Int): Data? = list[position]

    override fun getCount(): Int = list.size

    public fun getListData():MutableList<Data> {
       return list
    }
    private fun createView(position: Int, convertView: View, parent: ViewGroup): View {
        val textView = convertView as? TextView
        val lp = textView?.layoutParams as? ViewGroup.MarginLayoutParams
        lp?.width = ViewGroup.MarginLayoutParams.MATCH_PARENT
        lp?.let {
            textView?.layoutParams = it
        }
        textView?.text = list[position].value
        return convertView
    }
}