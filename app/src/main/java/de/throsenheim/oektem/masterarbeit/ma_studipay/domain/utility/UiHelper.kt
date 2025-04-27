package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext

import java.net.InetSocketAddress
import java.net.Socket

// Helper object containing utility functions used throughout the app.
object UiHelper {

    /**
     * Loads a user from the local database by matriculation number.
     *
     * @param context The context used to access the database.
     * @param matriculationNumber The matriculation number to identify the user.
     * @return A User object if found, otherwise null.
     */
    suspend fun loadUser(context: Context): User? {
        val sharedUser = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matriculationNumber = sharedUser.getString("current_username", "") ?: ""
        // Get an instance of the user DAO from the database.
        val userDao = AppDatabase.getDatabase(context).userDao()
        // Return the user matching the matriculation number.
        return userDao.getUserByMatriculationNumber(matriculationNumber)
    }

    suspend fun updateDatabase(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = context
        )
        val bankRepositoryImpl = BankRepositoryImpl(database.bankDao())
        // Synchronize bank data from the backend.
        bankRepositoryImpl.syncBanksFromBackend()
        // Synchronize user data with the backend.
        userRepositoryImpl.syncDatabase()
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


    suspend fun isHostReachableWithSocket(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("192.168.0.10", 5000), 1500)
                }
                true
            } catch (e: Exception) {
                Log.e("UiHelper", "Backend is not reachable: ${e.message}")
                false
            }
        }
    }
}
