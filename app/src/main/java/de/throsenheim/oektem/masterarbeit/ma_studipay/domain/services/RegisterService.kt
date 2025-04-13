package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.UserRegistrationRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

// RegisterService is an object responsible for handling the registration process of a new user.
// It validates if the user exists, hashes the password, creates a new user object, sends a registration request,
// and synchronizes the local database with the backend.
object RegisterService {

    /**
     * Handles user registration by checking if the user exists, hashing the provided password,
     * creating a new user, sending the registration request to the backend, and updating the local database.
     *
     * @param matriculationNumber The matriculation number uniquely identifying the user.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param password The plain-text password entered by the user.
     * @param userRepositoryImpl The repository used to perform database operations related to users.
     * @return A String message indicating the outcome of the registration.
     */
    fun registerService(
        matriculationNumber: String,
        firstName: String,
        lastName: String,
        password: String,
        userRepositoryImpl: UserRepositoryImpl
    ): String {
        // Use runBlocking to run the coroutine in a blocking manner, needed for synchronous registration execution.
        return runBlocking {
            // Check if a user with the given matriculation number already exists in the local database.
            val userExists =
                userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber) != null
            if (userExists) {
                // If a user is found, return an error message.
                return@runBlocking "Nutzer existiert bereits"
            } else {
                // Otherwise, hash the password using BCrypt with a cost factor of 12.
                val hashedPassword = hashPassword(password)
                // Generate a unique account number for the user.
                val user = User(
                    matriculationNumber = matriculationNumber,
                    firstName = firstName,
                    lastName = lastName,
                    password = hashedPassword,
                    accountNumber = generateUniqueAccountNumber(userRepositoryImpl),
                    balance = 0.0,        // Default balance is set to 0.0 for new users.
                    securePin = "0000"    // Default secure PIN is set; can be updated by the user later.
                )
                // Create a registration request object containing all necessary user data.
                val request = UserRegistrationRequest(
                    matriculationNumber = matriculationNumber,
                    firstName = firstName,
                    lastName = lastName,
                    password = hashedPassword,
                    accountNumber = user.accountNumber,
                    balance = user.balance,
                    securePin = user.securePin
                )
                try {
                    // Make the registration API call using Retrofit.
                    val response = RetrofitInstance.api.registerUser(request)
                    if (response.code() == 200) {
                        // If the backend registration is successful, insert the user into the local database.
                        userRepositoryImpl.insertUser(user)
                        // Synchronize the local user database to ensure it's up-to-date.
                        userRepositoryImpl.syncDatabase()
                    } else {
                        userRepositoryImpl.syncDatabase()
                        if (response.code() == 400) {
                            // Return an error message if the backend registration fails.
                            return@runBlocking "Nutzer existiert bereits"
                        } else if (response.code() == 500) {
                            // Return an error message if the backend registration fails.
                            return@runBlocking "Backend-Fehler: Datenbankfehler"
                        } else if (response.code() == 404) {
                            // Return an error message if the backend registration fails.
                            return@runBlocking "Fehler in der Anfrage"
                        } else {
                            // Return an error message if the backend registration fails.
                            return@runBlocking "Backend-Fehler"
                        }
                    }
                } catch (e: Exception) {
                    // Catch any exceptions during the API call and return an error message containing the exception's message.
                    return@runBlocking "Fehler: ${e.message}"
                }
                // Return a success message indicating the user was registered successfully.
                return@runBlocking "User erfolgreich registriert"
            }
        }
    }

    /**
     * Hashes the provided password using BCrypt.
     *
     * @param password The plain-text password.
     * @return The hashed password as a String.
     */
    private fun hashPassword(password: String): String {
        // Use BCrypt with default settings and a cost factor of 12.
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    /**
     * Generates a unique account number by generating a random 6-digit number and verifying that it is not already assigned.
     *
     * @param userRepositoryImpl The repository used to access user data.
     * @return A unique account number as a String.
     */
    private suspend fun generateUniqueAccountNumber(userRepositoryImpl: UserRepositoryImpl): String {
        var accountNumber: String
        // Loop until a unique account number (a random number between 100000 and 999999) is found.
        do {
            accountNumber = (100000..999999).random().toString()
        } while (userRepositoryImpl.userDao.getAllUsers().any { it.accountNumber == accountNumber })
        return accountNumber
    }
}
