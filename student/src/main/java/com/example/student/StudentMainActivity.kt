package com.example.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.student.databinding.ActivityMainStudentBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity Class for the Student
 * Here we initialize Menu and other things
 **/
@AndroidEntryPoint
class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView, navController)
    }
}