package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.SyncQueueDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.SyncQueueEntry
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.UserResponse

class UserRepository(
    val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService

) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByMatrikelnumber(matrikelnumber: String): User? {
        return userDao.getUserByMatrikelnumber(matrikelnumber)

    }

    suspend fun syncDatabase() {
        try {
            val response = RetrofitInstance.api.getAllUsers()
            if (response.isSuccessful) {
                val userResponse: UserResponse? = response.body()
                val users = userResponse?.users ?: emptyList()
                // Update local database with the fetched users
                userDao.insertUsers(users)
                Log.d("UserRepository", "Database synchronized successfully")
            } else {
                Log.e("UserRepository", "Failed to fetch users: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during database sync", e)
        }
    }

    // Lokale Registrierung
    suspend fun registerUserLocally(user: User) {
        userDao.insertUser(user) // Speichere den Nutzer in der lokalen Datenbank
        syncQueueDao.insert(
            SyncQueueEntry(
                operation = "INSERT",
                userId = user.matrikelnumber,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    //SecurePin aktualisieren
    suspend fun updateSecurePin(matrikelnumber: String, newPin: String) {
        userDao.updateSecurePin(matrikelnumber, newPin)
    }

    //SecurePin abrufen
    suspend fun getSecurePin(matrikelnumber: String): String? {
        return userDao.getSecurePin(matrikelnumber)
    }


}






