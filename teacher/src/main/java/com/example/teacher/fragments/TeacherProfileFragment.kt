package com.example.teacher.fragments

import androidx.fragment.app.Fragment
import com.example.profile.BaseProfileFragment
import com.example.teacher.fragments.profile.TeacherProfileSettingsFragment
import com.example.teacher.fragments.profile.TeacherProfileStatisticsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the Profile Menu Item (Teacher)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Statistics and Settings
 **/
@AndroidEntryPoint
class TeacherProfileFragment : BaseProfileFragment() {

    override fun getFragments(): List<Fragment> {
        return listOf(TeacherProfileStatisticsFragment(), TeacherProfileSettingsFragment())
    }

    override fun getTabTitle(position: Int): String {
        return if (position == 0) "Statistics" else "Settings"
    }
}