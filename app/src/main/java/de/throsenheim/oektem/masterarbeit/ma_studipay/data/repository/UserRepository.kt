package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService

class UserRepository(private val userDao: UserDao, private val apiService: ApiService) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByUserName(username: String): User? {
        return userDao.getUserByUsername(username)
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
}

