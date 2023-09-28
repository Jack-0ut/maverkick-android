package com.maverkick.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.maverkick.student.databinding.ActivityStudentMainBinding
import dagger.hilt.android.AndroidEntryPoint
/**
 * Main Activity Class for the Student
 * Here we initialize Menu and other things
 **/
@AndroidEntryPoint
class StudentMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(binding.studentNavHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.studentBottomNavigationView, navController)
    }
}