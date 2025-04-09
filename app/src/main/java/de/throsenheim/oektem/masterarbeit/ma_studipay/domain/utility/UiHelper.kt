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

object UiHelper {


    suspend fun loadUser(context: Context, immatriculationNumber: String): User? {
        val userDao = AppDatabase.getDatabase(context).userDao()
        return userDao.getUserByMatriculationNumber(immatriculationNumber)
    }


    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }


    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    suspend fun userUpdater(
        matriculationNumber: String,
        bankRepositoryImpl: BankRepositoryImpl,
        userRepositoryImpl: UserRepositoryImpl
    ): User? {
        // Attempt backend synchronization on the IO dispatcher
        try {
            withContext(Dispatchers.IO) {
                bankRepositoryImpl.syncBanksFromBackend()
                userRepositoryImpl.syncDatabase()
            }
        } catch (e: Exception) {
            Log.e("UiHelper", "Backend sync failed: ${e.message}")
        }

        // Fetch updated user data after synchronization
        val updatedUser = withContext(Dispatchers.IO) {
            userRepositoryImpl.getUserByImmatriculationNumber(matriculationNumber)
        }
        return updatedUser
    }

}