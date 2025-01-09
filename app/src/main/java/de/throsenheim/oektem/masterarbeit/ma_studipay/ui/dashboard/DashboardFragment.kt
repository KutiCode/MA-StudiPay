package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
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

        // Benutzernamen aus SharedPreferences laden
        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val currentUsername = sharedPref.getString("current_username", null)

        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByUsername(currentUsername)

                if (user != null) {
                    // Begrüßungstext mit Vorname aktualisieren
                    binding.welcomeText.text = "Hallo, ${user.firstName}"
                }
            }
        } else {
            // Fallback, falls kein Benutzer eingeloggt ist
            binding.welcomeText.text = "Hallo, Benutzer"
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Nichts passiert, wenn der Benutzer die Zurück-Taste drückt
                }
            })


        val orangeCard = view.findViewById<View>(R.id.balance_card)
        orangeCard.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_top)       // Slide-In von oben
                .setExitAnim(R.anim.slide_out_bottom)   // Slide-Out nach unten
                .setPopEnterAnim(R.anim.slide_in_bottom) // Rückweg: Slide-In von unten
                .setPopExitAnim(R.anim.slide_out_top)   // Rückweg: Slide-Out nach oben
                .build()

            findNavController().navigate(
                R.id.action_dashboardFragment_to_orangeDetailsFragment,
                null,
                navOptions
            )
        }


        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        // Navigation für die BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> { // Menüpunkt für Einstellungen
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
                    // Optional: Verhindere Navigation zum Dashboard, wenn du bereits dort bist
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navController.navigate(R.id.navigation_dashboard)
                    }
                    true
                }

                else -> false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
