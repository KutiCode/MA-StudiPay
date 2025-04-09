package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.carddetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentOrangeDetailsBinding
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

        setupBackPressHandler()
        setupSharedPreferences()
        setupButtons()
        setupBottomNavigation()
    }

    /**
     * Configures a back press callback that ignores the back button.
     */
    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Back button is ignored.
                }
            }
        )
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
            viewModel.loadUserDetails(requireContext(), currentMatriculationNumber)
        } else {
            showMissingValues()
        }

        // Observe changes in user details from the ViewModel.
        viewModel.userDetails.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUserDetails(user.balance, user.matrikelnumber, user.accountNumber)
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
            navigateToTransactionFragment("SEND")
        }

        // When the "Get from Bank" button is clicked, navigate to the transaction fragment with "RECEIVE" type.
        binding.getFromBankButton.setOnClickListener {
            navigateToTransactionFragment("RECEIVE")
        }

        // When the balance card is clicked, navigate back to the dashboard.
        binding.balanceCardDetail.setOnClickListener {
            navigateToDashboardFragment()
        }

    }

    /**
     * Navigates to the transaction fragment with a specified transaction type.
     *
     * @param transactionType The type of transaction ("SEND" or "RECEIVE").
     */
    private fun navigateToTransactionFragment(transactionType: String) {
        val bundle = Bundle().apply {
            putString("TRANSACTION_TYPE", transactionType)
            putString("SOURCE", "orangeDetails")
        }
        val navOptions = createNavOptions(
            enterAnim = R.anim.slide_in_right,
            exitAnim = R.anim.slide_out_left,
            popEnterAnim = R.anim.slide_in_left,
            popExitAnim = R.anim.slide_out_right
        )
        findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
    }

    /**
     * Navigates to the dashboard fragment using fade animations.
     */
    private fun navigateToDashboardFragment() {
        val navOptions = createNavOptions(R.anim.fade_in, R.anim.fade_out)
        findNavController().navigate(R.id.action_orangeDetailsFragment_to_dashboardFragment, null, navOptions)
    }


    /**
     * Creates navigation options with specified animation resources.
     *
     * @param enterAnim Animation resource for entering the destination.
     * @param exitAnim Animation resource for exiting the current screen.
     * @param popEnterAnim Animation resource for entering when navigating back (default: same as enterAnim).
     * @param popExitAnim Animation resource for exiting when navigating back (default: same as exitAnim).
     * @return Constructed NavOptions.
     */
    private fun createNavOptions(
        enterAnim: Int,
        exitAnim: Int,
        popEnterAnim: Int = enterAnim,
        popExitAnim: Int = exitAnim
    ): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .setPopEnterAnim(popEnterAnim)
            .setPopExitAnim(popExitAnim)
            .build()
    }

    /**
     * Configures the bottom navigation view to work with the NavController,
     * and sets up item selection to navigate to the corresponding destinations.
     */
    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigation
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    navigateWithNavOptions(R.id.navigation_settings)
                    true
                }
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navigateWithNavOptions(R.id.navigation_dashboard)
                    }
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Navigates to the specified destination using slide animations via NavOptions.
     *
     * @param destinationId The destination fragment's ID.
     */
    private fun navigateWithNavOptions(destinationId: Int) {
        val navOptions = createNavOptions(
            enterAnim = R.anim.slide_in_right,
            exitAnim = R.anim.slide_out_left,
            popEnterAnim = R.anim.slide_in_left,
            popExitAnim = R.anim.slide_out_right
        )
        findNavController().navigate(destinationId, null, navOptions)
    }
}
