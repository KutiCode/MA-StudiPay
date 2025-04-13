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
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserRepositoryImpl provides concrete implementations for user-related operations,
 * including inserting, fetching, updating, and synchronizing user data between the local database
 * and the backend API.
 */
class UserRepositoryImpl(
    val userDao: UserDao,                   // Data Access Object for user operations on the local database.
    private val apiService: ApiService,       // API service instance for remote network calls.
    private val context: Context              // Application context to access shared preferences and other resources.
) : UserRepository {

    /**
     * Inserts a new user into the local database.
     *
     * @param user The User entity to be inserted.
     */
    override suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    /**
     * Retrieves a user from the local database by their matriculation number.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @return The User object if found; otherwise, null.
     */
    override suspend fun getUserByMatriculationNumber(matriculationNumber: String): User? {
        return userDao.getUserByMatriculationNumber(matriculationNumber)
    }

    /**
     * Synchronizes the local user database with the backend.
     *
     * The method fetches a list of users from the remote API and inserts them into the local database.
     * Logs details on success or failure.
     */
    override suspend fun syncDatabase() {
        if (UiHelper.isHostReachableWithSocket()) {
        try {
            // Perform an API call to fetch all users.
            val response = RetrofitInstance.api.getAllUsers()
            if (response.isSuccessful) {
                // Get the list of users from the response.
                val userResponse: UserResponse? = response.body()
                val users = userResponse?.users ?: emptyList()
                // Insert the fetched users into the local database.
                userDao.insertUsers(users)
                Log.d("UserRepository", "Database synchronized successfully")
            } else {
                // Log an error message if the API call failed.
                Log.e("UserRepository", "Failed to fetch users: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during database sync", e)
        }
        } else {
            Log.e("UserRepository", "Host unreachable")
        }
    }

    /**
     * Updates the secure PIN for a specific user.
     *
     * It sends an update request to the backend, and if successful, it updates the PIN in the local database.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @param newPin The new secure PIN to set.
     */
    override suspend fun updateSecurePin(matriculationNumber: String, newPin: String) {
        val request = SecurePinUpdateRequest(matriculationNumber, newPin)
        val response = RetrofitInstance.api.updateSecurePin(request)
        if (response.isSuccessful) {
            // Update the PIN locally if the backend update succeeds.
            userDao.updateSecurePin(matriculationNumber, newPin)
        }
    }

    /**
     * Retrieves the secure PIN for a specific user from the local database.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @return The secure PIN as a String, or null if not found.
     */
    override suspend fun getSecurePin(matriculationNumber: String): String? {
        return userDao.getSecurePin(matriculationNumber)
    }

    /**
     * Synchronizes a specific user's data with the backend.
     *
     * It updates the backend with changes in the user entity and, if successful,
     * updates the local database accordingly.
     *
     * @param user The User object to synchronize.
     */
    override suspend fun syncUserWithBackend(user: User) {
        try {
            // Send a request to update the user details on the backend.
            val response = apiService.updateUser(user)
            if (response.isSuccessful) {
                // Update the user in the local database if successful.
                userDao.updateUser(user)
                Log.d("UserRepository", "User synchronized with backend successfully")
            } else {
                Log.e("UserRepository", "Failed to update user: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during user sync", e)
        }
    }

    /**
     * Retrieves the current logged-in user based on shared preferences.
     *
     * It accesses shared preferences to get the current user's matriculation number and then fetches
     * that user from the local database.
     *
     * @return The current User, or null if not found.
     */
    override suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            // Retrieve shared preferences to extract the current user's matriculation number.
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matriculationNumber = sharedPreferences.getString("current_username", null)
            // Return the user if the matriculation number exists.
            matriculationNumber?.let { userDao.getUserByMatriculationNumber(it) }
        }
    }
}
