package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.content.Context
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.ApiService
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.SecurePinUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.response.UserResponse
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepositoryImpl(
    val userDao: UserDao,
    private val apiService: ApiService,
    private val context: Context

): UserRepository {


    override suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    override suspend fun getUserByMatriculationNumber(immatriculationNumber: String): User? {
        return userDao.getUserByMatriculationNumber(immatriculationNumber)
    }

    override suspend fun syncDatabase() {
        try {
            val response = RetrofitInstance.api.getAllUsers()
            if (response.isSuccessful) {
                val userResponse: UserResponse? = response.body()
                val users = userResponse?.users ?: emptyList()
                userDao.insertUsers(users)
                Log.d("UserRepository", "Database synchronized successfully")
            } else {
                Log.e("UserRepository", "Failed to fetch users: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during database sync", e)
        }
    }


    override suspend fun updateSecurePin(immatriculationNumber: String, newPin: String) {
        val request = SecurePinUpdateRequest(immatriculationNumber, newPin)
        val response = RetrofitInstance.api.updateSecurePin(request)
        if (response.isSuccessful) {
            userDao.updateSecurePin(immatriculationNumber, newPin)
        }
    }

    override suspend fun getSecurePin(immatriculationNumber: String): String? {
        return userDao.getSecurePin(immatriculationNumber)
    }

    override suspend fun syncUserWithBackend(user: User) {
        try {
            val response = apiService.updateUser(user)
            if (response.isSuccessful) {
                userDao.updateUser(user)
                Log.d("UserRepository", "User synchronized with backend successfully")
            } else {
                Log.e("UserRepository", "Failed to update user: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during user sync", e)
        }
    }
    override suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matriculationNumber = sharedPreferences.getString("current_username", null)
            matriculationNumber?.let { userDao.getUserByMatriculationNumber(it) }
        }
    }

}