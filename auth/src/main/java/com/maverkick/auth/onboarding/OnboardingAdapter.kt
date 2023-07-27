package com.maverkick.auth.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter for the Onboarding process, which allow to display
 * the onboarding as the separate parts
 **/
class OnboardingAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val fragments = ArrayList<Fragment>()
    fun addFragment(fragment: Fragment) { fragments.add(fragment) }
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}