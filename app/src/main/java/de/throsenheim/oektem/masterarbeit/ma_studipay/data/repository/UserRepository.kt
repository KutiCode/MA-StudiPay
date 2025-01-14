package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.SyncQueueDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.SyncQueueEntry
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService
import de.throsenheim.oektem.masterarbeit.ma_studipay.worker.LoginRequest

class UserRepository(
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao,
    private val apiService: ApiService
) {

    // Neuen Nutzer hinzufügen und in die Queue aufnehmen
    suspend fun insertUser(user: User) {
        userDao.insertUser(user) // Lokale Datenbank aktualisieren

    }

    suspend fun login(username: String, password: String): User? {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    userDao.insertUser(user) // Speichere Nutzer lokal
                }
                user
            } else {
                null
            }
        } catch (e: Exception) {
            // Offline-Login
            userDao.getUserByUsername(username)?.takeIf { it.password == password }
        }
    }

    // Registrierung
    suspend fun register(user: User) {
        try {
            val response = apiService.registerUser(user)
            if (response.isSuccessful) {
                userDao.insertUser(user) // Lokale Datenbank aktualisieren
            }
        } catch (e: Exception) {
            // Offline-Registrierung
            userDao.insertUser(user)
            syncQueueDao.insert(
                SyncQueueEntry(
                    operation = "INSERT",
                    userId = user.matrikelnumber,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }


}


