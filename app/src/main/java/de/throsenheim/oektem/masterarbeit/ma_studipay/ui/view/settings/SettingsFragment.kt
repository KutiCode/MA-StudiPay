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
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.SettingsViewModel

// SettingsFragment displays user settings options such as logout,
// viewing user details, and bank connection details. It also sets up bottom navigation.
class SettingsFragment : Fragment() {

    // Obtain the SettingsViewModel instance using the viewModels delegate.
    private val viewModel: SettingsViewModel by viewModels()

    // Inflate the fragment's layout defined in fragment_settings.xml.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    // Called after the view is created; setup UI components, observers, and navigation listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and setup the bottom navigation using a helper.
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)

        // Retrieve the current user's username from shared preferences.
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)
        // Get a reference to the TextView that displays the user's name.
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)

        // If a username exists, load the user's full name via the ViewModel; otherwise, use a default greeting.
        currentUsername?.let {
            viewModel.loadUserName(requireContext(), it)
        } ?: run {
            userNameTextView.text = "Hallo, Benutzer"
        }

        // Observe the userName LiveData in the ViewModel to update the TextView when data changes.
        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            userNameTextView.text = userName
        }

        // Setup logoutSection: when clicked, call the logout method in the ViewModel
        // and navigate to the welcome screen.
        val logoutSection = view.findViewById<LinearLayout>(R.id.logout_section)
        logoutSection.setOnClickListener {
            viewModel.logoutUser(requireContext())
            findNavController().navigate(R.id.action_settingsFragment_to_welcomeFragment)
        }

        // Setup userInfoSection: when clicked, navigate to the UserInfoFragment using fade animations.
        val userInfoSection = view.findViewById<LinearLayout>(R.id.user_info_section)
        userInfoSection.setOnClickListener {
            val navOptions = NavigationHelper.buildFadeNavOptions()
            navController.navigate(R.id.action_settingsFragment_to_UserInfoFragment, null, navOptions)
        }

        // Setup bankDetailsSection: when clicked, navigate to the BankConnectionFragment using fade animations.
        val bankDetailsSection = view.findViewById<LinearLayout>(R.id.bank_connection_section)
        bankDetailsSection.setOnClickListener {
            val navOptions = NavigationHelper.buildFadeNavOptions()
            navController.navigate(R.id.action_settingsFragment_to_bankConnectionFragment, null, navOptions)
        }
    }
}
