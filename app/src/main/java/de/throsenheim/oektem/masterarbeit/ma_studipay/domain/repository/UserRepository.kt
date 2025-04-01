package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

interface UserRepository {

    suspend fun insertUser(user: User)

    suspend fun getUserByImmatriculationNumber(immatriculationNumber: String): User?

    suspend fun syncDatabase()

    suspend fun updateSecurePin(immatriculationNumber: String, newPin: String)

    suspend fun getSecurePin(immatriculationNumber: String): String?

    suspend fun syncUserWithBackend(user: User)

    suspend fun getCurrentUser(): User?

}