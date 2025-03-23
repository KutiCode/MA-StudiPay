package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token

import android.content.Context
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RiskValueUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.TransactionVerificationRequest
import java.text.SimpleDateFormat
import java.util.*

object TokenExtractor {

    private val today: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    suspend fun extractTokenFromResponse(
        context: Context,
        amount: Double,
        paymentToken: PaymentToken
    ): TransactionOutcome {
        val riskValue = calculateRiskValue(context, amount, paymentToken)

        return when {
            riskValue < 35 -> {
                transactionCertificate(context, paymentToken, amount)
                TransactionOutcome.Success
            }

            riskValue < 90 -> {
                // Hier könnte ein zusätzlicher Autorisierungsschritt erfolgen.
                authorizationRequestCheck(context, paymentToken, amount)

            }

            else -> {
                transactionRejectionCertificate(context, paymentToken, amount)
                TransactionOutcome.Rejection
            }
        }
    }


    private fun isTokenRecent(tokenDateString: String): Boolean {
        // Angenommener Datumsformat, wie es im Token vorliegt:
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tokenDate: Date = sdf.parse(tokenDateString) ?: return false
        val currentTime = Date()
        // Differenz in Millisekunden:
        val diffMillis = currentTime.time - tokenDate.time
        // Wenn die Differenz 10 Sekunden (10.000 ms) oder weniger beträgt, ist der Token aktuell.
        return diffMillis <= 10_000
    }

    private suspend fun calculateRiskValue(
        context: Context,
        amount: Double,
        paymentToken: PaymentToken
    ): Int {

        var riskValue = 100

        if (isTokenRecent(paymentToken.date)) {
            Log.d("RiskValue", "Date: " + riskValue.toString())
            riskValue -= 10
        }

        if (amount < 10.0) {
            riskValue -= 30
            Log.d("RiskValue", "Amount: " + riskValue.toString())
        } else if (amount < 30.0) {
            riskValue -= 20
            Log.d("RiskValue", "Amount: " + riskValue.toString())
        } else {
            riskValue -= 5
            Log.d("RiskValue", "Amount: " + riskValue.toString())
        }

        if (paymentToken.dailyTransactionCount < 3) {
            riskValue -= 10
            Log.d("RiskValue", "DTC: " + riskValue.toString())
        } else {
            riskValue += 5
            Log.d("RiskValue", "DTC: " + riskValue.toString())
        }

        if (paymentToken.lastTransactionDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today: String = sdf.format(Date())
            if (paymentToken.lastTransactionDate == today) {
                riskValue -= 10
                Log.d("RiskValue", "LTD: " + riskValue.toString())
            } else {
                riskValue += 5
                Log.d("RiskValue", "LTD: " + riskValue.toString())
            }
        }

        if (paymentToken.highRiskAbortedCount > 0) {
            riskValue += 10
            Log.d("RiskValue", "HRAC: " + riskValue.toString())
        }

        if (paymentToken.lastTransactionRiskValue < 50) {
            riskValue -= 10
            Log.d("RiskValue", "LTR: " + riskValue.toString())
        } else if (paymentToken.lastTransactionRiskValue < 80) {
            riskValue -= 5
            Log.d("RiskValue", "LTR: " + riskValue.toString())
        } else {
            riskValue += 10
            Log.d("RiskValue", "LTR: " + riskValue.toString())
        }

        if (verifyBankSecret(context, paymentToken)) {
            riskValue -= 25
            Log.d("RiskValue", "VBS: " + riskValue.toString())
        }
        paymentToken.lastTransactionRiskValue = riskValue
        Log.d("RiskValue", riskValue.toString())
        Log.d("LastRiskValue", paymentToken.lastTransactionRiskValue.toString())
        return riskValue

    }

    private suspend fun verifyBankSecret(context: Context, paymentToken: PaymentToken): Boolean {
        // Hole die lokale Bank (inklusive Secrets) anhand des BankCodes
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        val bankRepositoryImpl = BankRepositoryImpl(bankDao)
        val bankWithSecrets = bankRepositoryImpl.getBankWithSecrets(paymentToken.bankCode)

        // Wenn keine Bank oder keine Secrets gefunden wurden, ist die Überprüfung fehlgeschlagen
        if (bankWithSecrets == null || bankWithSecrets.secrets.isEmpty()) {
            return false
        }

        // Vergleiche das im Token enthaltene Secret mit den in der DB hinterlegten Secrets
        return bankWithSecrets.secrets.any { it.secretCode == paymentToken.bankSecret }
    }

