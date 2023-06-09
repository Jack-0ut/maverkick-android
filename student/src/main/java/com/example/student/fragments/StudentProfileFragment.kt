package com.example.student.fragments

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.profile.BaseProfileFragment
import com.example.student.fragments.profile_fragments.StudentProfileCoursesFragment
import com.example.student.fragments.profile_fragments.StudentProfileSettingsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for the Profile Menu Item (Student)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Courses and Settings
 **/
@AndroidEntryPoint
class StudentProfileFragment : BaseProfileFragment() {

    override fun getFragments(): List<Fragment> {
        return listOf(StudentProfileCoursesFragment(), StudentProfileSettingsFragment())
    }

    override fun getTabTitle(position: Int): String {
        return if (position == 0) "Courses" else "Settings"
    }

    override fun onChangeAccountClicked() {
        Toast.makeText(context, "Redirecting to the student", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://auth/onboarding_teacher"))
        startActivity(intent)
    }
}
