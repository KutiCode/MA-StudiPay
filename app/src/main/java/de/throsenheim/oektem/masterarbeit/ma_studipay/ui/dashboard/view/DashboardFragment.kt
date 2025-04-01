package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentDashboardBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token.TransactionStatusHolder
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel.DashboardViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel.DashboardViewModelFactory
import java.util.Calendar

/**
 * DashboardFragment serves as the main screen for the dashboard.
 *
 * It initializes the ViewModel, observes UI state changes, and sets up navigation listeners.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the view has been created.
     *
     * Initializes the ViewModel, sets up observers and listeners, and loads user data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TransactionStatusHolder.reset()
        initViewModel()
        setupObservers()
        setupListeners()
        loadUserData()
    }

    /**
     * Initializes the ViewModel by creating repository instances and a ViewModelFactory.
     */
    private fun initViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        val bankRepositoryImpl = BankRepositoryImpl(database.bankDao())
        val viewModelFactory = DashboardViewModelFactory(bankRepositoryImpl, userRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]
    }

    /**
     * Sets up LiveData observers to update the UI when the ViewModel's state changes.
     */
    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { uiState ->
            // Set a dynamic greeting based on the time of day.
            val greeting = getGreetingPrefix()
            binding.welcomeText.text = "$greeting, ${uiState.firstName}"
            binding.cardDashboardBalance.text = "Dein Guthaben:"
            binding.balanceText.text = uiState.balance
            binding.matrikelnummerText.text = "Matrikelnummer: ${uiState.matrikelNumber}"
        }
    }

    /**
     * Returns a greeting prefix based on the current time.
     *
     * @return The greeting prefix.
     */
    private fun getGreetingPrefix(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 5 -> "Nachteule"
            hour < 12 -> "Guten Morgen"
            hour < 17 -> "Guten Tag"
            hour < 21 -> "Guten Abend"
            else -> "Schönen Spätabend"
        }
    }

    /**
     * Sets up click listeners for UI elements to handle navigation.
     */
    private fun setupListeners() {
        // Navigation for the send button
        binding.sendButton.setOnClickListener {
            val navOptions = buildNavOptions(
                enter = R.anim.slide_in_right,
                exit = R.anim.slide_out_left,
                popEnter = R.anim.slide_in_left,
                popExit = R.anim.slide_out_right
            )
            findNavController().navigate(
                R.id.action_UserInfoFragment_to_userPinEntryFragment,
                Bundle().apply {
                    putBoolean("isChangePin", false)
                    Log.d(
                        "DashboardFragment",
                        "Navigating to UserPinEntryFragment without changing PIN"
                    )
                },
                navOptions
            )
        }

        // Navigation for the receive button
        binding.receiveButton.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TRANSACTION_TYPE", "RECEIVE")
                putString("SOURCE", "dashboard")
            }
            val navOptions = buildNavOptions(
                enter = R.anim.slide_in_right,
                exit = R.anim.slide_out_left,
                popEnter = R.anim.slide_in_left,
                popExit = R.anim.slide_out_right
            )
            findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
        }

        // Navigation for card details
        binding.balanceCard.setOnClickListener {
            navigateWithFadeAnimation(R.id.orangeDetailsFragment)
        }

        // Bottom Navigation View item selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    navigateWithSlideAnimation(R.id.navigation_settings)
                    true
                }
                R.id.navigation_home -> {
                    // Remain on the dashboard; no navigation required.
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Loads user data using the matriculation number stored in SharedPreferences.
     * If no matriculation number is found, displays a default greeting.
     */
    private fun loadUserData() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matriculationNumber = sharedPreferences.getString("current_username", null)
        if (matriculationNumber != null) {
            viewModel.loadUserData(matriculationNumber)
        } else {
            binding.welcomeText.text = "Hallo, Gast"
        }
    }

    /**
     * Constructs NavOptions with the specified animations.
     *
     * @param enter Animation resource for entering the destination.
     * @param exit Animation resource for exiting the current screen.
     * @param popEnter Animation resource for entering when navigating back.
     * @param popExit Animation resource for exiting when navigating back.
     * @return The constructed NavOptions.
     */
    private fun buildNavOptions(enter: Int, exit: Int, popEnter: Int, popExit: Int): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(enter)
            .setExitAnim(exit)
            .setPopEnterAnim(popEnter)
            .setPopExitAnim(popExit)
            .build()
    }

    /**
     * Navigates to the specified destination using slide animations.
     *
     * @param destinationId The ID of the destination.
     */
    private fun navigateWithSlideAnimation(destinationId: Int) {
        val navOptions = buildNavOptions(
            enter = R.anim.slide_in_right,
            exit = R.anim.slide_out_left,
            popEnter = R.anim.slide_in_left,
            popExit = R.anim.slide_out_right
        )
        findNavController().navigate(destinationId, null, navOptions)
    }

    /**
     * Navigates to the specified destination using fade animations.
     *
     * @param destinationId The ID of the destination.
     */
    private fun navigateWithFadeAnimation(destinationId: Int) {
        val navOptions = buildNavOptions(
            enter = R.anim.fade_in,
            exit = R.anim.fade_out,
            popEnter = R.anim.fade_in,
            popExit = R.anim.fade_out
        )
        findNavController().navigate(destinationId, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        TransactionStatusHolder.reset()
    }
}
