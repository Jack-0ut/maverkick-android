package com.example.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.data.sharedpref.SharedPrefManager
import com.example.profile.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The base class for the Profile Activities, which abstract the
 * basic features
 **/
@AndroidEntryPoint
abstract class BaseProfileFragment : Fragment() {
    @Inject
    lateinit var sharedPrefManager: SharedPrefManager
    @Inject lateinit var auth: FirebaseAuth

    private val viewModel: ProfileViewModel by viewModels()

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
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()


        // Observe the data from ViewModel
        viewModel.username.observe(viewLifecycleOwner) { username ->
            binding.username.text = username
        }

        /*viewModel.userImageURL.observe(viewLifecycleOwner) { userImageUrl ->
            // Load the image from the URL into your ImageView
            // If you're using Glide:
            Glide.with(this).load(userImageUrl).into(binding.profilePicture)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun getFragments(): List<Fragment>

    protected abstract fun getTabTitle(position: Int): String

    protected abstract fun onChangeAccountClicked()

    private fun setupViewPager() {
        val profilePageAdapter = ProfilePageAdapter(this, getFragments())
        binding.viewPager.adapter = profilePageAdapter
    }

    private fun handleLogout() {
        auth.signOut()
        sharedPrefManager.clearPreferences()
        Toast.makeText(context, "You have been signed out.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://auth/login"))
        startActivity(intent)
    }
}
