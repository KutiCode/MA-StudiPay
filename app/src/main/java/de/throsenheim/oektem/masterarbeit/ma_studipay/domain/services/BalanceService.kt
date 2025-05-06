package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import android.content.Context
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.runBlocking
import retrofit2.Response

// BalanceService provides methods to update the user's balance by either adding or deducting an amount.
// It leverages Retrofit for network calls and Room for local data persistence.
object BalanceService {

    /**
     * Adds a specified amount to the user's balance.
     *
     * This function builds a BalanceUpdateRequest and sends it to the backend API to add the given amount.
     * If the API call is successful, the local database is updated with the new balance.
     *
     * @param context The application context used to access the local database.
     * @param amount The amount to be added to the user's balance.
     * @return True if the API call is successful and the local database is updated; otherwise, false.
     */
    fun addBalanceService(context: Context, amount: Double): Boolean {
        return runBlocking {
            val user = UiHelper.loadUser(context)

            if (user?.bank_code == null) {
                Log.d("BalanceService", "User has no bank code")
                // If the user does not have a bank code, return false.
                false
            } else {
                if (UiHelper.isHostReachableWithSocket()) {
                    Log.d("BalanceService", "User current Balance: ${user.balance}")
                    // Build a BalanceUpdateRequest object with the matriculation number and amount.
                    val addRequest = BalanceUpdateRequest(user.matriculationNumber, amount)
                    // Make a synchronous API call to add the balance using Retrofit.
                    val addResponse: Response<Unit> = RetrofitInstance.api.addBalance(addRequest)
                    // Check if the backend call was successful.
                    if (addResponse.isSuccessful) {
                        // If successful, update the local database.
                        // Retrieve the user DAO instance from the AppDatabase.
                        val userDao = AppDatabase.getDatabase(context).userDao()
                        // Fetch the user from the database by matriculation number.
                        user.let {
                            // Increase the user's local balance by the specified amount.
                            it.balance += amount
                            // Update the user's balance in the local database.
                            userDao.updateUserBalance(user.matriculationNumber, it.balance)
                            Log.d("BalanceService", "User balance updated: ${it.balance}")
                        }
                        // Return true indicating the addition was successful.
                        true
                    } else {
                        // Return false if the API call to add balance was not successful.
                        false
                    }

                } else {
                    false
                }

            }
        }
    }

    /**
     * Deducts a specified amount from the user's balance.
     *
     * This function builds a BalanceUpdateRequest and sends it to the backend API to deduct the given amount.
     * If the API call is successful, the local database is updated with the new balance.
     *
     * @param context The application context used to access the local database.
     * @param amount The amount to be deducted from the user's balance.
     * @return True if the API call is successful and the local database is updated; otherwise, false.
     */
    fun reduceBalanceService(
        context: Context,
        amount: Double
    ): Boolean {
        return runBlocking {
            val user = UiHelper.loadUser(context)
            if (user?.bank_code == null) {
                Log.d("BalanceService", "User has no bank code")
                // If the user does not have a bank code, return false.
                false

            } else {
                if (UiHelper.isHostReachableWithSocket()) {
                    Log.d("BalanceService", "User current Balance: ${user.balance}")
                    // Build a BalanceUpdateRequest for deducting the amount.
                    val deductRequest = BalanceUpdateRequest(user.matriculationNumber, amount)
                    // Make a synchronous API call to deduct balance using Retrofit.
                    val deductResponse: Response<Unit> =
                        RetrofitInstance.api.deductBalance(deductRequest)
                    // Check if the API call was successful.
                    if (deductResponse.isSuccessful) {
                        // If successful, update the local database.
                        // Obtain the user DAO instance from the AppDatabase.
                        val userDao = AppDatabase.getDatabase(context).userDao()
                        // Retrieve the user identified by the matriculation number.

                        user.let {
                            // Subtract the specified amount from the user's local balance.
                            it.balance -= amount
                            // Update the user's balance in the local database.
                            userDao.updateUserBalance(user.matriculationNumber, it.balance)
                            Log.d("BalanceService", "User balance updated: ${it.balance}")
                        }
                        // Return true indicating the deduction was successful.
                        true
                    } else {
                        // Return false if the API call to deduct balance was unsuccessful.
                        false
                    }
                } else {
                    false
                }
            }
        }
    }
}