    private suspend fun transactionCertificate(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ) {
        if (paymentToken.balance < amount) {
            transactionRejectionCertificate(context, paymentToken, amount)
        } else {
            // In dieser Funktion werden die nötigen Informationen und schritte durchgeführt, um die Transaktion zu bestätigen.
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matrikelNumber = sharedPref.getString("current_username", null)
            val userDao = AppDatabase.getDatabase(context).userDao()
            val requestAdd = BalanceUpdateRequest(matrikelNumber!!, amount)
            val requestDeduct = BalanceUpdateRequest(paymentToken.matrikelNumber, amount)

            val responseAdd = RetrofitInstance.api.addBalance(requestAdd)
            if (responseAdd.isSuccessful) {
                val user = userDao.getUserByMatrikelnumber(matrikelNumber)
                user?.let {
                    it.balance += amount
                    userDao.updateUserBalance(matrikelNumber, it.balance)
                }
            } else {
                transactionRejectionCertificate(context, paymentToken, amount)
            }

            val responseDeduct = RetrofitInstance.api.deductBalance(requestDeduct)
            if (responseDeduct.isSuccessful) {
                val userHold = userDao.getUserByMatrikelnumber(paymentToken.matrikelNumber)
                val userHoldNewBalance = userHold?.balance?.minus(amount)
                if (userHold != null) {
                    userDao.updateUserBalance(userHold.matrikelnumber, userHoldNewBalance!!)
                }

            } else {
                transactionRejectionCertificate(context, paymentToken, amount)
            }

            updateRiskParams(
                context,
                paymentToken.matrikelNumber,
                paymentToken.dailyTransactionCount + 1,
                today,
                0,
                paymentToken.lastTransactionRiskValue
            )
        }

    }


    private suspend fun authorizationRequestCheck(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ): TransactionOutcome {
        val requestAuth = TransactionVerificationRequest(paymentToken.matrikelNumber, amount)
        val responseAuth = RetrofitInstance.api.verifyTransaction(requestAuth)

        if (responseAuth.isSuccessful) {
            val riskParamsUpdated = updateRiskParams(
                context,
                paymentToken.matrikelNumber,
                paymentToken.dailyTransactionCount + 1,
                today,
                paymentToken.highRiskAbortedCount,
                paymentToken.lastTransactionRiskValue
            )
            if (riskParamsUpdated) {
                transactionCertificate(context, paymentToken, amount)
                return TransactionOutcome.Success
            } else {
                transactionRejectionCertificate(context, paymentToken, amount)
                return TransactionOutcome.Rejection
            }


        } else {
            transactionRejectionCertificate(context, paymentToken, amount)
            return TransactionOutcome.Rejection
        }
    }

    private suspend fun transactionRejectionCertificate(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ): TransactionOutcome {
        val requestRiskValueUpdate = RiskValueUpdateRequest(
            paymentToken.matrikelNumber,
            paymentToken.dailyTransactionCount + 1,
            today,
            paymentToken.highRiskAbortedCount + 1,
            paymentToken.lastTransactionRiskValue
        )
        RetrofitInstance.api.updateRiskParams(requestRiskValueUpdate)
        return TransactionOutcome.Rejection
    }

    private suspend fun updateRiskParams(
        context: Context,
        matrikelNumber: String,
        dailyTransactionCount: Int,
        lastTransactionDate: String,
        highRiskAbortedCount: Int,
        lastTransactionRiskValue: Int
    ): Boolean {
        // In dieser Funktion werden die Risikoparameter des Nutzers aktualisiert.

        val requestParamUpdate = RiskValueUpdateRequest(
            matrikelNumber,
            dailyTransactionCount,
            lastTransactionDate,
            highRiskAbortedCount,
            lastTransactionRiskValue
        )

        val response = RetrofitInstance.api.updateRiskParams(requestParamUpdate)

        if (response.isSuccessful) {
            return true
        }
        return false

    }
}