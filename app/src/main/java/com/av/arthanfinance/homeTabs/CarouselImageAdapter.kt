package com.av.arthanfinance.homeTabs

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.InitiateApplyLoanActivity
import com.av.arthanfinance.util.ArthanFinConstants
import com.github.islamkhsh.CardSliderAdapter
import kotlinx.android.synthetic.main.dashboard_carousel_card.view.*

    class CarouselImageAdapter(private val imgList: List<ImageData>, val customerData: CustomerHomeTabResponse) : CardSliderAdapter<CarouselImageAdapter.CarouselImageViewHolder>() {

    override fun bindVH(holder: CarouselImageViewHolder, position: Int) {
        val image = imgList[position]
        holder.itemView.run {
            dashboard_carouselimageview.setImageResource(image.index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_carousel_card, parent, false)
        view.parentHome.setOnClickListener {
            val intent = Intent(it.context, InitiateApplyLoanActivity::class.java)
            intent.putExtra(ArthanFinConstants.IS_CREATE_FLOW,true)
            intent.putExtra("customerData", customerData)
            it.context.startActivity(intent)
        }
        return CarouselImageViewHolder(view)
    }

        override fun getItemCount() = imgList.size

    class CarouselImageViewHolder(view: View) : RecyclerView.ViewHolder(view)
}