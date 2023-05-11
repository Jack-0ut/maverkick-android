package com.example.maverkick

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.maverkick.databinding.ActivityOnboardBinding

/**
 * Activity that launches when the user just installed the app
 * and doesn't have the account set yet
 * Here user should choose whether to be Teacher or Student
 */
class OnboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.optionStudent.setOnClickListener {
            val intent = Intent(this, StudentRegistrationActivity::class.java)
            startActivity(intent)
        }

        binding.optionTeacher.setOnClickListener {
            val intent = Intent(this, TeacherRegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}