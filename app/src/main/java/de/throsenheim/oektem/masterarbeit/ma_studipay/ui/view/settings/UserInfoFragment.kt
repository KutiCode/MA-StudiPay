package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserInfoViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.UserInfoFactory

// Fragment that displays the user's information and bank connection details.
// Allows navigation to settings and secure PIN update views.
class UserInfoFragment : Fragment() {

    // ViewModel responsible for handling user information and bank data.
    private lateinit var viewModel: UserInfoViewModel

    // Inflate the fragment layout.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_info, container, false)
    }

    // Called immediately after the view is created; setup UI elements, observers, and navigation.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate BankRepositoryImpl using the bank DAO from the app database.
        val bankRepositoryImpl = BankRepositoryImpl(
            bankDao = AppDatabase.getDatabase(requireContext()).bankDao()
        )

        // Create the ViewModel using a custom factory (UserInfoFactory) that provides the bank repository.
        val viewModelFactory = UserInfoFactory(bankRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserInfoViewModel::class.java]

        // Setup click listener on a view (info_card) to navigate to the SettingsFragment.
        val settingsFragment = view.findViewById<View>(R.id.info_card)
        settingsFragment.setOnClickListener {
            // Build navigation options with fade animations using the NavigationHelper.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Use the NavController to navigate from UserInfoFragment to SettingsFragment.
            findNavController().navigate(
                R.id.action_UserInfoFragment_to_settingsFragment,
                null,
                navOptions
            )
        }

        // Retrieve the shared preferences to get current user data (specifically, the username).
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)

        // Get references to TextViews that display various pieces of user info.
        val infoFullNameValue = view.findViewById<TextView>(R.id.infoFullNameValue)
        val infoMatriculationNumberValue = view.findViewById<TextView>(R.id.infoMatrikelnummerValue)
        val infoAccountNumberValue = view.findViewById<TextView>(R.id.infoAccountNumberValue)
        val infoBankConnection = view.findViewById<TextView>(R.id.conneected_bank_value)

        // Observe the bank LiveData from the ViewModel; update bank connection TextView accordingly.
        viewModel.bank.observe(viewLifecycleOwner) { bank ->
            infoBankConnection.text = bank?.name ?: "Keine Bankverbindung"
        }

        // If the current username is present, fetch user details via the ViewModel.
        currentUsername?.let {
            viewModel.fetchUser(requireContext(), it)
        } ?: run {
            // If no username is found, show default text indicating missing values.
            infoFullNameValue.text = "Fehlende Werte"
            infoMatriculationNumberValue.text = "Fehlende Werte"
            infoAccountNumberValue.text = "Fehlende Werte"
            infoBankConnection.text = "Keine Bankverbindung"
        }

        // Observe the user LiveData from the ViewModel; update UI fields with user details.
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                infoFullNameValue.text = "${user.firstName} ${user.lastName}"
                infoMatriculationNumberValue.text = user.matriculationNumber
                infoAccountNumberValue.text = user.accountNumber
            } else {
                infoFullNameValue.text = "Fehlende Werte"
                infoMatriculationNumberValue.text = "Fehlende Werte"
                infoAccountNumberValue.text = "Fehlende Werte"
            }
        }

        // Get the NavController to handle navigation actions.
        val navController = findNavController()

        // Setup the change PIN button and its click listener.
        val changePinButton = view.findViewById<MaterialButton>(R.id.change_secure_pin_button)
        changePinButton.setOnClickListener {
            // Build slide animation navigation options using NavigationHelper.
            val navOptions = NavigationHelper.buildSlideNavOptions()
            // Create a Bundle with a flag indicating this is a PIN change operation.
            val args = Bundle().apply {
                putBoolean("isChangePin", true)
                Log.d("UserInfoFragment", "Navigating to UserPinEntryFragment with Change Pin")
            }
            // Navigate to the UserPinEntryFragment with the provided bundle and navigation options.
            navController.navigate(
                R.id.action_UserInfoFragment_to_userPinEntryFragment,
                args,
                navOptions
            )
        }

        // Get the BottomNavigationView from the layout.
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // Setup the bottom navigation using a helper function to ensure consistent navigation behavior.
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }
}
