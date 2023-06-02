package com.example.student.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.data.sharedpref.SharedPrefManager
import com.example.student.databinding.FragmentProfileBinding
import com.example.student.fragments.profile_fragments.ProfileCoursesFragment
import com.example.student.fragments.profile_fragments.ProfileSettingsFragment
import com.example.student.viewmodels.ProfileViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment for the Profile Menu Item (Student)
 * It displays the username,picture and change_account icon
 * And consists of two sub-fragments:Courses and Settings
 **/
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    @Inject lateinit var sharedPrefManager: SharedPrefManager
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Subscribe the UI to the ViewModel
        setupViewModel()

        // Set up ViewPager2 with the profile page adapter
        setupViewPager()

        // Handle click events on the change_account ImageView
        binding.changeAccount.setOnClickListener {
            // Implement your action here. For instance, you could navigate to another screen,
            // display a dialog, etc. This is a placeholder toast message for this example.
            Toast.makeText(context, "Change account clicked", Toast.LENGTH_SHORT).show()
        }

        // Initialize the TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Courses" else "Settings"
        }.attach()
    }

    // Set up the ViewPager2
    private fun setupViewPager() {
        val profilePageAdapter = ProfilePageAdapter(this)
        binding.viewPager.adapter = profilePageAdapter
    }

    // Subscribe the UI to the ViewModel
    private fun setupViewModel() {
        /*viewModel.run {
            username.observe(viewLifecycleOwner) {
                binding.username.text = it
            }
            // Set the profile image
            viewModel.userImageURL.observe(viewLifecycleOwner) { imageUrl ->
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.profilePicture) // your ImageView here
            }
        }*/
        binding.username.text = sharedPrefManager.getUsername()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Adapter for the menu on the top of the Profile Screen,
 * where we separate the functional into Settings and Courses
 **/
class ProfilePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileCoursesFragment()
            1 -> ProfileSettingsFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}