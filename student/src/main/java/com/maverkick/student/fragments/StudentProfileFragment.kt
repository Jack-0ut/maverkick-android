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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment for the Profile Menu Item (Student)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Courses and Settings
 **/
@AndroidEntryPoint
class StudentProfileFragment : BaseProfileFragment() {

    private val studentViewModel: StudentProfileViewModel by viewModels()

    override fun getFragments(): List<Fragment> =
        listOf(StudentProfileCoursesFragment(), StudentProfileSettingsFragment())

    override fun getTabTitle(position: Int): String =
        if (position == 0) "Courses" else "Settings"

    override fun getViewModel(): ProfileViewModelInterface = studentViewModel

    override fun onChangeAccountClicked() {
        lifecycleScope.launch {
            handleAccountSwitching()
        }
    }

    private suspend fun handleAccountSwitching() {
        val teacherExists = studentViewModel.checkTeacherAccountExists()
        val intentUriString = if (teacherExists) "maverkick://teacher/main" else "maverkick://auth/onboarding_teacher"

        withContext(Dispatchers.Main) {
            val intentUri = Uri.parse(intentUriString)
            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
