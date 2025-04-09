package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token


import android.content.Context
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.*

object TokenGenerator {


    suspend fun generateToken(context: Context): PaymentToken {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matriculationNumber = sharedPref.getString("current_username", null)
            ?: throw Exception("Keine Matrikelnummer in SharedPreferences gefunden")


        val userApi = RetrofitInstance.api
        val userDao = AppDatabase.getDatabase(context).userDao()
        val userRepositoryImpl = UserRepositoryImpl(
            userDao,
            userApi,
            context
        )
        val user = userRepositoryImpl.getUserByMatriculationNumber(matriculationNumber)
            ?: throw Exception("User mit Matrikelnummer $matriculationNumber nicht gefunden")


        val bankDao = AppDatabase.getDatabase(context).bankDao()
        val bankRepositoryImpl = BankRepositoryImpl(bankDao)

        val bankWithSecrets = bankRepositoryImpl.getBankWithSecrets(user.bank_code ?: "")
        val bankSecret = bankWithSecrets?.secrets?.firstOrNull()?.secretCode
            ?: throw Exception("Kein BankSecret für Bank-Code ${user.bank_code} gefunden")


        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = formatter.format(currentDate)


        return PaymentToken(
            firstName = user.firstName,
            lastName = user.lastName,
            matriculationNumber = user.matriculationNumber,
            accountNumber = user.accountNumber,
            balance = user.balance,
            bankCode = user.bank_code ?: "",
            bankSecret = bankSecret,
            date = dateString,
            dailyTransactionCount = user.dailyTransactionCount,
            lastTransactionDate = user.lastTransactionDate,
            highRiskAbortedCount = user.highRiskAbortedCount,
            lastTransactionRiskValue = user.lastTransaktionRiskValue

        )
    }
}
