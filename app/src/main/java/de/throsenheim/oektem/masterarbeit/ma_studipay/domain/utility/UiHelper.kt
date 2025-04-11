package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Helper object containing utility functions used throughout the app.
object UiHelper {

    /**
     * Loads a user from the local database by matriculation number.
     *
     * @param context The context used to access the database.
     * @param matriculationNumber The matriculation number to identify the user.
     * @return A User object if found, otherwise null.
     */
    suspend fun loadUser(context: Context, matriculationNumber: String): User? {
        // Get an instance of the user DAO from the database.
        val userDao = AppDatabase.getDatabase(context).userDao()
        // Return the user matching the matriculation number.
        return userDao.getUserByMatriculationNumber(matriculationNumber)
    }

    /**
     * Checks if the device's WiFi is enabled.
     *
     * @param context The context used to access the WiFi service.
     * @return True if WiFi is enabled, false otherwise.
     */
    fun isWifiEnabled(context: Context): Boolean {
        // Retrieve the WifiManager from the application context.
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    /**
     * Checks if the device is currently connected to a WiFi network.
     *
     * @param context The context used to retrieve connectivity services.
     * @return True if connected to WiFi, false otherwise.
     */
    fun isWifiConnected(context: Context): Boolean {
        // Retrieve the ConnectivityManager to get network information.
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Get the current active network; return false if none exists.
        val network = connectivityManager.activeNetwork ?: return false
        // Get network capabilities for the active network; return false if none.
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        // Return true if the network has WiFi transport.
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Synchronizes the backend data for banks and users, and then fetches the updated user.
     *
     * This function first attempts to sync bank and user data on the IO dispatcher.
     * If an error occurs during synchronization, it logs the error.
     * Finally, it fetches and returns the updated user data.
     *
     * @param matriculationNumber The matriculation number of the user to update.
     * @param bankRepositoryImpl The repository for bank data.
     * @param userRepositoryImpl The repository for user data.
     * @return The updated User object if found, otherwise null.
     */
    suspend fun userUpdater(
        matriculationNumber: String,
        bankRepositoryImpl: BankRepositoryImpl,
        userRepositoryImpl: UserRepositoryImpl
    ): User? {
        // Attempt to synchronize backend data using the IO dispatcher.
        try {
            withContext(Dispatchers.IO) {
                // Sync bank data from the backend.
                bankRepositoryImpl.syncBanksFromBackend()
                // Sync user data with the backend.
                userRepositoryImpl.syncDatabase()
            }
        } catch (e: Exception) {
            // Log an error message if synchronization fails.
            Log.e("UiHelper", "Backend sync failed: ${e.message}")
        }

        // Fetch the updated user data on the IO dispatcher and return it.
        val updatedUser = withContext(Dispatchers.IO) {
            userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)
        }
        return updatedUser
    }
}
