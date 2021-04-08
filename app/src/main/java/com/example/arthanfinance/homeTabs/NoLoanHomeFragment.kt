package com.example.arthanfinance.homeTabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.arthanfinance.CustomerHomeTabResponse
import com.example.arthanfinance.R
import kotlinx.android.synthetic.main.fragment_home_tab.*


class NoLoanHomeFragment : Fragment() {

    var customerData: CustomerHomeTabResponse? = null
    private lateinit var customerName: TextView
    private lateinit var customerLastLogin: TextView
    private var sampleImages: IntArray = intArrayOf(
        R.drawable.image38,
        R.drawable.image41,
        R.drawable.image38,
        R.drawable.image41
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        customerName.setText(custName)

        val loginTime = customerData?.lastLoginTime
        customerLastLogin.setText(loginTime)

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

        return view
    }
}