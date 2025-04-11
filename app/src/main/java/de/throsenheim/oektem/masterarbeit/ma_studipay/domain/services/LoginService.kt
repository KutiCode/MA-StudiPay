package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking

// LoginService encapsulates the logic for authenticating a user.
object LoginService {

    /**
     * Attempts to log in the user by verifying the matriculation number and password.
     *
     * This function synchronously updates the local database by calling syncDatabase().
     * It then retrieves the user by their matriculation number and verifies the provided password.
     *
     * @param matriculationNumber The unique matriculation number for the user.
     * @param password The plain-text password entered by the user.
     * @param userRepositoryImpl The repository used to interact with the user database.
     * @return True if a user exists with the given matriculation number and the password matches; otherwise, false.
     */
    fun loginService(
        matriculationNumber: String,
        password: String,
        userRepositoryImpl: UserRepositoryImpl
    ): Boolean {
        // runBlocking is used here to execute coroutine code in a blocking manner
        // which is useful for synchronous login handling.
        return runBlocking {
            // Synchronize the local database with the backend.
            userRepositoryImpl.syncDatabase()
            // Retrieve the user using the matriculation number.
            val user = userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)

            // Check if the user exists and if the provided password matches the hashed password.
            user != null && verifyPassword(password, user.password)
        }
    }

    /**
     * Verifies the provided plain-text password against the stored hashed password.
     *
     * Uses BCrypt to compare the passwords.
     *
     * @param password The plain-text password to verify.
     * @param hashedPassword The hashed password stored for the user.
     * @return True if the verification succeeds; otherwise, false.
     */
    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        // Use BCrypt.verifyer() to check if the plain text password matches the hashed password.
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}
