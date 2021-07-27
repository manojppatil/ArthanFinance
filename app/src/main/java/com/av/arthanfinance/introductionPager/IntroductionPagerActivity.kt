package com.av.arthanfinance.introductionPager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.arthanfinance.core.base.BaseActivity
import com.av.arthanfinance.R

class IntroductionPagerActivity : BaseActivity() {
    override val layoutId: Int
        get() = R.layout.activity_introduction_pager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()
        val viewPager: ViewPager2 = findViewById(R.id.introViewPager)

        //val fragments: ArrayList<Fragment> = arrayListOf(Intro1Fragment(), Intro2Fragment(), LoginFragment())
        val fragments: ArrayList<Fragment> = arrayListOf(LoginFragment())
        val adapter = IntroPagerAdapter(fragments, this)
        viewPager.adapter = adapter
    }
}

class IntroPagerAdapter(val fragments: ArrayList<Fragment>, activity: AppCompatActivity): FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}