package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import android.transition.ChangeBounds
import android.transition.TransitionSet
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.launch

class OrangeDetailsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_orange_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Nichts passiert, wenn der Benutzer die Zurück-Taste drückt
                }
            })



















        // Referenz zur BottomNavigationView
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Verknüpfe die BottomNavigationView mit dem NavController
        val navController = findNavController()
        bottomNavigationView.setupWithNavController(navController)

        // Optionale manuelle Navigation (falls nötig)
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

                R.id.navigation_home -> {
                    // Prüfen, ob die aktuelle Seite nicht bereits das Dashboard ist
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_top)      // Dashboard kommt von oben herein
                            .setExitAnim(R.anim.slide_out_bottom)  // Aktuelle Seite verschwindet nach unten
                            .setPopEnterAnim(R.anim.slide_in_bottom) // Rückweg: Dashboard kommt von unten herein
                            .setPopExitAnim(R.anim.slide_out_top)  // Rückweg: Aktuelle Seite verschwindet nach oben
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

