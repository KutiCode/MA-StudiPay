package de.throsenheim.oektem.masterarbeit.ma_studipay

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * MainActivity ist der Einstiegspunkt der Anwendung.
 *
 * Diese Activity initialisiert die Datenbank-Repositories, setzt das Layout und
 * steuert die Navigation basierend auf dem Anmeldestatus des Benutzers. Zudem werden
 * Daten synchronisiert, wenn die Activity gestartet oder wieder in den Vordergrund tritt.
 */

class MainActivity : AppCompatActivity() {

    /** Repository für benutzerbezogene Operationen. */
    private lateinit var userRepository: UserRepository

    /** Repository für bankbezogene Operationen. */
    private lateinit var bankRepository: BankRepository

    /**
     * Wird aufgerufen, wenn die Activity erstellt wird.
     *
     * Hier wird zunächst das Layout gesetzt, dann werden die Repositories
     * mithilfe der zentralisierten Datenbankinstanz initialisiert. Außerdem wird
     * die Navigation basierend auf dem Login-Status des Benutzers gesteuert und
     * die Synchronisation der Daten angestoßen.
     *
     * @param savedInstanceState Falls vorhanden, enthält dieses Bundle die zuletzt
     *                           gespeicherten Zustandsinformationen.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Zentrale Initialisierung der Datenbank und Repositories
        val database = AppDatabase.getDatabase(this)
        bankRepository = BankRepository(bankDao = database.bankDao())
        userRepository = UserRepository(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = this
        )

        // Zurück-Taste wird deaktiviert um unerwünschte Navigationen zu vermeiden
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Keine Aktion bei Betätigung der Zurück-Taste
            }
        })

        // Navigation basierend auf dem Anmeldestatus steuern
        navigateBasedOnLoginStatus()

        // Starte asynchrone Daten-Synchronisation
        syncData()
    }

    /**
     * Wird aufgerufen, wenn die Activity wieder in den Vordergrund tritt.
     *
     * Hier wird erneut die Daten-Synchronisation angestoßen, um stets aktuelle
     * Informationen anzuzeigen.
     */
    override fun onResume() {
        super.onResume()
        syncData()
    }

    /**
     * Führt die Synchronisation der Bank- und Benutzerdaten asynchron im Hintergrund aus.
     */
    private fun syncData() {
        lifecycleScope.launch(Dispatchers.IO) {
            bankRepository.syncBanksFromBackend()
            userRepository.syncDatabase()
            Log.d("MainActivity", "Datenbank synchronisiert")
            Log.d("MainActivity", "Bank-Daten synchronisiert")
        }
    }

    /**
     * Steuert die Navigation basierend auf dem Anmeldestatus des Benutzers.
     *
     * Liest den Login-Status aus den SharedPreferences aus und navigiert dann zum
     * Dashboard, wenn der Benutzer angemeldet ist, oder zum Welcome-Fragment andernfalls.
     */
    private fun navigateBasedOnLoginStatus() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        // Verzögerte Navigation, bis der NavController initialisiert ist
        window.decorView.post {
            val navController = findNavController(R.id.nav_host_fragment)
            if (isLoggedIn) {
                Log.d("MainActivity", "Benutzer ist angemeldet: ${sharedPref.getString("username", "matrikelnumber")}")
                navController.navigate(R.id.navigation_dashboard)
            } else {
                navController.navigate(R.id.welcomeFragment)
            }
        }
    }
}
