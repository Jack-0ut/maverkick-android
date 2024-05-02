package com.maverkick.auth.onboarding.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.maverkick.auth.databinding.ActivityOnboardingBinding
import com.maverkick.auth.onboarding.OnboardingAdapter
import com.maverkick.auth.onboarding.student.fragments.AgeFragment
import com.maverkick.auth.onboarding.student.fragments.DailyStudyFragment
import com.maverkick.auth.onboarding.student.fragments.InterestsFragment
import com.maverkick.data.models.Student
import com.maverkick.data.sharedpref.SharedPrefManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * This class is the Activity in which we ask User to
 * add some additional information about himself in order
 * to become student: age, dailyStudyMinutes, skills he would like to learn
 **/
@AndroidEntryPoint
class StudentOnboarding : AppCompatActivity() {

    private val binding by lazy { ActivityOnboardingBinding.inflate(layoutInflater) }
    private val ageFragment by lazy { AgeFragment() }
    private val dailyStudyFragment by lazy { DailyStudyFragment() }
    private val interestsFragment by lazy { InterestsFragment() }
    private val onboardingViewModel by viewModels<StudentOnboardingViewModel>()

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            handlePageChange(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.onboardingViewPager.adapter = OnboardingAdapter(this).apply {
            addFragment(ageFragment)
            addFragment(dailyStudyFragment)
            addFragment(interestsFragment)
        }

        setupObservers()
        setupListeners()

        binding.onboardingViewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun setupObservers() {
        onboardingViewModel.createStudentResult.observe(this) { handleStudentCreationResult(it) }
    }

    private fun handlePageChange(position: Int) {
        when (position) {
            0 -> {
                if (ageFragment.isAdded && ageFragment.isVisible) {
                    ageFragment.onNextClicked()
                }
            }
            1 -> {
                if (dailyStudyFragment.isAdded && dailyStudyFragment.isVisible) {
                    dailyStudyFragment.onNextClicked()
                }
            }
            2 -> {
                if (interestsFragment.isAdded && interestsFragment.isVisible) {
                    interestsFragment.onNextClicked()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            val currentItem = binding.onboardingViewPager.currentItem
            if (currentItem == 2) {
                onboardingViewModel.createStudentAndAddToFirestore()
            } else {
                val nextItem = (currentItem + 1).coerceAtMost(2)
                binding.onboardingViewPager.currentItem = nextItem
            }
        }
    }

    private fun handleStudentCreationResult(result: Result<Student>) {
        if (result.isSuccess) {
            sharedPrefManager.setIsOnboarded(true)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("maverkick://student/main"))
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(binding.root, result.exceptionOrNull()?.message ?: "An error occurred", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.onboardingViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
    }
}
