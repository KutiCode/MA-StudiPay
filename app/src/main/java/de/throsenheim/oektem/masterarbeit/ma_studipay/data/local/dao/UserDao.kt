package de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

@Dao
interface UserDao {

    // Benutzer hinzufügen oder aktualisieren
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)


    // Benutzer anhand der Matrikelnummer abrufen (Primary Key)
    @Query("SELECT * FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getUserByMatrikelnumber(matrikelnumber: String): User?

    // Alle Benutzer abrufen (z. B. für Debugging)
    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>

    // Benutzer-Vorname anhand der Matrikelnummer abrufen
    @Query("SELECT firstName FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getUserFirstName(matrikelnumber: String): String?

    // Benutzer-Nachname anhand der Matrikelnummer abrufen
    @Query("SELECT lastName FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getUserLastName(matrikelnumber: String): String?

    // Benutzer-Guthaben anhand der Matrikelnummer abrufen
    @Query("SELECT balance FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getUserBalance(matrikelnumber: String): Double?

    // Benutzer-Kontonummer anhand der Matrikelnummer abrufen
    @Query("SELECT accountNumber FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getUserAccountNumber(matrikelnumber: String): String?

    // Prüfen, ob eine Kontonummer bereits existiert
    @Query("SELECT COUNT(*) FROM user WHERE accountNumber = :accountNumber")
    suspend fun countByKontonummer(accountNumber: String): Int

    // Secure Pin aktualisieren
    @Query("UPDATE user SET securePin = :securePin WHERE matrikelnumber = :matrikelnumber")
    suspend fun updateSecurePin(matrikelnumber: String, securePin: String)

    // Secure Pin abrufen
    @Query("SELECT securePin FROM user WHERE matrikelnumber = :matrikelnumber")
    suspend fun getSecurePin(matrikelnumber: String): String?

    @Query("UPDATE user SET balance = :balance WHERE matrikelnumber = :matrikelnumber")
    suspend fun updateUserBalance(matrikelnumber: String, balance: Double)

    // Mehrere Benutzer hinzufügen oder aktualisieren
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)
}
