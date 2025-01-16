package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        // Benutzerdaten aus der Datenbank abrufen
        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val currentUsername = sharedPref.getString("current_username", null)

        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByMatrikelnumber(currentUsername)

                if (user != null) {
                    // Begrüßung und Guthabenanzeige
                    binding.welcomeText.text = "Hallo, ${user.firstName}"
                    binding.cardDashboardBalance.text = "Dein Guthaben:"
                    binding.balanceText.text = "${user.balance} €"
                    binding.matrikelnummerText.text = "Matrikelnummer: ${user.matrikelnumber}"
                }
            }
        } else {
            binding.welcomeText.text = "Hallo, Benutzer"
        }

        // Senden-Button-Listener
        binding.sendButton.setOnClickListener {
            navigateToTransactionFragment("SEND")
        }

        // Empfangen-Button-Listener
        binding.receiveButton.setOnClickListener {
            navigateToTransactionFragment("RECEIVE")
        }
        val orangeCard = view.findViewById<View>(R.id.balance_card)
        orangeCard.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)  // Animation beim Eintritt
                .setExitAnim(R.anim.fade_out) // Animation beim Verlassen
                .setPopEnterAnim(R.anim.fade_in) // Animation beim Zurückkehren
                .setPopExitAnim(R.anim.fade_out) // Animation beim Zurücknavigieren
                .build()

            findNavController().navigate(
                R.id.action_dashboardFragment_to_orangeDetailsFragment,
                null,
                navOptions
            )
        }
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()

                    navController.navigate(R.id.navigation_settings, null, navOptions)
                    true
                }

                R.id.navigation_dashboard -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navController.navigate(R.id.navigation_dashboard)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun navigateToTransactionFragment(transactionType: String) {
        val bundle = Bundle().apply {
            putString("TRANSACTION_TYPE", transactionType)
        }

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        findNavController().navigate(R.id.userTransactionFragment, bundle, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
