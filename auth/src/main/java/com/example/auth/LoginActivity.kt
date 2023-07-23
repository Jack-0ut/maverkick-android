package com.example.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.auth.databinding.ActivityLoginBinding
import com.example.data.repositories.AuthRepository
import com.example.data.repositories.StudentRepository
import com.example.data.repositories.TeacherRepository
import com.example.data.repositories.UserRepository
import com.example.data.sharedpref.SharedPrefManager
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

        // user clicks on the login button
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            // Check if email or password is empty
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // call the login function
            authRepository.login(email, password,
                onSuccess = { userId ->
                    userRepository.getCurrentUser(
                        onSuccess = { user ->
                            sharedPrefManager.saveUser(user)
                            checkUserTypeAndNavigate(userId)
                        },
                        onFailure = { e ->
                            Toast.makeText(this, "Failed to fetch user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFailure = { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // navigate to the registration page
        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    /**Checking on who's the user (Student or Teacher or both) **/
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
                onFailure = { println("Error fetching student: ${it.message}") }
            )

            teacherResult.await().fold(
                onSuccess = {
                    isTeacher = true
                    sharedPrefManager.saveTeacher(it)
                },
                onFailure = { println("Error fetching teacher: ${it.message}") }
            )
            // check for the different cases when we have role == student | teacher or both
            when {
                isStudent && isTeacher -> showRoleSelectionDialog()
                isStudent -> navigateToMainActivity("student")
                isTeacher -> navigateToMainActivity("teacher")
                else -> {
                    Toast.makeText(this@LoginActivity, "No role found for user: $userId", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** If we have user, who's Student and Teacher at the same time, let user to choose as whom login **/
    private fun showRoleSelectionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("You've got two accounts: Choose the one ")
        builder.setItems(arrayOf("Student", "Teacher")) { _, which ->
            when(which) {
                0 -> {
                    navigateToMainActivity("student")
                }
                1 -> {
                    navigateToMainActivity("teacher")
                }
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
}
