package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import kotlinx.coroutines.runBlocking

// BankService object provides functions to perform bank-related operations,
// such as assigning a selected bank to an existing user.
object BankService {

    /**
     * Assigns a bank to the user with the specified matriculation number.
     *
     * This function retrieves the user from the repository,
     * updates the user's bank_code with the provided bank's code,
     * and then synchronizes the updated user data with the backend.
     *
     * @param userRepositoryImpl The repository used for accessing and updating user data.
     * @param matriculationNumber The unique matriculation number identifying the user.
     * @param bank The Bank object to be assigned to the user.
     * @return A Boolean indicating whether the operation was executed (always returns true in this implementation).
     */
    fun assignBankService(
        userRepositoryImpl: UserRepositoryImpl,
        matriculationNumber: String,
        bank: Bank
    ): Boolean {
        // runBlocking is used to perform the operations synchronously.
        return runBlocking {
            // Retrieve the user by matriculation number.
            val user = userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)
            if (user != null) {
                // Log information about updating the user's bank.
                Log.d(
                    "BankService",
                    "Aktualisiere User ${user.matriculationNumber} mit bankCode: ${bank.bank_code}"
                )
                // Update the user's bank_code with the selected bank's code.
                user.bank_code = bank.bank_code
                // Synchronize the updated user with the backend.
                userRepositoryImpl.syncUserWithBackend(user)
            }
            // In this implementation, always return true regardless of result.
            true
        }
    }
}
