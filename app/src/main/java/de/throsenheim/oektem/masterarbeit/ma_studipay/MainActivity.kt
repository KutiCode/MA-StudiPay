package de.throsenheim.oektem.masterarbeit.ma_studipay

/**
 * Required imports
 */
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MainActivity is the entry point of the application.
 *
 * This activity initializes the database repositories, sets the layout, and manages navigation
 * based on the user's login status. Additionally, it synchronizes data when the activity is started
 * or resumed.
 */
class MainActivity : AppCompatActivity() {

    /** Repository for user-related operations. */
    private lateinit var userRepositoryImpl: UserRepositoryImpl

    /** Repository for bank-related operations. */
    private lateinit var bankRepository: BankRepository

    /**
     * Called when the activity is created.
     *
     * This method sets the layout, initializes the repositories using the centralized database instance,
     * manages navigation based on the user's login status, and starts data synchronization.
     *
     * @param savedInstanceState If available, contains the last saved state information.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Central initialization of the database and repositories
        val database = AppDatabase.getDatabase(this)
        bankRepository = BankRepository(bankDao = database.bankDao())
        userRepositoryImpl = UserRepositoryImpl(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = this
        )

        // Disable the back button to avoid unwanted navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing when the back button is pressed
            }
        })

        // Manage navigation based on the user's login status
        navigateBasedOnLoginStatus()

        // Start asynchronous data synchronization
        syncData()
    }

    /**
     * Called when the activity is resumed.
     *
     * Triggers data synchronization to always display up-to-date information.
     */
    override fun onResume() {
        super.onResume()
        syncData()
    }

    /**
     * Performs asynchronous synchronization of bank and user data in the background.
     */
    private fun syncData() {
        lifecycleScope.launch(Dispatchers.IO) {
            bankRepository.syncBanksFromBackend()
            userRepositoryImpl.syncDatabase()
            Log.d("MainActivity", "Database synchronized")
            Log.d("MainActivity", "Bank data synchronized")
        }
    }

    /**
     * Manages navigation based on the user's login status.
     *
     * Reads the login status from SharedPreferences and navigates to the dashboard if the user is logged in,
     * or to the welcome fragment otherwise.
     */
    private fun navigateBasedOnLoginStatus() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        // Delayed navigation until the NavController is initialized
        window.decorView.post {
            val navController = findNavController(R.id.nav_host_fragment)
            if (isLoggedIn) {
                Log.d(
                    "MainActivity",
                    "User is logged in: ${sharedPref.getString("username", "matrikelnumber")}"
                )
                navController.navigate(R.id.navigation_dashboard)
            } else {
                navController.navigate(R.id.welcomeFragment)
            }
        }
    }
}
