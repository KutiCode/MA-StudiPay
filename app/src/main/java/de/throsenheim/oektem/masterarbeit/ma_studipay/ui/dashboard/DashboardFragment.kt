package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserDetails()
        setupButtons()
        setupBottomNavigation()
    }

    private fun setupUserDetails() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)

        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByMatrikelnumber(currentUsername)

                if (user != null) {
                    updateUserInfo(user.firstName, user.balance, user.matrikelnumber)
                } else {
                    showDefaultWelcomeMessage()
                }
            }
        } else {
            showDefaultWelcomeMessage()
        }
    }

    private fun updateUserInfo(firstName: String, balance: Double, matrikelNumber: String) {
        binding.welcomeText.text = "Hallo, $firstName"
        binding.cardDashboardBalance.text = "Dein Guthaben:"
        binding.balanceText.text = "$balance €"
        binding.matrikelnummerText.text = "Matrikelnummer: $matrikelNumber"
    }

    private fun showDefaultWelcomeMessage() {
        binding.welcomeText.text = "Hallo, Benutzer"
    }

    private fun setupButtons() {
        binding.sendButton.setOnClickListener {
            navigateWithNavOptions(R.id.action_dashboardFragment_to_userPinEntryFragment)
        }

        binding.receiveButton.setOnClickListener {
            navigateToTransactionFragment("RECEIVE")
        }

        binding.balanceCard.setOnClickListener {
            navigateWithNavOptions(R.id.action_dashboardFragment_to_orangeDetailsFragment)
        }
    }

    private fun navigateToTransactionFragment(transactionType: String) {
        val bundle = Bundle().apply {
            putString("TRANSACTION_TYPE", transactionType)
        }
        val navOptions = createNavOptions()
        findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigation
        val navController = findNavController()
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    navigateWithNavOptions(R.id.navigation_settings)
                    true
                }
                R.id.navigation_dashboard -> {
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
        val navOptions = createNavOptions()
        findNavController().navigate(destinationId, null, navOptions)
    }

    private fun createNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
