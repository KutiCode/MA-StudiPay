package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.BankDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.dto.BankResponseDto
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.BankWithSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BankRepositoryImpl(private val bankDao: BankDao): BankRepository{

    override suspend fun getBankByCode(bankCode: String): Bank? {
        return bankDao.getBankByCode(bankCode)
    }

    override suspend fun syncBanksFromBackend() {
        withContext(Dispatchers.IO) {
            try {
                val response: BankResponseDto = RetrofitInstance.api.getAllBankSecrets()
                Log.d("BankRepository", "Backend response: $response")

                // Bestehende Daten löschen (optional)
                bankDao.deleteAllSecrets()
                bankDao.deleteAllBanks()

                // Iteriere über alle Banken im Response
                response.banks.forEach { bankDto ->
                    val bankEntity = Bank(
                        name = bankDto.bank_name,
                        bank_code = bankDto.bank_code
                    )
                    val bankId = bankDao.insertBank(bankEntity)
                    Log.d(
                        "BankRepository",
                        "Inserted Bank with bankCode: ${bankDto.bank_code} (id: $bankId)"
                    )

                    bankDto.secrets.forEach { secretDto ->
                        val secretEntity = BankSecrets(
                            bank_code = bankDto.bank_code,
                            secretCode = secretDto.code,
                            generatedAt = secretDto.generated_at
                        )
                        bankDao.insertSecret(secretEntity)
                        Log.d(
                            "BankRepository",
                            "Inserted Secret: ${secretDto.code} for bankCode: ${bankDto.bank_code}"
                        )
                    }
                }
                // Überprüfe, wie viele Banken in der lokalen DB vorhanden sind:
                val banksLocal = bankDao.getAllBanksWithSecrets()
                Log.d("BankRepository", "Local banks count: ${banksLocal.size}")
            } catch (e: Exception) {
                Log.e("BankRepository", "Exception during bank sync", e)
            }
        }
    }

    override suspend fun getBankWithSecrets(bankCode: String): BankWithSecrets? {
        return bankDao.getBankWithSecrets(bankCode)
    }

}
