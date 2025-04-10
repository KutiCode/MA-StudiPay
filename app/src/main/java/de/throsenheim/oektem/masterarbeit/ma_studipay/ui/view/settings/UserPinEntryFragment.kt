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



class UserPinEntryFragment : Fragment() {

    private lateinit var pinInput: EditText
    private lateinit var viewModel: UserPinEntryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pin_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        pinInput = view.findViewById(R.id.pin_entry)

        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        val viewModelFactory = UserPinEntryFactory(userRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserPinEntryViewModel::class.java]


        viewModel.pin.observe(viewLifecycleOwner, { pin ->
            pinInput.setText(pin)
        })


        // Setting up Secure Pin
        val changeText = view.findViewById<TextView>(R.id.pin_label)
        val continueButton = view.findViewById<MaterialButton>(R.id.pin_continue_button)
        continueButton.setOnClickListener {
            val securePin = pinInput.text.toString()
            val sharedPref =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matriculationNumber = sharedPref.getString("current_username", null)
            val isChangePin = arguments?.getBoolean("isChangePin") ?: false
            val storedPin = sharedPref.getString("secure_pin", null)
            if (matriculationNumber != null) {
                if (isChangePin) {
                    changeText.text = "Neue Secure Pin eingeben"
                    viewModel.updateSecurePin(requireContext(), matriculationNumber, securePin)
                    Log.d(
                        "UserPinEntryFragment",
                        "Matrikelnummer: $matriculationNumber, Neue Secure Pin: $storedPin"

                    )
                    val navOptions = NavigationHelper.buildSlideNavOptions()
                    navController.navigate(R.id.dashboardFragment, null, navOptions)
                } else {
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

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }
}