package com.example.app.onboarding.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.app.databinding.ActivityStudentOnboardingBinding
import com.example.app.onboarding.student.fragments.AgeFragment
import com.example.app.onboarding.student.fragments.DailyStudyFragment
import com.example.app.onboarding.student.fragments.SkillsFragment
import com.example.student.StudentMainActivity
import dagger.hilt.android.AndroidEntryPoint


/**
 * This class is the Activity in which we ask User to
 * add some additional information about himself in order
 * to become student: age, dailyStudyMinutes, skills he would like to learn
 **/
@AndroidEntryPoint
class StudentOnboarding : AppCompatActivity() {

    private lateinit var binding: ActivityStudentOnboardingBinding
    private lateinit var ageFragment: AgeFragment
    private lateinit var dailyStudyFragment: DailyStudyFragment
    private lateinit var skillsFragment: SkillsFragment

    private val onboardingViewModel by viewModels<StudentOnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize fragments
        ageFragment = AgeFragment()
        dailyStudyFragment = DailyStudyFragment()
        skillsFragment = SkillsFragment()

        // Set up your ViewPager with the fragments
        val adapter = OnboardingAdapter(this)
        adapter.addFragment(ageFragment)
        adapter.addFragment(dailyStudyFragment)
        adapter.addFragment(skillsFragment)
        binding.onboardingViewPager.adapter = adapter

        // Observe createStudentResult here, so that you can handle error cases whenever they occur
        onboardingViewModel.createStudentResult.observe(this) { result ->
            if (result.isSuccess) {
                // Navigate to StudentMainActivity
                val intent = Intent(this, StudentMainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Handle error case
                // For example, show a Toast with the error message
                Toast.makeText(this, result.exceptionOrNull()?.message, Toast.LENGTH_LONG).show()
            }
        }

        // Handling the next icon click
        binding.nextButton.setOnClickListener {
            when (binding.onboardingViewPager.currentItem) {
                0 -> ageFragment.onNextClicked()
                1 -> dailyStudyFragment.onNextClicked()
                2 -> {
                    skillsFragment.onNextClicked()
                    onboardingViewModel.createStudentAndAddToFirestore()
                }
            }
            binding.onboardingViewPager.currentItem = binding.onboardingViewPager.currentItem + 1
        }
    }
}

/**
 * Adapter for the Onboarding process, which allow to display
 * the onboarding as the separate parts
 **/
class OnboardingAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private val fragments = ArrayList<Fragment>()
    fun addFragment(fragment: Fragment) { fragments.add(fragment) }
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}