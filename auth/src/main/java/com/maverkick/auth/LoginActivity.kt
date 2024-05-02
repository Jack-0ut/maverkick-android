package com.maverkick.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.maverkick.auth.databinding.ActivityLoginBinding
import com.maverkick.data.repositories.AuthRepository
import com.maverkick.data.repositories.StudentRepository
import com.maverkick.data.repositories.TeacherRepository
import com.maverkick.data.repositories.UserRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity that will login user using email and password.
 * Then we're checking the Repository for the Student or Teacher
 * that correspond to such user
 **/
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var studentRepository: StudentRepository
    @Inject lateinit var teacherRepository: TeacherRepository
    @Inject lateinit var sharedPrefManager: SharedPrefManager
    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isEmpty()) {
                showSnackBar("Please enter email")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showSnackBar("Please enter password")
                return@setOnClickListener
            }

            authRepository.login(email, password,
                onSuccess = { userId ->
                    userRepository.getCurrentUser(
                        onSuccess = { user ->
                            sharedPrefManager.saveUser(user)
                            checkUserTypeAndNavigate(userId)
                        },
                        onFailure = { e ->
                            showSnackBar("Failed to fetch user: ${e.message}")
                        }
                    )
                },
                onFailure = { e ->
                    showSnackBar("Login failed: ${e.message}")
                }
            )
        }

        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    /** Checking on who's the user (Student or Teacher or both) **/
    private fun checkUserTypeAndNavigate(userId: String) {
        var isStudent = false
        var isTeacher = false

        lifecycleScope.launch {

            val studentResult = async { studentRepository.getStudentById(userId) }
            val teacherResult = async { teacherRepository.getTeacherById(userId) }

            studentResult.await().fold(
                onSuccess = {
                    isStudent = true
                    sharedPrefManager.saveStudent(it)
                },
                onFailure = {
                    showSnackBar(getString(R.string.error_fetching_data))
                }
            )

            teacherResult.await().fold(
                onSuccess = {
                    isTeacher = true
                    sharedPrefManager.saveTeacher(it)
                },
                onFailure = {
                    showSnackBar(getString(R.string.error_fetching_data))
                }
            )

            // Check for the different cases when we have role == student | teacher or both
            when {
                isStudent && isTeacher -> showRoleSelectionDialog()
                isStudent -> navigateToMainActivity("student")
                isTeacher -> navigateToMainActivity("teacher")
                else -> {
                    showSnackBar(getString(R.string.no_role_found, userId))
                }
            }
        }
    }

    /** If we have user, who's Student and Teacher at the same time, let user to choose as whom login **/
    private fun showRoleSelectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.choose_account_type))
        builder.setItems(arrayOf(getString(R.string.student), getString(R.string.teacher))) { _, which ->
            when(which) {
                0 -> navigateToMainActivity("student")
                1 -> navigateToMainActivity("teacher")
            }
        }
        builder.show()
    }

    /** Navigate to the main activity based on role (Either Student or Teacher) **/
    private fun navigateToMainActivity(role: String) {
        // Save user role to shared preferences
        sharedPrefManager.saveActiveRole(role)

        val intentUri = when (role) {
            "student" -> Uri.parse("maverkick://student/main")
            "teacher" -> Uri.parse("maverkick://teacher/main")
            else -> throw IllegalArgumentException("Unknown role: $role")
        }
        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        startActivity(intent)
        finish()
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
