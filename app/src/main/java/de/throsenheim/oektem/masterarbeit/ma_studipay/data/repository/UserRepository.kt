package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.content.Context
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.UserDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.ApiService
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.SecurePinUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    val userDao: UserDao,
    private val apiService: ApiService,
    private val context: Context

) {


    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByMatrikelnumber(matrikelnumber: String): User? {
        return userDao.getUserByMatrikelnumber(matrikelnumber)
    }

    suspend fun syncDatabase() {
        try {
            val response = RetrofitInstance.api.getAllUsers()
            if (response.isSuccessful) {
                val userResponse: UserResponse? = response.body()
                val users = userResponse?.users ?: emptyList()
                // Update local database with the fetched users
                userDao.insertUsers(users)
                Log.d("UserRepository", "Database synchronized successfully")
            } else {
                Log.e("UserRepository", "Failed to fetch users: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during database sync", e)
        }
    }

    // SecurePin aktualisieren
    suspend fun updateSecurePin(matrikelnumber: String, newPin: String) {
        val request = SecurePinUpdateRequest(matrikelnumber, newPin)
        val response = RetrofitInstance.api.updateSecurePin(request)
        if (response.isSuccessful) {
            userDao.updateSecurePin(matrikelnumber, newPin)
        }
    }

    // SecurePin abrufen
    suspend fun getSecurePin(matrikelnumber: String): String? {
        return userDao.getSecurePin(matrikelnumber)
    }

    suspend fun syncUserWithBackend(user: User) {
        try {
            val response = apiService.updateUser(user)
            if (response.isSuccessful) {
                // Update the user in the local database
                userDao.updateUser(user)
                Log.d("UserRepository", "User synchronized with backend successfully")
            } else {
                // Handle unsuccessful response, e.g., log the error
                Log.e("UserRepository", "Failed to update user: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle the exception, e.g., log the error
            Log.e("UserRepository", "Exception during user sync", e)
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matrikelnumber = sharedPreferences.getString("current_username", null)
            matrikelnumber?.let { userDao.getUserByMatrikelnumber(it) }
        }
    }

}