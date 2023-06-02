package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.databinding.ActivityLoginBinding
import com.example.data.repositories.AuthRepository
import com.example.data.repositories.StudentRepository
import com.example.data.repositories.TeacherRepository
import com.example.data.repositories.UserRepository
import com.example.data.sharedpref.SharedPrefManager
import com.example.student.StudentMainActivity
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
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    userRepository.getCurrentUser(
                        onSuccess = { user ->
                            sharedPrefManager.saveUsername(user.username)
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

        // go to the registration page
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
            // Check for the student with that userId
            val studentCheck = async {
                studentRepository.getStudentById(
                    userId,
                    onSuccess = {
                        isStudent = true
                        sharedPrefManager.saveStudentId(userId)  // Save student ID to shared preferences
                    },
                    onFailure = { e -> println("Error fetching student: ${e.message}") }
                )
            }

            // Check for the teacher with that userId
            val teacherCheck = async {
                teacherRepository.getTeacherById(
                    userId,
                    onSuccess = {
                        isTeacher = true
                        sharedPrefManager.saveTeacherId(userId)  // Save teacher ID to shared preferences
                    },
                    onFailure = { e -> println("Error fetching teacher: ${e.message}") }
                )
            }

            studentCheck.await()
            teacherCheck.await()

            when {
                isStudent && isTeacher -> showRoleSelectionDialog()
                isStudent -> navigateToMainActivity("student")
                isTeacher -> navigateToMainActivity("teacher")
                else -> {
                    //TODO find what to do here
                    Toast.makeText(this@LoginActivity, "No role found for user: $userId", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** If we have user, who's Student and Teacher at the same type, let to choose as whom login **/
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

    /** Navigate to the activity based on role **/
    private fun navigateToMainActivity(role: String) {
        // Save user role to shared preferences
        sharedPrefManager.saveActiveRole(role)

        val intent = when (role) {
            "student" -> Intent(this, StudentMainActivity::class.java)
            //"teacher" -> Intent(this, TeacherMainActivity::class.java)
            else -> throw IllegalArgumentException("Unknown role: $role")
        }
        startActivity(intent)
        finish()
    }

}
