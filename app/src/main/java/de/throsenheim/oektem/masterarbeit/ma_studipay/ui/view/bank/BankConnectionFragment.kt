package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankConnectionViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.BankConnectionFactory
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import kotlinx.coroutines.launch

class BankConnectionFragment : Fragment() {

    private val viewModel: BankConnectionViewModel by viewModels {
        BankConnectionFactory(requireContext())
    }
    private lateinit var bankRepositoryImpl: BankRepositoryImpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bankRepositoryImpl = BankRepositoryImpl(AppDatabase.getDatabase(requireContext()).bankDao())
        return inflater.inflate(R.layout.fragment_bank_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsFragment = view.findViewById<View>(R.id.bank_info_card)
        settingsFragment.setOnClickListener {
            viewModel.onSettingsClicked()
        }


        val bankInfoTextView = view.findViewById<TextView>(R.id.bank_connection_label)
        viewModel.currentUserBank.observe(viewLifecycleOwner) { bankName ->
            bankInfoTextView.text = bankName ?: "Keine Bankverbindung gefunden"
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadCurrentUserBank()
        }
        viewModel.navigateToSettings.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                val navOptions = NavigationHelper.buildFadeNavOptions()
                findNavController().navigate(
                    R.id.action_BankConnectionFragment_to_settingsFragment,
                    null,
                    navOptions
                )
                viewModel.onNavigatedToSettings()
            }
        }

        val changeBankConnectionButton = view.findViewById<View>(R.id.change_bank_connection_button)
        changeBankConnectionButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.onBankConnectionClicked()
                viewModel.loadCurrentUserBank() // Ensure the bank is reloaded after changing the connection
                val navOptions = NavigationHelper.buildFadeNavOptions()
                findNavController().navigate(
                    R.id.action_BankConnectionFragment_to_BankSelectionFragment,
                    null,
                    navOptions
                )
            }
        }

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)

    }
}