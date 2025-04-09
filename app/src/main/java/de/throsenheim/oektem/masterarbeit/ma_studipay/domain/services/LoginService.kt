package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking

object LoginService {
    fun loginService(
        matriculationNumber: String,
        password: String,
        userRepositoryImpl: UserRepositoryImpl
    ): Boolean {
        return runBlocking {
            val user = userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)

            if (user != null && verifyPassword(password, user.password)) {
                // Login successful
                true
            } else {
                userRepositoryImpl.syncDatabase()
                false
            }
        }
    }

    private fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}