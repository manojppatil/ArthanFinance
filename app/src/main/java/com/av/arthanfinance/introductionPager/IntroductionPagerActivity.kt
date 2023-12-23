package com.av.arthanfinance.introductionPager

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R
import com.av.arthanfinance.util.ArthanFinConstants

class IntroductionPagerActivity : BaseActivity() {
    override val layoutId: Int
        get() = R.layout.activity_introduction_pager

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()
        val viewPager: ViewPager2 = findViewById(R.id.introViewPager)

        val fragments: ArrayList<Fragment> =
            arrayListOf(Intro1Fragment(), Intro2Fragment(), LoginFragment())
//        val fragments: ArrayList<Fragment> = arrayListOf(LoginFragment())
        val adapter = IntroPagerAdapter(fragments, this)
        viewPager.adapter = adapter
//        viewPager.setOnTouchListener(object : View.OnTouchListener {
//            private var pointX = 0f
//            private var pointY = 0f
//            private val tolerance = 50
//            override fun onTouch(v: View?, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_MOVE -> return false //This is important, if you return TRUE the action of swipe will not take place.
////                    MotionEvent.ACTION_DOWN -> {
////                        pointX = event.x
////                        pointY = event.y
////                    }
////                    MotionEvent.ACTION_UP -> {
////                        val sameX =
////                            pointX + tolerance > event.x && pointX - tolerance < event.x
////                        val sameY =
////                            pointY + tolerance > event.y && pointY - tolerance < event.y
////                        if (sameX && sameY) {
////                            //The user "clicked" certain point in the screen or just returned to the same position an raised the finger
////                        }
////                    }
//                }
//                return false
//            }
//        })

        val sharedPref: SharedPreferences =
            getSharedPreferences("customerData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(ArthanFinConstants.isMpinSet, false)
        editor.putBoolean(ArthanFinConstants.isFingerPrintSet, false)
        editor.apply()
    }
}

class IntroPagerAdapter(val fragments: ArrayList<Fragment>, activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}