// MainActivity.kt
package de.throsenheim.oektem.masterarbeit.ma_studipay

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import androidx.work.*
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.worker.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupWorkManager()
        syncUserDatabase()
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

    private fun setupWorkManager() {
        // Erstelle eine PeriodicWorkRequest
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Nur bei aktiver Verbindung
                    .build()
            )
            .build()

        // WorkManager-Aufgabe einplanen
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "SyncWorker",
            ExistingPeriodicWorkPolicy.KEEP, // Verhindert das erneute Planen, wenn bereits aktiv
            syncWorkRequest
        )
        Log.d("MainActivity", "WorkManager setup complete")
    }

    override fun onResume() {
        super.onResume()
        syncUserDatabase()
    }

    private fun syncUserDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepository.syncDatabase()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}