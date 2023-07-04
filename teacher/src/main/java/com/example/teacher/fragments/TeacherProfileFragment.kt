package com.example.teacher.fragments

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.profile.BaseProfileFragment
import com.example.profile.ProfileViewModelInterface
import com.example.teacher.fragments.profile.TeacherProfileSettingsFragment
import com.example.teacher.fragments.profile.TeacherProfileStatisticsFragment
import com.example.teacher.viewmodels.TeacherProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for the Profile Menu Item (Teacher)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Statistics and Settings
 **/
@AndroidEntryPoint
class TeacherProfileFragment : BaseProfileFragment() {
    private val teacherViewModel: TeacherProfileViewModel by viewModels()

    override fun getFragments(): List<Fragment> {
        return listOf(TeacherProfileStatisticsFragment(), TeacherProfileSettingsFragment())
    }

    override fun getTabTitle(position: Int): String {
        return if (position == 0) "Statistics" else "Settings"
    }

    override fun getViewModel(): ProfileViewModelInterface {
        return teacherViewModel
    }

    /** Teacher wants to switch to the student view **/
    override fun onChangeAccountClicked() {
        lifecycleScope.launch {
            val studentExists = teacherViewModel.checkStudentAccountExists()

            if (studentExists) {
                // Redirect to the StudentMainActivity
                val intentUri = Uri.parse("app://student/main")
                val intent = Intent(Intent.ACTION_VIEW, intentUri)
                startActivity(intent)
            } else {
                // Redirect to the onboarding student activity
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://auth/onboarding_student"))
                startActivity(intent)
            }
        }
    }

}
