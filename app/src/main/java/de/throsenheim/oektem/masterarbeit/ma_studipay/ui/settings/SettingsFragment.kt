package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.*
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> { // Menüpunkt für Einstellungen
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()

                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    true
                }

                R.id.navigation_home -> {
                    // Optional: Verhindere Navigation zum Dashboard, wenn du bereits dort bist
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.navigation_dashboard, null,navOptions)
                    }
                    true
                }

                else -> false
            }
        }
        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val currentUsername = sharedPref.getString("current_username", null)
        val userNameTextView = view.findViewById<TextView>(R.id.user_name)
        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByUsername(currentUsername)

                if (user != null) {
                    // Begrüßungstext mit Vorname aktualisieren
                    userNameTextView.text = "${user.firstName} ${user.lastName}"
                }
            }
        } else {
            // Fallback, falls kein Benutzer eingeloggt ist
            userNameTextView.text = "Hallo, Benutzer"
        }
        // Abmelden-Bereich finden
        val logoutSection = view.findViewById<LinearLayout>(R.id.logout_section)
        logoutSection.setOnClickListener {
            logoutUser()
        }
        val userInfoSection = view.findViewById<LinearLayout>(R.id.user_info_section)
        userInfoSection.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            navController.navigate(R.id.action_settingsFragment_to_UserInfoFragment, null, navOptions)
        }
    }

    private fun logoutUser() {
        // Lösche die gespeicherten Benutzerdaten aus SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Alle Daten löschen
            apply()
        }

        // Navigiere zur Welcome-Seite
        findNavController().navigate(R.id.action_settingsFragment_to_welcomeFragment)
    }

}