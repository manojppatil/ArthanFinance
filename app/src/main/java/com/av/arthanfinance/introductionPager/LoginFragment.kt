package com.av.arthanfinance.introductionPager

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.av.arthanfinance.FingerPrintLoginActivity
import com.av.arthanfinance.MPINLoginActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.RegistrationActivity
import com.clevertap.android.sdk.CleverTapAPI

class LoginFragment : Fragment() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        var clevertapDefaultInstance: CleverTapAPI? = null
        clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(context)//added by CleverTap Assistant
        clevertapDefaultInstance?.pushEvent("Intro3 visit")//added by CleverTap Assistant
        btnLogin = view.findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Login clicked")//added by CleverTap Assistant
            val intent =
                Intent(activity as IntroductionPagerActivity, MPINLoginActivity::class.java)
            intent.putExtra("context_from", 2)
            startActivity(intent)
            activity?.finish()

        }

        btnRegister = view.findViewById(R.id.btn_register)
        btnRegister.setOnClickListener {
            clevertapDefaultInstance?.pushEvent("Register clicked")//added by CleverTap Assistant
            val intent =
                Intent(activity as IntroductionPagerActivity, RegistrationActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}