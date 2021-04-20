package com.example.arthanfinance.introductionPager

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.arthanfinance.FingerPrintLoginActivity
import com.example.arthanfinance.MPINLoginActivity
import com.example.arthanfinance.R
import com.example.arthanfinance.RegistrationActivity

class LoginFragment : Fragment() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        btnLogin = view.findViewById(R.id.btn_login)
        btnLogin.setOnClickListener{
//            val intent = Intent(activity?.applicationContext, FingerPrintLoginActivity::class.java)
            val intent = Intent(activity?.applicationContext, MPINLoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister = view.findViewById(R.id.btn_register)
        btnRegister.setOnClickListener{
            val intent = Intent(activity?.applicationContext, RegistrationActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}