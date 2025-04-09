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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl

import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserInfoViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.UserInfoFactory

class UserInfoFragment : Fragment() {

    private lateinit var viewModel: UserInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bankRepositoryImpl = BankRepositoryImpl(
            bankDao = AppDatabase.getDatabase(requireContext()).bankDao()

        )

        val viewModelFactory = UserInfoFactory(bankRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[UserInfoViewModel::class.java]


        val settingsFragment = view.findViewById<View>(R.id.info_card)
        settingsFragment.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            findNavController().navigate(
                R.id.action_UserInfoFragment_to_settingsFragment,
                null,
                navOptions
            )
        }


        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)
        val infoFullNameValue = view.findViewById<TextView>(R.id.infoFullNameValue)
        val infoMatriculationNumberValue = view.findViewById<TextView>(R.id.infoMatrikelnummerValue)
        val infoAccountNumberValue = view.findViewById<TextView>(R.id.infoAccountNumberValue)
        val infoBankConnection = view.findViewById<TextView>(R.id.conneected_bank_value)
        viewModel.bank.observe(viewLifecycleOwner) { bank ->
            infoBankConnection.text = bank?.name ?: "Keine Bankverbindung"
        }

        currentUsername?.let {
            viewModel.fetchUser(requireContext(), it)
        } ?: run {
            infoFullNameValue.text = "Fehlende Werte"
            infoMatriculationNumberValue.text = "Fehlende Werte"
            infoAccountNumberValue.text = "Fehlende Werte"
            infoBankConnection.text = "Keine Bankverbindung"

        }

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
        val navController = findNavController()
        val changePinButton = view.findViewById<MaterialButton>(R.id.change_secure_pin_button)

        changePinButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()

            navController.navigate(
                R.id.action_UserInfoFragment_to_userPinEntryFragment,
                Bundle().apply {
                    putBoolean("isChangePin", true)
                    Log.d("UserInfoFragment", "Navigating to UserPinEntryFragment with Change Pin")
                },
                navOptions
            )
        }


        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)


        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboardFragment -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    navController.navigate(R.id.dashboardFragment, null, navOptions)
                    true
                }
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.dashboardFragment, null, navOptions)
                    }
                    true
                }
                else -> false
            }
        }
    }
}