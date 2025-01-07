package de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User

@Dao
interface UserDao {

    // Benutzer hinzufügen
    @Insert
    suspend fun insertUser(user: User)

    // Benutzer anhand des Benutzernamens abrufen
    @Query("SELECT * FROM users WHERE benutzername = :benutzername")
    suspend fun getUserByBenutzername(benutzername: String): User?

    // Alle Benutzer abrufen (z. B. für Debugging)
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT vorname FROM users WHERE id = :userId")
    suspend fun getUserVorname(userId: Int): String?

}
