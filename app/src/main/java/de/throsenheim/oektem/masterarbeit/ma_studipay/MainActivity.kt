package de.throsenheim.oektem.masterarbeit.ma_studipay

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Deaktiviere die Zurück-Taste
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Keine Aktion bei Zurück-Taste
            }
        })

        // Navigation verzögern, bis der NavController initialisiert ist
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        // Post-Handler verwenden
        window.decorView.post {
            val navController = findNavController(R.id.nav_host_fragment)
            if (isLoggedIn) {
                // Benutzer ist angemeldet
                Log.d("MainActivity", "Benutzer ist angemeldet" + sharedPref.getString("username","matrikelnumber"))
                navController.navigate(R.id.navigation_dashboard)
            } else {
                // Benutzer ist nicht angemeldet
                navController.navigate(R.id.welcomeFragment)
            }
        }
    }
}
