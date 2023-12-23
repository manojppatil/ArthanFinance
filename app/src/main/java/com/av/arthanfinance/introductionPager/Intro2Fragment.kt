package com.av.arthanfinance.introductionPager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.av.arthanfinance.R
import com.clevertap.android.sdk.CleverTapAPI

class Intro2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
		var clevertapDefaultInstance: CleverTapAPI? = null
		clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(context)//added by CleverTap Assistant
		clevertapDefaultInstance?.pushEvent("Intro2 visit")//added by CleverTap Assistant

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro2, container, false)
    }
}