package com.av.arthanfinance.homeTabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.av.arthanfinance.CustomerHomeTabResponse
import com.av.arthanfinance.R
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_home_tab.*


class NoLoanHomeFragment : Fragment() {

    var customerData: CustomerHomeTabResponse? = null
    private lateinit var customerName: TextView
    private lateinit var customerLastLogin: TextView
    private lateinit var circleImg : CircleImageView
    private var sampleImages: IntArray = intArrayOf(
        R.drawable.image38,
        R.drawable.image41,
        R.drawable.image38,
        R.drawable.image41
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imagesList = arrayListOf<ImageData>()
        val posters = resources.obtainTypedArray(R.array.posters)

        for (i in 0 until posters.length()) {
            imagesList.add(
                ImageData(
                    posters.getResourceId(i, -1),
                    "str1"
                )
            )
        }

        val custName = customerData?.customerName
        customerName.text = custName

        val loginTime = customerData?.lastLoginTime
        customerLastLogin.text = loginTime
        if (customerData?.customerImage.equals("") || customerData?.customerImage == null){
                Glide
                    .with(this@NoLoanHomeFragment)
                    .load(R.drawable.ic_user).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(circleImg);

        }else{
            customerData?.customerImage?.let {
                Glide
                    .with(this@NoLoanHomeFragment)
                    .load(it).fitCenter()
                    .placeholder(R.drawable.ic_arthan_logo)
                    .into(circleImg);
            }
        }


        posters.recycle()
        cardSliderViewPager.adapter = customerData?.let { CarouselImageAdapter(imagesList, it) }

        carouselView.pageCount = sampleImages.size
        carouselView.setIndicatorVisibility(View.INVISIBLE)
        carouselView.setImageListener{ position, imageView ->
            imageView.setImageResource(sampleImages[position])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_tab, container, false)

        customerName = view.findViewById(R.id.customerName)
        customerLastLogin = view.findViewById(R.id.customerLastLogin)
        circleImg = view.findViewById(R.id.circle_img)
        return view
    }
}