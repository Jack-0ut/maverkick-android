package com.maverkick.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.maverkick.auth.databinding.ActivityRoleSelectionBinding

import com.maverkick.auth.onboarding.student.StudentOnboarding
import com.maverkick.auth.onboarding.teacher.TeacherOnboarding


/**
 * Activity that launches when the user just installed the app
 * and doesn't have the account set yet
 * Here user should choose whether to be Teacher or Student
 */
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.optionTeacher.setOnClickListener {
            navigateToMain("teacher")
        }

        binding.optionStudent.setOnClickListener {
            navigateToMain("student")
        }
    }

    /**When user choose the specific role, navigate to the specific onboarding **/
    private fun navigateToMain(role: String) {
        val intent = when (role) {
            "student" -> Intent(this, StudentOnboarding::class.java)
            "teacher" -> Intent(this, TeacherOnboarding::class.java)
            else -> throw IllegalArgumentException("Invalid role: $role")
        }
        startActivity(intent)
    }

}

