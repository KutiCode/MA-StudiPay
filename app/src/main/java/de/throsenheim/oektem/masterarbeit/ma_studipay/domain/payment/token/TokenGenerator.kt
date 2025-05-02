package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.*

// TokenGenerator encapsulates the logic for generating a payment token based on user and bank details.
object TokenGenerator {
    // Gson instance for JSON conversion.
    private val gson = Gson()
    /**
     * Generates a PaymentToken for the current user.
     *
     * The function retrieves the user’s matriculation number from shared preferences, fetches the user
     * from the local database, retrieves the bank secret from the bank repository, and combines these
     * details along with the current date and transaction details into a PaymentToken object.
     *
     * @param context The context used for accessing shared preferences, local database, and remote API.
     * @return A PaymentToken populated with the user's data and bank secret.
     * @throws Exception if required user data or bank secrets are missing.
     */
    suspend fun generateToken(context: Context): String {
        // Retrieve the current user's matriculation number from SharedPreferences.
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matriculationNumber = sharedPref.getString("current_username", null)
            ?: throw Exception("Keine Matrikelnummer in SharedPreferences gefunden")

        // Create instances for API and local database access.
        val userApi = RetrofitInstance.api
        val userDao = AppDatabase.getDatabase(context).userDao()
        // Create a UserRepositoryImpl to work with user-related data.
        val userRepositoryImpl = UserRepositoryImpl(
            userDao,
            userApi,
            context
        )
        // Retrieve the user by matriculation number; throw an exception if not found.
        val user = userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)
            ?: throw Exception("User mit Matrikelnummer $matriculationNumber nicht gefunden")

        // Retrieve the bank DAO from the local database.
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        // Create a BankRepositoryImpl instance to fetch bank information.
        val bankRepositoryImpl = BankRepositoryImpl(bankDao)
        // Synchronize the bank data from the backend to ensure it's up-to-date.
        bankRepositoryImpl.syncBanksFromBackend()
        // Retrieve the bank's secrets (sensitive data) using the user's bank code.
        val bankWithSecrets = bankRepositoryImpl.getBankWithSecrets(user.bank_code ?: "")
        // Extract the first secret's code or throw an exception if not found.
        val bankSecrets = bankWithSecrets?.secrets?.map { it.secretCode }
            ?: throw Exception("Kein BankSecret für Bank-Code ${user.bank_code} gefunden")

        // Get the current date and format it as a string.
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = formatter.format(currentDate)
        // Create a PaymentToken using the gathered user and bank details.
        val generatedToken =
            PaymentToken(
            firstName = user.firstName,                      // User's first name.
            lastName = user.lastName,                        // User's last name.
            matriculationNumber = user.matriculationNumber,  // User's matriculation number.
            accountNumber = user.accountNumber,              // User's account number.
            balance = user.balance,                          // User's account balance.
            bankCode = user.bank_code ?: "",                 // Bank code associated with the user.
                bankSecrets = bankSecrets,                   // Secret associated with the bank.
            date = dateString,                               // Current date as a string.
            dailyTransactionCount = user.dailyTransactionCount,         // Daily transaction count.
            lastTransactionDate = user.lastTransactionDate,               // The date of the last transaction.
            highRiskAbortedCount = user.highRiskAbortedCount,             // Number of high-risk aborted transactions.
                lastTransactionRiskValue = user.lastTransactionRiskValue      // Risk value of the last transaction.
        )


        // Log the generated token for debugging purposes.
        Log.d("TokenGenerator", "Generated token: $generatedToken")
        val stringToken = gson.toJson(generatedToken)
        return stringToken
    }
}
