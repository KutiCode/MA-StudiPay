package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

import android.content.Context
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.RiskValueUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.TransactionVerificationRequest
import java.text.SimpleDateFormat
import java.util.*

/**
 * TokenExtractor encapsulates logic to process a PaymentToken and determine the outcome of a transaction.
 * It calculates a risk value based on several parameters, validates the token recency,
 * and performs authorization or rejection based on the risk level.
 */
object TokenExtractor {

    // 'today' is a computed property that returns the current date in "yyyy-MM-dd" format.
    private val today: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    /**
     * Extracts the token from the response and determines the outcome of the transaction.
     *
     * It calculates a risk value based on factors such as recency, amount, transaction count, and bank secret verification.
     * Depending on the risk value, it may certify the transaction or request additional authorization,
     * returning either Success or Rejection.
     *
     * @param context The context used to access the database and shared preferences.
     * @param amount The transaction amount.
     * @param paymentToken The PaymentToken object containing transaction and user data.
     * @return A TransactionOutcome (Success, or Rejection) based on risk assessment.
     */
    suspend fun extractTokenFromResponse(
        context: Context,
        amount: Double,
        paymentToken: PaymentToken
    ): TransactionOutcome {
        // Calculate the risk value for this transaction.
        val riskValue = calculateRiskValue(context, amount, paymentToken)

        // Determine the outcome based on the calculated risk value.
        return when {
            // If risk is low, certify the transaction and mark as Success.
            riskValue < 35 -> {
                transactionCertificate(context, paymentToken, amount)
                TransactionOutcome.Success
            }
            // If risk is moderate, perform an additional authorization check.
            riskValue < 90 -> {
                authorizationRequestCheck(context, paymentToken, amount)
            }
            // If risk is high, generate a rejection certificate and return Rejection.
            else -> {
                transactionRejectionCertificate(paymentToken)
                TransactionOutcome.Rejection
            }
        }
    }

    /**
     * Checks if the given token date is recent.
     *
     * Compares the token's timestamp with the current time.
     * Returns true if the difference is less than or equal to 10,000 milliseconds.
     *
     * @param tokenDateString The date string from the token in "yyyy-MM-dd HH:mm:ss" format.
     * @return True if the token is recent, false otherwise.
     */
    private fun isTokenRecent(tokenDateString: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        // Parse the token's date; return false if parsing fails.
        val tokenDate: Date = sdf.parse(tokenDateString) ?: return false
        val currentTime = Date()
        val diffMillis = currentTime.time - tokenDate.time
        return diffMillis <= 10_000
    }

    /**
     * Calculates a risk value based on several parameters such as token recency, transaction amount,
     * daily transaction count, last transaction date, high-risk aborted count, last transaction risk value,
     * and bank secret verification.
     *
     * The risk value starts at 100 and is reduced or increased based on each parameter.
     *
     * @param context The context to access the bank repository.
     * @param amount The transaction amount.
     * @param paymentToken The PaymentToken containing user and transaction details.
     * @return The calculated risk value as an integer.
     */
    private suspend fun calculateRiskValue(
        context: Context,
        amount: Double,
        paymentToken: PaymentToken
    ): Int {

        var riskValue = 100

        // Reduce risk if the token is recent.
        if (isTokenRecent(paymentToken.date)) {
            Log.d("RiskValue", "Token recency reduces risk: $riskValue")
            riskValue -= 10
        }

        // Adjust risk based on the transaction amount.
        if (amount < 10.0) {
            riskValue -= 30
            Log.d("RiskValue", "Low amount reduces risk significantly: $riskValue")
        } else if (amount < 30.0) {
            riskValue -= 20
            Log.d("RiskValue", "Moderate amount reduces risk: $riskValue")
        } else {
            riskValue -= 5
            Log.d("RiskValue", "High amount reduces risk minimally: $riskValue")
        }

        // Adjust risk based on daily transaction count.
        if (paymentToken.dailyTransactionCount < 3) {
            riskValue -= 10
            Log.d("RiskValue", "Low daily transaction count reduces risk: $riskValue")
        } else {
            riskValue += 5
            Log.d("RiskValue", "High daily transaction count increases risk: $riskValue")
        }

        // Adjust risk based on last transaction date.
        if (paymentToken.lastTransactionDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today: String = sdf.format(Date())
            if (paymentToken.lastTransactionDate == today) {
                riskValue -= 10
                Log.d("RiskValue", "Recent last transaction date reduces risk: $riskValue")
            } else {
                riskValue += 5
                Log.d("RiskValue", "Old last transaction date increases risk: $riskValue")
            }
        }

        // Increase risk if there have been aborted high-risk transactions.
        if (paymentToken.highRiskAbortedCount > 0) {
            riskValue += 10
            Log.d("RiskValue", "High-risk aborted transactions increase risk: $riskValue")
        }

        // Adjust risk based on the last transaction's risk value.
        if (paymentToken.lastTransactionRiskValue < 50) {
            riskValue -= 10
            Log.d("RiskValue", "Low last transaction risk reduces risk: $riskValue")
        } else if (paymentToken.lastTransactionRiskValue < 80) {
            riskValue -= 5
            Log.d("RiskValue", "Moderate last transaction risk slightly reduces risk: $riskValue")
        } else {
            riskValue += 10
            Log.d("RiskValue", "High last transaction risk increases risk: $riskValue")
        }

        // Verify the bank secret; if valid, decrease the risk value significantly.
        if (verifyBankSecret(context, paymentToken)) {
            riskValue -= 25
            Log.d("RiskValue", "Valid bank secret reduces risk: $riskValue")
        }
        // Update the payment token's last transaction risk value with the calculated risk.
        paymentToken.lastTransactionRiskValue = riskValue
        Log.d("RiskValue", "Final calculated risk: $riskValue")
        Log.d("LastRiskValue", paymentToken.lastTransactionRiskValue.toString())
        return riskValue
    }

