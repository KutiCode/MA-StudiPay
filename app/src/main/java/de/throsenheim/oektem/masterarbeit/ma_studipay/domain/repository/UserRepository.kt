package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

/**
 * UserRepository defines the contract for operations related to user data management.
 *
 * This includes inserting new users, retrieving users by matriculation number,
 * synchronizing the local database with the backend, managing secure PINs, and retrieving
 * the current user.
 */
interface UserRepository {

    /**
     * Inserts a new user into the local data store.
     *
     * @param user The User object to insert.
     */
    suspend fun insertUser(user: User)

    /**
     * Retrieves a user by their matriculation number.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @return The User object if found, otherwise null.
     */
    suspend fun getUserByMatriculationNumber(matriculationNumber: String): User?

    /**
     * Synchronizes the local database with the backend.
     *
     * This function ensures that all local user data is up-to-date with the remote source.
     */
    suspend fun syncDatabase()

    /**
     * Updates the secure PIN for a given user.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @param newPin The new secure PIN to be set for the user.
     */
    suspend fun updateSecurePin(matriculationNumber: String, newPin: String)

    /**
     * Retrieves the secure PIN for a user.
     *
     * @param matriculationNumber The unique identifier for the user.
     * @return The secure PIN as a String if it exists, otherwise null.
     */
    suspend fun getSecurePin(matriculationNumber: String): String?

    /**
     * Synchronizes the specified user's data with the backend.
     *
     * This may involve updating the remote database with local changes.
     *
     * @param user The User object to synchronize.
     */
    suspend fun syncUserWithBackend(user: User)

    /**
     * Retrieves the current logged-in user.
     *
     * @return The current User if one exists, otherwise null.
     */
    suspend fun getCurrentUser(): User?
}
