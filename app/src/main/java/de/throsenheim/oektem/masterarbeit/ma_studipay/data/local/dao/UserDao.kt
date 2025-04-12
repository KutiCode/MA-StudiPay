package de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

/**
 * UserDao defines the methods for accessing and manipulating user-related data in the local Room database.
 */
@Dao
interface UserDao {

    // Inserts a new user into the database.
    // If a user with the same primary key exists, it will be replaced.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Retrieves a user from the database by their matriculation number (primary key).
    @Query("SELECT * FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getUserByMatriculationNumber(matriculationNumber: String): User?

    // Retrieves all users from the database.
    // Useful for debugging or for syncing operations.
    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>

    // Retrieves the first name of a user by matriculation number.
    @Query("SELECT firstName FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getUserFirstName(matriculationNumber: String): String?

    // Retrieves the last name of a user by matriculation number.
    @Query("SELECT lastName FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getUserLastName(matriculationNumber: String): String?

    // Retrieves the balance of a user by matriculation number.
    @Query("SELECT balance FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getUserBalance(matriculationNumber: String): Double?

    // Retrieves the account number of a user by matriculation number.
    @Query("SELECT accountNumber FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getUserAccountNumber(matriculationNumber: String): String?

    // Checks if a given account number already exists by counting the number of users with that account number.
    @Query("SELECT COUNT(*) FROM user WHERE accountNumber = :accountNumber")
    suspend fun countByKontonummer(accountNumber: String): Int

    // Updates the secure PIN for a user identified by their matriculation number.
    @Query("UPDATE user SET securePin = :securePin WHERE matriculationNumber = :matriculationNumber")
    suspend fun updateSecurePin(matriculationNumber: String, securePin: String)

    // Retrieves the secure PIN of a user by matriculation number.
    @Query("SELECT securePin FROM user WHERE matriculationNumber = :matriculationNumber")
    suspend fun getSecurePin(matriculationNumber: String): String?

    // Updates the balance of a user identified by their matriculation number.
    @Query("UPDATE user SET balance = :balance WHERE matriculationNumber = :matriculationNumber")
    suspend fun updateUserBalance(matriculationNumber: String, balance: Double)

    // Inserts or replaces a list of users in the database.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // Updates a user record in the database.
    @Update
    suspend fun updateUser(user: User)
}
