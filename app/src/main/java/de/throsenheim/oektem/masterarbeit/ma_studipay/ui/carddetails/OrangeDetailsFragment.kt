package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails

import android.os.Bundle
import android.util.Log
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




        val detailedCard = view.findViewById<View>(R.id.balance_card_detail)
        detailedCard.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)  // Animation beim Eintritt
                .setExitAnim(R.anim.fade_out) // Animation beim Verlassen
                .setPopEnterAnim(R.anim.fade_in) // Animation beim Zurückkehren
                .setPopExitAnim(R.anim.fade_out) // Animation beim Zurücknavigieren
                .build()

            findNavController().navigate(
                R.id.action_orangeDetailsFragment_to_dashboardFragment,
                null,
                navOptions
            )
        }
        val transactionButton = view.findViewById<View>(R.id.transaction_button)
        transactionButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            Log.d("OrangeDetailsFragment", "Transaction button clicked")

            // Navigation sicherstellen
            view.post {
                try {
                    findNavController().navigate(
                        R.id.action_orangeDetailsFragment_to_lastTransactionsFragment,
                        null,
                        navOptions
                    )
                    Log.d("Navigation", "Navigated to LastTransactionsFragment.")
                } catch (e: Exception) {
                    Log.e("NavigationError", "Failed to navigate to LastTransactionsFragment.", e)
                }
            }
        }


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

