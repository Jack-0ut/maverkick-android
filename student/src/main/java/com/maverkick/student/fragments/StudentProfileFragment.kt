package com.maverkick.student.fragments

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.maverkick.profile.BaseProfileFragment
import com.maverkick.profile.ProfileViewModelInterface
import com.maverkick.student.fragments.profile.StudentProfileCoursesFragment
import com.maverkick.student.fragments.profile.StudentProfileSettingsFragment
import com.maverkick.student.viewmodels.StudentProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for the Profile Menu Item (Student)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Courses and Settings
 **/
@AndroidEntryPoint
class StudentProfileFragment : BaseProfileFragment() {

    private val studentViewModel: StudentProfileViewModel by viewModels()

    override fun getFragments(): List<Fragment> {
        return listOf(StudentProfileCoursesFragment(), StudentProfileSettingsFragment())
    }

    override fun getTabTitle(position: Int): String {
        return if (position == 0) "Courses" else "Settings"
    }

    override fun getViewModel(): ProfileViewModelInterface {
        return studentViewModel
    }

    /** Student wants to switch to the teacher view **/
    override fun onChangeAccountClicked() {
        lifecycleScope.launch{
            val teacherExists = studentViewModel.checkTeacherAccountExists()

            // Assuming the existence of a method to check if the teacher account exists
            if (teacherExists) {
                // Redirect to the TeacherMainActivity
                val intentUri = Uri.parse("maverkick://teacher/main")
                val intent = Intent(Intent.ACTION_VIEW, intentUri)
                startActivity(intent)
            } else {
                // Redirect to the onboarding teacher activity
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://auth/onboarding_teacher"))
                startActivity(intent)
            }
        }
    }

}