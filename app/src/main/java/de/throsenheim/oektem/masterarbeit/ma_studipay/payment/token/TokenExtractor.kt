package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token

import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao.BankDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.*

object TokenExtractor {


    suspend fun extractTokenFromResponse(
        context: Context,
        amount: Int,
        paymentToken: PaymentToken
    ) {
        var riskValue = calculateRiskValue(context, amount, paymentToken)

        if (riskValue < 40) {
            // Transaktion hat kein Risiko. Sofortige Ausführung
            transactionCertificate(context, paymentToken, amount)
        } else if (riskValue < 90) {
            // Transkation hat mittleres Risiko. Bestätigung ist nötig.
            authorizationRequestCheck(context, paymentToken, amount)
        } else {
            // Transaktion hat hohes Risiko. Transaktion wird abgebrochen.
            transactionRejectionCertificate(context, paymentToken, amount)

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
        amount: Int,
        paymentToken: PaymentToken
    ): Int {

        var riskValue = 100

        if (isTokenRecent(paymentToken.date)) {
            riskValue -= 10
        }

        if (amount < 10) {
            riskValue -= 30
        } else if (amount < 30) {
            riskValue -= 20
        } else {
            riskValue -= 5
        }

        if (verifyBankSecret(context, paymentToken)) {
            riskValue -= 25
        }

        return riskValue

    }

    private suspend fun verifyBankSecret(context: Context, paymentToken: PaymentToken): Boolean {
        // Hole die lokale Bank (inklusive Secrets) anhand des BankCodes
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        val bankRepository = BankRepository(bankDao)
        val bankWithSecrets = bankRepository.getBankWithSecrets(paymentToken.bankCode)

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
        amount: Int
    ) {
        if (paymentToken.balance < amount) {
            transactionRejectionCertificate(context, paymentToken, amount)
        } else {
            // In dieser Funktion werden die nötigen Informationen und schritte durchgeführt, um die Transaktion zu bestätigen.
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val matrikelNumber = sharedPref.getString("current_username", null)
            val userDao = AppDatabase.getDatabase(context).userDao()
            val requestAdd = BalanceUpdateRequest(matrikelNumber!!, amount.toDouble())
            val requestDeduct = BalanceUpdateRequest(paymentToken.matrikelNumber, amount.toDouble())

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
        }

    }


    private suspend fun authorizationRequestCheck(
        context: Context,
        paymentToken: PaymentToken,
        amount: Int
    ) {

    }

    private suspend fun transactionRejectionCertificate(
        context: Context,
        paymentToken: PaymentToken,
        amount: Int
    ) {
        // In dieser Funktion wird die Transaktion abgebrochen.
    }
}