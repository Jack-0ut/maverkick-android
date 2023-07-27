package com.maverkick.auth.onboarding.teacher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.maverkick.auth.databinding.ActivityOnboardingBinding
import com.maverkick.auth.onboarding.OnboardingAdapter
import com.maverkick.auth.onboarding.teacher.fragments.CountryFragment
import com.maverkick.auth.onboarding.teacher.fragments.ExpertiseFragment
import com.maverkick.auth.onboarding.teacher.fragments.FullNameFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * This class is the Activity in which we ask User to
 * add some additional information about himself in order
 * to become student: fullName and list of skills/subjects she/he 's planning to teach
 **/
@AndroidEntryPoint
class TeacherOnboarding: AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var fullNameFragment: FullNameFragment
    private lateinit var countryFragment: CountryFragment
    private lateinit var expertiseFragment: ExpertiseFragment

    private val onboardingViewModel by viewModels<TeacherOnboardingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize fragments
        fullNameFragment = FullNameFragment()
        countryFragment = CountryFragment()
        expertiseFragment = ExpertiseFragment()

        // Set up your ViewPager with the fragments
        val adapter = OnboardingAdapter(this)
        adapter.addFragment(fullNameFragment)
        adapter.addFragment(countryFragment)
        adapter.addFragment(expertiseFragment)
        binding.onboardingViewPager.adapter = adapter

        // Observe createStudentResult here, so that you can handle error cases whenever they occur
        onboardingViewModel.createTeacherResult.observe(this) { result ->
            if (result.isSuccess) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://teacher/main"))
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Can't create the teacher, try it again!", Toast.LENGTH_LONG).show()
            }
        }

        // Handling the next icon click
        binding.nextButton.setOnClickListener {
            when (binding.onboardingViewPager.currentItem) {
                0 -> fullNameFragment.onNextClicked()
                1 -> countryFragment.onNextClicked()
                2 -> {
                    expertiseFragment.onNextClicked()
                    onboardingViewModel.createTeacherAndAddToFirestore()
                }
            }
            binding.onboardingViewPager.currentItem = binding.onboardingViewPager.currentItem + 1
        }
    }
}
