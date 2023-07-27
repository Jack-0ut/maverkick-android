package com.maverkick.auth.onboarding.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.maverkick.auth.databinding.ActivityOnboardingBinding
import com.maverkick.auth.onboarding.OnboardingAdapter
import com.maverkick.auth.onboarding.student.fragments.AgeFragment
import com.maverkick.auth.onboarding.student.fragments.DailyStudyFragment
import com.maverkick.auth.onboarding.student.fragments.InterestsFragment
import dagger.hilt.android.AndroidEntryPoint


/**
 * This class is the Activity in which we ask User to
 * add some additional information about himself in order
 * to become student: age, dailyStudyMinutes, skills he would like to learn
 **/
@AndroidEntryPoint
class StudentOnboarding : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var ageFragment: AgeFragment
    private lateinit var dailyStudyFragment: DailyStudyFragment
    private lateinit var interestsFragment: InterestsFragment

    private val onboardingViewModel by viewModels<StudentOnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize fragments
        ageFragment = AgeFragment()
        dailyStudyFragment = DailyStudyFragment()
        interestsFragment = InterestsFragment()

        // Set up your ViewPager with the fragments
        val adapter = OnboardingAdapter(this)
        adapter.addFragment(ageFragment)
        adapter.addFragment(dailyStudyFragment)
        adapter.addFragment(interestsFragment)
        binding.onboardingViewPager.adapter = adapter

        // Observe createStudentResult here, so that you can handle error cases whenever they occur
        onboardingViewModel.createStudentResult.observe(this) { result ->
            if (result.isSuccess) {
                // Navigate to StudentMainActivity
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/main"))
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
                    interestsFragment.onNextClicked()
                    onboardingViewModel.createStudentAndAddToFirestore()
                }
            }
            binding.onboardingViewPager.currentItem = binding.onboardingViewPager.currentItem + 1
        }
    }
}