    /**
     * Verifies if the bank secret included in the payment token matches one stored in the bank data.
     *
     * It retrieves the bank data using the bank code and checks if any secret matches.
     *
     * @param context The context used to access the local database.
     * @param paymentToken The PaymentToken that contains the bank code and bank secret.
     * @return True if the bank secret is verified; otherwise, false.
     */
    private suspend fun verifyBankSecret(context: Context, paymentToken: PaymentToken): Boolean {
        // Access the bank DAO from the database.
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        // Create a BankRepositoryImpl instance to get bank secrets.
        val bankRepositoryImpl = BankRepositoryImpl(bankDao)
        // Retrieve the bank with its secrets using the user's bank code.
        val bankWithSecrets = bankRepositoryImpl.getBankWithSecrets(paymentToken.bankCode)

        if (bankWithSecrets == null || bankWithSecrets.secrets.isEmpty()) {
            return false
        }

        // Return true if any secret in the bank matches the secret in the payment token.
        return bankWithSecrets.secrets.any { it.secretCode in paymentToken.bankSecrets }
    }

    /**
     * Processes the transaction certificate when risk is low.
     *
     * It checks whether the user has sufficient balance,
     * updates local balance values, and updates risk parameters.
     * If the user has insufficient balance, the transaction is rejected.
     *
     * @param context The context used to access the database and shared preferences.
     * @param paymentToken The PaymentToken with transaction and user details.
     * @param amount The amount to be transacted.
     */
    private suspend fun transactionCertificate(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ) {
        // If the user's balance is insufficient, generate a rejection certificate.
        if (paymentToken.balance < amount) {
            transactionRejectionCertificate(paymentToken)
        } else {
            // Retrieve the matriculation number from shared preferences.
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matriculationNumber = sharedPref.getString("current_username", null)
            // Access the user DAO.
            val userDao = AppDatabase.getDatabase(context).userDao()

            // Prepare requests for both adding and deducting balance.
            val requestAdd = BalanceUpdateRequest(matriculationNumber!!, amount)
            val requestDeduct = BalanceUpdateRequest(paymentToken.matriculationNumber, amount)

            // Call the API to add balance.
            val responseAdd = RetrofitInstance.api.addBalance(requestAdd)
            if (responseAdd.isSuccessful) {
                val user = userDao.getUserByMatriculationNumber(matriculationNumber)
                user?.let {
                    it.balance += amount
                    // Update the user's balance in the local database.
                    userDao.updateUserBalance(matriculationNumber, it.balance)
                }
            } else {
                // If the add operation fails, generate a rejection certificate.
                transactionRejectionCertificate(paymentToken)
            }

            // Call the API to deduct balance.
            val responseDeduct = RetrofitInstance.api.deductBalance(requestDeduct)
            if (responseDeduct.isSuccessful) {
                // Get the user who is receiving the funds.
                val userHold =
                    userDao.getUserByMatriculationNumber(paymentToken.matriculationNumber)
                val userHoldNewBalance = userHold?.balance?.minus(amount)
                if (userHold != null) {
                    // Update the receiver's balance.
                    userDao.updateUserBalance(userHold.matriculationNumber, userHoldNewBalance!!)
                }
            } else {
                // If the deduction operation fails, reject the transaction.
                transactionRejectionCertificate(paymentToken)
            }

            // Update risk parameters after a successful transaction.
            updateRiskParams(
                paymentToken.matriculationNumber,
                paymentToken.dailyTransactionCount + 1,
                today,
                0,
                paymentToken.lastTransactionRiskValue
            )
        }
    }

