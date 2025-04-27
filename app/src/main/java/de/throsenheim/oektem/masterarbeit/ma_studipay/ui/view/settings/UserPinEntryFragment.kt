package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserPinEntryViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.UserPinEntryFactory

// Fragment allowing the user to either update or verify their secure PIN.
class UserPinEntryFragment : Fragment() {

    // EditText to capture the user-entered PIN.
    private lateinit var pinInput: EditText

    // ViewModel instance managing PIN-related operations.
    private lateinit var viewModel: UserPinEntryViewModel

    // Inflates the layout for the fragment.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pin_entry, container, false)
    }

    // Called after the view is created; initializes UI components, ViewModel, and click listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Obtain the NavController for navigation.
        val navController = findNavController()
        // Initialize the PIN input EditText.
        pinInput = view.findViewById(R.id.pin_entry)

        // Create the UserRepositoryImpl instance by passing necessary dependencies.
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        // Set up the ViewModel using a custom factory.
        val viewModelFactory = UserPinEntryFactory(userRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserPinEntryViewModel::class.java]

        // Observe the PIN LiveData from the ViewModel and update the input field accordingly.
        viewModel.pin.observe(viewLifecycleOwner, { pin ->
            pinInput.setText(pin)
        })

        // Retrieve the TextView for displaying a PIN change prompt.
        val changeText = view.findViewById<TextView>(R.id.pin_label)
        // Retrieve the continue button used for PIN verification/update.
        val continueButton = view.findViewById<MaterialButton>(R.id.pin_continue_button)
        continueButton.setOnClickListener {
            // Capture the PIN entered by the user.
            val securePin = pinInput.text.toString()
            // Access the app's shared preferences to obtain the current user's details.
            val sharedPref =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            // Retrieve the current username (matriculation number) and stored PIN (if any).
            val matriculationNumber = sharedPref.getString("current_username", null)
            val isChangePin = arguments?.getBoolean("isChangePin")
            val storedPin = sharedPref.getString("secure_pin", null)
            if (matriculationNumber != null) {
                if (isChangePin == true) {
                    // If in change PIN mode, update the prompt text and update the new PIN.
                    changeText.text = "Neue Secure Pin eingeben"
                    viewModel.updateSecurePin(requireContext(), matriculationNumber, securePin)
                    Log.d(
                        "UserPinEntryFragment",
                        "Matrikelnummer: $matriculationNumber, Neue Secure Pin: $securePin"
                    )
                    // Build slide animations for navigation.
                    val navOptions = NavigationHelper.buildSlideNavOptions()
                    // Navigate to the dashboard after PIN update.
                    navController.navigate(R.id.dashboardFragment, null, navOptions)
                } else {
                    // If verifying PIN, update the prompt text accordingly.
                    changeText.text = "Bist du's wirklich?"
                    viewModel.verifySecurePin(
                        requireContext(),
                        navController,
                        matriculationNumber,
                        securePin
                    )
                    Log.d(
                        "UserPinEntryFragment",
                        "Matrikelnummer: $matriculationNumber, Secure Pin: $storedPin"
                    )
                }
            }
        }

        // Set up the bottom navigation for the fragment.
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }
}
