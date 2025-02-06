package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails.view

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
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails.viewmodel.OrangeDetailsViewModel

class OrangeDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrangeDetailsBinding
    private val viewModel: OrangeDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrangeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupSharedPreferences()
        setupButtons()
        setupBottomNavigation()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Ignore the back button
                }
            }
        )
    }

    private fun setupSharedPreferences() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentMatrikelnumber = sharedPref.getString("current_username", null)

        if (currentMatrikelnumber != null) {
            viewModel.loadUserDetails(requireContext(), currentMatrikelnumber)
        } else {
            showMissingValues()
        }

        viewModel.userDetails.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                updateUserDetails(user.balance, user.matrikelnumber, user.accountNumber)
            } else {
                showMissingValues()
            }
        })
    }

    private fun updateUserDetails(balance: Double, matrikelNumber: String, accountNumber: String) {
        binding.cardBalanceValue.text = "$balance €"
        binding.matrikelnummerValue.text = matrikelNumber
        binding.acountnumberValue.text = accountNumber
    }

    private fun showMissingValues() {
        binding.cardBalanceValue.text = "Fehlende Werte"
        binding.matrikelnummerValue.text = "Fehlende Werte"
    }

    private fun setupButtons() {
        binding.sendToBankButton.setOnClickListener {
            navigateToTransactionFragment("SEND")
        }

        binding.getFromBankButton.setOnClickListener {
            navigateToTransactionFragment("RECEIVE")
        }

        binding.balanceCardDetail.setOnClickListener {
            navigateToDashboardFragment()
        }

        binding.transactionButton.setOnClickListener {
            navigateToLastTransactionsFragment()
        }
    }

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

    private fun navigateToDashboardFragment() {
        val navOptions = createNavOptions(R.anim.fade_in, R.anim.fade_out)
        findNavController().navigate(R.id.action_orangeDetailsFragment_to_dashboardFragment, null, navOptions)
    }

    private fun navigateToLastTransactionsFragment() {
        val navOptions = createNavOptions(
            enterAnim = R.anim.slide_in_right,
            exitAnim = R.anim.slide_out_left,
            popEnterAnim = R.anim.slide_in_left,
            popExitAnim = R.anim.slide_out_right
        )
        findNavController().navigate(R.id.action_orangeDetailsFragment_to_lastTransactionsFragment, null, navOptions)
    }

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