    /**
     * Checks for additional authorization if the risk value falls within a moderate range.
     *
     * It sends a transaction verification request to the backend.
     * If the response is successful, it updates risk parameters and, if updated successfully,
     * processes the transaction certificate, otherwise rejects the transaction.
     *
     * @param context The context used for API calls and database operations.
     * @param paymentToken The PaymentToken containing transaction details.
     * @param amount The transaction amount.
     * @return A TransactionOutcome indicating Success or Rejection.
     */
    private suspend fun authorizationRequestCheck(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ): TransactionOutcome {
        // Create a transaction verification request.
        val requestAuth = TransactionVerificationRequest(paymentToken.matriculationNumber, amount)
        // Send the request to the backend.
        val responseAuth = RetrofitInstance.api.verifyTransaction(requestAuth)

        if (responseAuth.isSuccessful) {
            // Update risk parameters if the response is successful.
            val riskParamsUpdated = updateRiskParams(
                paymentToken.matriculationNumber,
                paymentToken.dailyTransactionCount + 1,
                today,
                paymentToken.highRiskAbortedCount,
                paymentToken.lastTransactionRiskValue
            )
            return if (riskParamsUpdated) {
                // Process the transaction certificate if risk parameters are updated successfully.
                transactionCertificate(context, paymentToken, amount)
                TransactionOutcome.Success
            } else {
                // Reject the transaction if risk parameters update fails.
                transactionRejectionCertificate(paymentToken)
                TransactionOutcome.Rejection
            }
        } else {
            // If the authorization request fails, reject the transaction.
            transactionRejectionCertificate(paymentToken)
            return TransactionOutcome.Rejection
        }
    }

    /**
     * Generates a transaction rejection certificate by updating risk parameters with an incremented high-risk aborted count.
     *
     * @param paymentToken The PaymentToken whose transaction is being rejected.
     * @return A TransactionOutcome indicating Rejection.
     */
    private suspend fun transactionRejectionCertificate(
        paymentToken: PaymentToken,
    ): TransactionOutcome {
        // Prepare a request to update risk parameters, incrementing the high-risk aborted count.
        val requestRiskValueUpdate = RiskValueUpdateRequest(
            paymentToken.matriculationNumber,
            paymentToken.dailyTransactionCount + 1,
            today,
            paymentToken.highRiskAbortedCount + 1,
            paymentToken.lastTransactionRiskValue
        )
        // Update the risk parameters on the backend.
        RetrofitInstance.api.updateRiskParams(requestRiskValueUpdate)
        return TransactionOutcome.Rejection
    }

    /**
     * Updates the risk parameters for a transaction.
     *
     * This sends an update request to the backend with the new risk parameters.
     *
     * @param matriculationNumber The user's matriculation number.
     * @param dailyTransactionCount The updated count of daily transactions.
     * @param lastTransactionDate The current date.
     * @param highRiskAbortedCount The updated count of high-risk aborted transactions.
     * @param lastTransactionRiskValue The latest risk value calculated for the transaction.
     * @return True if the update is successful; false otherwise.
     */
    private suspend fun updateRiskParams(
        matriculationNumber: String,
        dailyTransactionCount: Int,
        lastTransactionDate: String,
        highRiskAbortedCount: Int,
        lastTransactionRiskValue: Int
    ): Boolean {
        // Build the update request with the new risk parameters.
        val requestParamUpdate = RiskValueUpdateRequest(
            matriculationNumber,
            dailyTransactionCount,
            lastTransactionDate,
            highRiskAbortedCount,
            lastTransactionRiskValue
        )

        // Call the API to update risk parameters.
        val response = RetrofitInstance.api.updateRiskParams(requestParamUpdate)

        return response.isSuccessful
    }
}
