package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.UserRegistrationRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

object RegisterService {


    fun registerService(
        matriculationNumber: String,
        firstName: String,
        lastName: String,
        password: String,
        userRepositoryImpl: UserRepositoryImpl
    ): String {
        return runBlocking {
            val userExists =
                userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber) != null
            if (userExists) {
                return@runBlocking "Nutzer existiert bereits"
            } else {
                val hashedPassword = hashPassword(password)
                val user = User(
                    matriculationNumber = matriculationNumber,
                    firstName = firstName,
                    lastName = lastName,
                    password = hashedPassword,
                    accountNumber = generateUniqueAccountNumber(userRepositoryImpl),
                    balance = 0.0,
                    securePin = "0000"
                )
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
                    val response = RetrofitInstance.api.registerUser(request)
                    if (response.isSuccessful) {
                        userRepositoryImpl.insertUser(user)
                        userRepositoryImpl.syncDatabase()

                    } else {
                        return@runBlocking "Registrierung beim Backend fehlgeschlagen"

                    }
                } catch (e: Exception) {
                    return@runBlocking "Fehler: ${e.message}"

                }
                return@runBlocking "User erfolgreich registriert"
            }

        }
    }


    private fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    private suspend fun generateUniqueAccountNumber(userRepositoryImpl: UserRepositoryImpl): String {
        var accountNumber: String
        do {
            accountNumber = (100000..999999).random().toString()
        } while (userRepositoryImpl.userDao.getAllUsers().any { it.accountNumber == accountNumber })
        return accountNumber
    }
}