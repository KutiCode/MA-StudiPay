package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.carddetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentOrangeDetailsBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.carddetails.OrangeDetailsViewModel

/**
 * OrangeDetailsFragment displays the details for the orange card.
 *
 * It handles loading user details from the database, setting up UI buttons for transactions,
 * and configuring the bottom navigation. It also handles the back button press.
 */
class OrangeDetailsFragment : Fragment() {

    // Binding for the fragment's layout.
    private lateinit var binding: FragmentOrangeDetailsBinding

    // ViewModel to manage user detail data.
    private val viewModel: OrangeDetailsViewModel by viewModels()

    /**
     * Inflates the layout for the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrangeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the view is created.
     *
     * Sets up the back press handler, loads user details from SharedPreferences,
     * sets up button click listeners, and configures bottom navigation.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSharedPreferences()
        setupButtons()
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }



    /**
     * Loads the current user's matriculation number from SharedPreferences.
     * If found, instructs the ViewModel to load user details; otherwise, shows missing values.
     * Observes the userDetails LiveData to update the UI.
     */
    private fun setupSharedPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentMatriculationNumber = sharedPref.getString("current_username", null)

        if (currentMatriculationNumber != null) {
            viewModel.loadUserDetails(requireContext())
        } else {
            showMissingValues()
        }

        // Observe changes in user details from the ViewModel.
        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUserDetails(user.balance, user.matriculationNumber, user.accountNumber)
            } else {
                showMissingValues()
            }
        }
    }

    /**
     * Updates the UI with the provided user details.
     *
     * @param balance The user's balance.
     * @param matriculationNumber The user's matriculation number.
     * @param accountNumber The user's account number.
     */
    private fun updateUserDetails(
        balance: Double,
        matriculationNumber: String,
        accountNumber: String
    ) {
        binding.cardBalanceValue.text = "$balance €"
        binding.matrikelnummerValue.text = matriculationNumber
        binding.acountnumberValue.text = accountNumber
    }

    /**
     * Displays default values in the UI if user details are missing.
     */
    private fun showMissingValues() {
        binding.cardBalanceValue.text = "Ungültige Werte"
        binding.matrikelnummerValue.text = "Ungültige Werte"
    }

    /**
     * Sets up click listeners for various buttons to navigate to different fragments.
     */
    private fun setupButtons() {
        // When the "Send to Bank" button is clicked, navigate to the transaction fragment with "SEND" type.
        binding.sendToBankButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TRANSACTION_TYPE", "SEND")
                putString("SOURCE", "orangeDetails")
            }
            val navOptions = NavigationHelper.buildSlideNavOptions()
            findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
        }

        // When the "Get from Bank" button is clicked, navigate to the transaction fragment with "RECEIVE" type.
        binding.getFromBankButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TRANSACTION_TYPE", "RECEIVE")
                putString("SOURCE", "orangeDetails")
            }
            val navOptions = NavigationHelper.buildSlideNavOptions()
            findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
        }

        // When the balance card is clicked, navigate back to the dashboard.
        binding.balanceCardDetail.setOnClickListener {
            val navOptions = NavigationHelper.buildFadeNavOptions()
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)

        }


    }



}
