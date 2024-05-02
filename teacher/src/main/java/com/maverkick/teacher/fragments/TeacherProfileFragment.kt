package com.maverkick.teacher.fragments

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.maverkick.profile.BaseProfileFragment
import com.maverkick.profile.ProfileViewModelInterface
import com.maverkick.teacher.fragments.profile.TeacherProfileSettingsFragment
import com.maverkick.teacher.fragments.profile.TeacherProfileStatisticsFragment
import com.maverkick.teacher.viewmodels.TeacherProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment for the Profile Menu Item (Teacher)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Statistics and Settings
 **/
@AndroidEntryPoint
class TeacherProfileFragment : BaseProfileFragment() {
    private val teacherViewModel: TeacherProfileViewModel by viewModels()

    override fun getFragments(): List<Fragment> =
        listOf(TeacherProfileStatisticsFragment(), TeacherProfileSettingsFragment())

    override fun getTabTitle(position: Int): String =
        if (position == 0) "Statistics" else "Settings"

    override fun getViewModel(): ProfileViewModelInterface = teacherViewModel

    /** Teacher wants to switch to the student view **/
    override fun onChangeAccountClicked() {
        lifecycleScope.launch {
            handleAccountSwitching()
        }
    }

    private suspend fun handleAccountSwitching() {
        val studentExists = teacherViewModel.checkStudentAccountExists()
        val intentUriString = if (studentExists) "maverkick://student/main" else "maverkick://auth/onboarding_student"

        withContext(Dispatchers.Main) {
            val intentUri = Uri.parse(intentUriString)
            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
