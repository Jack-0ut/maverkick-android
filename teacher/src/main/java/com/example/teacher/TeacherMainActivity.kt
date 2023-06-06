package com.example.teacher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.teacher.databinding.ActivityTeacherMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity Class for the Teacher
 * Here we initialize Menu and other things
 **/
@AndroidEntryPoint
class TeacherMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(binding.teacherNavHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.teacherBottomNavigationView, navController)
    }
}