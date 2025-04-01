package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.view

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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
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

        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        val viewModelFactory = UserPinEntryViewModelFactory(userRepositoryImpl)
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
            val matrikelnummer = sharedPref.getString("current_username", null)
            val isChangePin = arguments?.getBoolean("isChangePin") ?: false
            val storedPin = sharedPref.getString("secure_pin", null)
            if (matrikelnummer != null) {
                if (isChangePin) {
                    changeText.text = "Neue Secure Pin eingeben"
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