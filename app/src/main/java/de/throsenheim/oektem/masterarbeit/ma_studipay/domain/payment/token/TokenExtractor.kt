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
                authorizationRequestCheck(context, paymentToken, amount)

            }

            else -> {
                transactionRejectionCertificate(paymentToken)
                TransactionOutcome.Rejection
            }
        }
    }


    private fun isTokenRecent(tokenDateString: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tokenDate: Date = sdf.parse(tokenDateString) ?: return false
        val currentTime = Date()
        val diffMillis = currentTime.time - tokenDate.time
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
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        val bankRepositoryImpl = BankRepositoryImpl(bankDao)
        val bankWithSecrets = bankRepositoryImpl.getBankWithSecrets(paymentToken.bankCode)

        if (bankWithSecrets == null || bankWithSecrets.secrets.isEmpty()) {
            return false
        }

        return bankWithSecrets.secrets.any { it.secretCode == paymentToken.bankSecret }
    }

    private suspend fun transactionCertificate(
        context: Context,
        paymentToken: PaymentToken,
        amount: Double
    ) {
        if (paymentToken.balance < amount) {
            transactionRejectionCertificate(paymentToken)
        } else {
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matriculationNumber = sharedPref.getString("current_username", null)
            val userDao = AppDatabase.getDatabase(context).userDao()
            val requestAdd = BalanceUpdateRequest(matriculationNumber!!, amount)
            val requestDeduct = BalanceUpdateRequest(paymentToken.matriculationNumber, amount)

            val responseAdd = RetrofitInstance.api.addBalance(requestAdd)
            if (responseAdd.isSuccessful) {
                val user = userDao.getUserByMatriculationNumber(matriculationNumber)
                user?.let {
                    it.balance += amount
                    userDao.updateUserBalance(matriculationNumber, it.balance)
                }
            } else {
                transactionRejectionCertificate(paymentToken)
            }

            val responseDeduct = RetrofitInstance.api.deductBalance(requestDeduct)
            if (responseDeduct.isSuccessful) {
                val userHold =
                    userDao.getUserByMatriculationNumber(paymentToken.matriculationNumber)
                val userHoldNewBalance = userHold?.balance?.minus(amount)
                if (userHold != null) {
                    userDao.updateUserBalance(userHold.matriculationNumber, userHoldNewBalance!!)
                }

            } else {
                transactionRejectionCertificate(paymentToken)
            }

            updateRiskParams(
                paymentToken.matriculationNumber,
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
        val requestAuth = TransactionVerificationRequest(paymentToken.matriculationNumber, amount)
        val responseAuth = RetrofitInstance.api.verifyTransaction(requestAuth)

        if (responseAuth.isSuccessful) {
            val riskParamsUpdated = updateRiskParams(
                paymentToken.matriculationNumber,
                paymentToken.dailyTransactionCount + 1,
                today,
                paymentToken.highRiskAbortedCount,
                paymentToken.lastTransactionRiskValue
            )
            if (riskParamsUpdated) {
                transactionCertificate(context, paymentToken, amount)
                return TransactionOutcome.Success
            } else {
                transactionRejectionCertificate(paymentToken)
                return TransactionOutcome.Rejection
            }


        } else {
            transactionRejectionCertificate(paymentToken)
            return TransactionOutcome.Rejection
        }
    }

    private suspend fun transactionRejectionCertificate(
        paymentToken: PaymentToken,
    ): TransactionOutcome {
        val requestRiskValueUpdate = RiskValueUpdateRequest(
            paymentToken.matriculationNumber,
            paymentToken.dailyTransactionCount + 1,
            today,
            paymentToken.highRiskAbortedCount + 1,
            paymentToken.lastTransactionRiskValue
        )
        RetrofitInstance.api.updateRiskParams(requestRiskValueUpdate)
        return TransactionOutcome.Rejection
    }

    private suspend fun updateRiskParams(
        
        matriculationNumber: String,
        dailyTransactionCount: Int,
        lastTransactionDate: String,
        highRiskAbortedCount: Int,
        lastTransactionRiskValue: Int
    ): Boolean {

        val requestParamUpdate = RiskValueUpdateRequest(
            matriculationNumber,
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