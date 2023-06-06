package com.example.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auth.databinding.ActivityRegistrationBinding
import com.example.data.repositories.AuthRepository
import com.example.data.sharedpref.SharedPrefManager
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

        // The registration process
        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val username = binding.usernameInput.text.toString()
            val password = binding.passwordInput.text.toString()

            // Validate the input fields
            if (isValidEmail(email) && isValidUsername(username) && isValidPassword(password)) {
                authRepository.register(email, username, password,
                    onSuccess = { user ->
                        // Save the user object to shared preferences
                        sharedPrefManager.saveUser(user)
                        // Navigate to RoleSelectActivity
                        val intent = Intent(this, RoleSelectionActivity::class.java)
                        startActivity(intent)
                    },
                    onFailure = { e ->
                        Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        // show an error message or do something else on failure
                    }
                )

            } else {
                Toast.makeText(this, "Please enter valid email, username, and password", Toast.LENGTH_SHORT).show()
            }
        }

        // navigate to the login screen
        binding.loginText.setOnClickListener {
            // Navigate to login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    // Validate email address
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Validate username
    private fun isValidUsername(username: String): Boolean {
        // Add any specific conditions for a valid username, e.g., minimum length
        return username.length >= 5
    }

    // Validate password
    private fun isValidPassword(password: String): Boolean {
        // Add any specific conditions for a valid password, e.g., minimum length, at least one number and one letter, etc.
        return password.length >= 6
    }
}
