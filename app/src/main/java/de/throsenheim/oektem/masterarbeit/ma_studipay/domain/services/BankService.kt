package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import kotlinx.coroutines.runBlocking

object BankService {


    fun assignBankService(
        userRepositoryImpl: UserRepositoryImpl,
        matriculationNumber: String,
        bank: Bank
    ): Boolean {
        return runBlocking {
            val user = userRepositoryImpl.getUserByImmatriculationNumber(matriculationNumber)
            if (user != null) {
                Log.d(
                    "BankService",
                    "Aktualisiere User ${user.matrikelnumber} mit bankCode: ${bank.bank_code}"
                )
                user.bank_code = bank.bank_code
                userRepositoryImpl.syncUserWithBackend(user)

            }
            true
        }

    }


}