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
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    // Alle Benutzer abrufen (z. B. für Debugging)
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT firstName FROM users WHERE id = :userId")
    suspend fun getUserFirstName(userId: Int): String?

    @Query("SELECT lastName FROM users WHERE id = :userId")
    suspend fun getUserLastName(userId: Int): String?

    @Query("SELECT balance FROM users WHERE id = :userId")
    suspend fun getUserBalance(userId: Int): Double?

    @Query("SELECT matrikelnumber FROM users WHERE id = :userId")
    suspend fun getUserMatrikelnumber(userId: Int): String?


    @Query("SELECT accountNumber FROM users WHERE id = :userId")
    suspend fun getUserAccountNumber(userId: Int): String?

    @Query("SELECT COUNT(*) FROM users WHERE accountNumber = :accountNumber")
    suspend fun countByKontonummer(accountNumber: String): Int

}
