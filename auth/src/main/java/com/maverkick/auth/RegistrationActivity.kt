package com.maverkick.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.maverkick.auth.databinding.ActivityRegistrationBinding
import com.maverkick.auth.onboarding.student.StudentOnboarding
import com.maverkick.data.repositories.AuthRepository
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity that is responsible for the user registration
 * using email,username and password.
 **/
@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            when {
                !isValidEmail(email) -> showSnackbar(getString(R.string.invalid_email_message))
                !isValidUsername(username) -> showSnackbar(getString(R.string.invalid_username_message))
                !isValidPassword(password) -> showSnackbar(getString(R.string.invalid_password_message))
                else -> registerUser(email, username, password)
            }
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, username: String, password: String) {
        authRepository.register(email, username, password,
            onSuccess = { user ->
                sharedPrefManager.saveUser(user)
                sharedPrefManager.setIsOnboarded(false)
                startActivity(Intent(this, StudentOnboarding::class.java))
            },
            onFailure = {
                showSnackbar(getString(R.string.registration_failed_message))
                sharedPrefManager.clearUser()
                sharedPrefManager.clearPreferences()
            })
    }

    private fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidUsername(username: String) = username.length >= 5

    private fun isValidPassword(password: String) = password.length >= 6

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
