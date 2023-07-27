package com.maverkick.profile


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter used for the Profile Fragment to separate the functional into
 * different sub-fragments
 * @param fragment The host fragment in which the ViewPager2 is placed.
 * @param fragments The list of fragments you want to display in the ViewPager2
 **/
class ProfilePageAdapter(fragment: Fragment, private val fragments: List<Fragment>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}