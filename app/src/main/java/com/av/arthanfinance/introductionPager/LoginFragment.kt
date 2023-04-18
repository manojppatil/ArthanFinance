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

class LoginFragment : Fragment() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        btnLogin = view.findViewById(R.id.btn_login)
        btnLogin.setOnClickListener {
//            val intent = Intent(activity?.applicationContext, FingerPrintLoginActivity::class.java)
            val intent = Intent(activity as IntroductionPagerActivity, MPINLoginActivity::class.java)
            intent.putExtra("context_from", 2)
            startActivity(intent)
            activity?.finish()
        }

        btnRegister = view.findViewById(R.id.btn_register)
        btnRegister.setOnClickListener {
            val intent = Intent(activity as IntroductionPagerActivity, RegistrationActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        return view
    }
}