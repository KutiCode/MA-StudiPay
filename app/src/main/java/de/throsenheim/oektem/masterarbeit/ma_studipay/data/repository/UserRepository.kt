package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.SyncQueueDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.SyncQueueEntry
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService

class UserRepository(
    private val userDao: UserDao,
    private val syncQueueDao: SyncQueueDao
) {

    // Neuen Nutzer hinzufügen und in die Queue aufnehmen
    suspend fun insertUser(user: User) {
        userDao.insertUser(user) // Lokale Datenbank aktualisieren
        syncQueueDao.insert(
            SyncQueueEntry(
                operation = "INSERT",
                userId = user.matrikelnumber,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // Synchronisation mit dem Backend
    suspend fun syncWithBackend(apiService: ApiService) {
        val unsyncedEntries = syncQueueDao.getAllEntries() // Hole ausstehende Einträge
        for (entry in unsyncedEntries) {
            when (entry.operation) {
                "INSERT" -> {
                    val user = userDao.getUserByMatrikelnumber(entry.userId)
                    if (user != null) {
                        try {
                            val response = apiService.registerUser(user)
                            if (response.isSuccessful) {
                                syncQueueDao.delete(entry) // Erfolgreich synchronisiert
                            }
                        } catch (e: Exception) {
                            // Fehlerbehandlung, falls die Synchronisation fehlschlägt
                        }
                    }
                }
                // Weitere Operationen wie "UPDATE", "DELETE" hier ergänzen
            }
        }
    }
}


