package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.SyncQueueDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.SyncQueueEntry
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService

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

    //Funktion um die Datenbank aktuell zu halten
    suspend fun syncDatabase() {
        try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    for (user in users) {
                        userDao.insertUser(user) // Aktualisiere lokale Datenbank
                    }
                }
                Log.d("UserRepository", "Datenbank synchronisiert")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Fehler bei der Synchronisierung", e)
            // Keine Verbindung oder Fehler bei der Synchronisation
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






