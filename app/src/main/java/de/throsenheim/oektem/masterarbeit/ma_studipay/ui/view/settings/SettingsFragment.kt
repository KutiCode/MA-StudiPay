package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.SettingsViewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    true
                }
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    }
                    true
                }
                else -> false
            }
        }

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)

        currentUsername?.let {
            viewModel.loadUserName(requireContext(), it)
        } ?: run {
            userNameTextView.text = "Hallo, Benutzer"
        }

        viewModel.userName.observe(viewLifecycleOwner, Observer { userName ->
            userNameTextView.text = userName
        })

        val logoutSection = view.findViewById<LinearLayout>(R.id.logout_section)
        logoutSection.setOnClickListener {
            viewModel.logoutUser(requireContext())
            findNavController().navigate(R.id.action_settingsFragment_to_welcomeFragment)
        }

        val userInfoSection = view.findViewById<LinearLayout>(R.id.user_info_section)
        userInfoSection.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            navController.navigate(R.id.action_settingsFragment_to_UserInfoFragment, null, navOptions)
        }

        val bankDetailsSection = view.findViewById<LinearLayout>(R.id.bank_connection_section)
        bankDetailsSection.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            navController.navigate(R.id.action_settingsFragment_to_bankConnectionFragment, null, navOptions)
        }
    }
}