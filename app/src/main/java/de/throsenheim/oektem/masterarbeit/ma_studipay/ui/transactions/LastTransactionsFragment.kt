package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import androidx.activity.OnBackPressedCallback

class LastTransactionsFragment : Fragment() {

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
                            .setEnterAnim(R.anim.fade_in)
                            .setExitAnim(R.anim.fade_out)
                            .setPopEnterAnim(R.anim.fade_in)
                            .setPopExitAnim(R.anim.fade_out)
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

