package com.example.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.profile.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

/**
 * The base class for the Profile Activities, which abstract the
 * basic features
 **/
@AndroidEntryPoint
abstract class BaseProfileFragment : Fragment() {
    protected abstract fun getViewModel(): ProfileViewModelInterface

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

        setupViewPager()

        binding.changeAccount.setOnClickListener {
            onChangeAccountClicked()
        }

        binding.logOut.setOnClickListener{
            handleLogout()
        }

        binding.editProfilePicture.setOnClickListener {
            getContent.launch("image/*")
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        // Use the getViewModel() method to get the ViewModel
        val viewModel = getViewModel()

        // Observe the username
        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.username.text = username
        }

        // Observe the profile picture
        viewModel.profilePicture.observe(viewLifecycleOwner) { profilePictureUrl ->
            // Load the image from the URL into your ImageView
            Glide.with(this).load(profilePictureUrl).into(binding.profilePicture)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** get the profile picture from gallery */
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            getViewModel().updateProfilePicture(uri)
        }
    }

    protected abstract fun getFragments(): List<Fragment>

    protected abstract fun getTabTitle(position: Int): String

    protected abstract fun onChangeAccountClicked()

    private fun setupViewPager() {
        val profilePageAdapter = ProfilePageAdapter(this, getFragments())
        binding.viewPager.adapter = profilePageAdapter
    }

    private fun handleLogout() {
        getViewModel().logout()
        getViewModel().clearPreferences()
        Toast.makeText(context, "You have been signed out.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://auth/login"))
        startActivity(intent)
    }
}
