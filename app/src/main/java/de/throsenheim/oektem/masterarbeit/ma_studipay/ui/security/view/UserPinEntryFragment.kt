package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.viewmodel.UserPinEntryViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.viewmodel.UserPinEntryViewModelFactory



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

        val userRepository = UserRepository(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        val viewModelFactory = UserPinEntryViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserPinEntryViewModel::class.java]

        val buttons = listOf(
            R.id.pin_number_0,
            R.id.pin_number_1,
            R.id.pin_number_2,
            R.id.pin_number_3,
            R.id.pin_number_4,
            R.id.pin_number_5,
            R.id.pin_number_6,
            R.id.pin_number_7,
            R.id.pin_number_8,
            R.id.pin_number_9
        )

        for (buttonId in buttons) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                viewModel.appendDigit(button.text.toString())
            }
        }

        val clearButton = view.findViewById<MaterialButton>(R.id.pin_number_ac)
        clearButton.setOnClickListener {
            viewModel.clearPin()
        }

        val deleteButton = view.findViewById<MaterialButton>(R.id.pin_number_delete)
        deleteButton.setOnClickListener {
            viewModel.deleteLastDigit()
        }

        viewModel.pin.observe(viewLifecycleOwner, { pin ->
            pinInput.setText(pin)
        })


        // Setting up Secure Pin
        val continueButton = view.findViewById<MaterialButton>(R.id.pin_continue_button)
        continueButton.setOnClickListener {
            val securePin = pinInput.text.toString()
            val sharedPref =
                requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matrikelnummer = sharedPref.getString("current_username", null)
            val isChangePin = arguments?.getBoolean("isChangePin") ?: false
            val storedPin = sharedPref.getString("secure_pin", null)
            if (matrikelnummer != null) {
                if (isChangePin) {
                    viewModel.updateSecurePin(requireContext(), matrikelnummer, securePin)
                    Log.d(
                        "UserPinEntryFragment",
                        "Matrikelnummer: $matrikelnummer, Neue Secure Pin: $storedPin"

                    )
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                } else {
                    viewModel.verifySecurePin(
                        requireContext(),
                        navController,
                        matrikelnummer,
                        securePin
                    )
                    Log.d(
                        "UserPinEntryFragment",
                        "Matrikelnummer: $matrikelnummer, Secure Pin: $storedPin"

                    )
                }


            }
        }

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
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
    }
}