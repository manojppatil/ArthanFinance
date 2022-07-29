package com.av.arthanfinance.homeTabs

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arthanfinance.core.util.FiniculeUtil
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.InitiateApplyLoanActivity2
import com.av.arthanfinance.R
import com.av.arthanfinance.applyLoan.ApplyCoupon
import com.av.arthanfinance.applyLoan.InitiateApplyLoanActivity
import com.av.arthanfinance.applyLoan.LoanProcessResponse
import com.av.arthanfinance.networkService.ApiClient
import com.av.arthanfinance.user_kyc.UploadBusinessDetailsActivity
import com.av.arthanfinance.util.ArthanFinConstants
import com.github.islamkhsh.CardSliderAdapter
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_initiate_apply_loan.*
import kotlinx.android.synthetic.main.dashboard_carousel_card.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarouselImageAdapter(
    private val imgList: List<ImageData>,
    val customerData: CustomerHomeTabResponse
) : CardSliderAdapter<CarouselImageAdapter.CarouselImageViewHolder>() {

    override fun bindVH(holder: CarouselImageViewHolder, position: Int) {
        val image = imgList[position]
        holder.itemView.run {
            dashboard_carouselimageview.setImageResource(image.index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_carousel_card, parent, false)
        view.applyLoan.setOnClickListener {
//            sendLoanRequest(parent.context)
            val intent = Intent(it.context, UploadBusinessDetailsActivity::class.java)
            intent.putExtra(ArthanFinConstants.IS_CREATE_FLOW, true)
            intent.putExtra(ArthanFinConstants.MOVE_TO_BUSINESS_DETAILS, true)
            intent.putExtra("customerData", customerData)
            intent.putExtra("loanResponse", "")
            it.context.startActivity(intent)
        }
        return CarouselImageViewHolder(view)
    }

    private fun sendLoanRequest(context: Context) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("customerId", customerData.customerId)
        ApiClient().getAuthApiService(context).applyForLoan(jsonObject)
            .enqueue(object : Callback<LoanProcessResponse> {
                override fun onResponse(
                    call: Call<LoanProcessResponse>,
                    response: Response<LoanProcessResponse>
                ) {
                    val loanResponse = response.body()
                    if (loanResponse != null) {
                        if (loanResponse.limitHit.equals("No")) {
                            loanResponse.eligilityAmount = response.body()!!.eligilityAmount
                            navigateToBusinessDetailsActivity(loanResponse, true, context)
                        } else if (loanResponse.limitHit.equals("Yes")) {
                            Toast.makeText(
                                context,
                                "Sorry, You are not Eligible to Apply for new Loan.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
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

    private fun navigateToBusinessDetailsActivity(
        loanResponse: LoanProcessResponse,
        navigateToBusinessDetails: Boolean, context: Context
    ) {
        val intent =
            Intent(context, UploadBusinessDetailsActivity::class.java)
        intent.putExtra(ArthanFinConstants.IS_CREATE_FLOW, true)
        intent.putExtra(ArthanFinConstants.MOVE_TO_BUSINESS_DETAILS, navigateToBusinessDetails)
        intent.putExtra("loanResponse", loanResponse)
        context.startActivity(intent)
    }

    override fun getItemCount() = imgList.size

    class CarouselImageViewHolder(view: View) : RecyclerView.ViewHolder(view)
}