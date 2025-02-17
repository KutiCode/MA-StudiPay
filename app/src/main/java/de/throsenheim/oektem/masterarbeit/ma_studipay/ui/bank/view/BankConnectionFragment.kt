package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel.BankConnectionViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel.BankConnectionViewModelFactory
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.launch

class BankConnectionFragment : Fragment() {

    private val viewModel: BankConnectionViewModel by viewModels {
        BankConnectionViewModelFactory(requireContext())
    }
    private lateinit var bankRepository: BankRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bankRepository = BankRepository(AppDatabase.getDatabase(requireContext()).bankDao())
        return inflater.inflate(R.layout.fragment_bank_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsFragment = view.findViewById<View>(R.id.bank_info_card)
        settingsFragment.setOnClickListener {
            viewModel.onSettingsClicked()
        }


        val bankInfoTextView = view.findViewById<TextView>(R.id.bank_connection_label)
        viewModel.currentUserBank.observe(viewLifecycleOwner, Observer { bankName ->
            bankInfoTextView.text = bankName ?: "Keine Bankverbindung gefunden"
        })
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadCurrentUserBank()
        }
        viewModel.navigateToSettings.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()
                findNavController().navigate(
                    R.id.action_BankConnectionFragment_to_settingsFragment,
                    null,
                    navOptions
                )
                viewModel.onNavigatedToSettings()
            }
        })

        val changeBankConnectionButton = view.findViewById<View>(R.id.change_bank_connection_button)
        changeBankConnectionButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.onBankConnectionClicked()
                viewModel.loadCurrentUserBank() // Ensure the bank is reloaded after changing the connection
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()
                findNavController().navigate(
                    R.id.action_BankConnectionFragment_to_BankSelectionFragment,
                    null,
                    navOptions
                )
            }
        }

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
    }
}