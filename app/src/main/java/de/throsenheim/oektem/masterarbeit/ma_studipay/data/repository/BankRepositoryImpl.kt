package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao.BankDao
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.dto.BankResponseDto
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankWithSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository.BankRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BankRepositoryImpl provides a concrete implementation for BankRepository.
 *
 * It is responsible for retrieving bank data from the local database,
 * synchronizing bank details from the backend, and retrieving bank secrets.
 */
class BankRepositoryImpl(private val bankDao: BankDao) : BankRepository {

    /**
     * Retrieves a Bank object by its bank code from the local database.
     *
     * @param bankCode The unique bank code.
     * @return A Bank object if found, or null if not.
     */
    override suspend fun getBankByCode(bankCode: String): Bank? {
        return bankDao.getBankByCode(bankCode)
    }

    /**
     * Synchronizes bank data from the backend.
     *
     * This method fetches the list of banks and their secrets from the backend API,
     * clears the current local bank and secret data, and then populates the database
     * with the updated information.
     */
    override suspend fun syncBanksFromBackend() {
        withContext(Dispatchers.IO) {
            try {
                // Retrieve bank secrets from the backend
                val response: BankResponseDto = RetrofitInstance.api.getAllBankSecrets()
                Log.d("BankRepository", "Backend response: $response")

                // Clear existing local bank and secret data.
                bankDao.deleteAllSecrets()
                bankDao.deleteAllBanks()

                // For each bank in the response, insert the bank and its corresponding secrets.
                response.banks.forEach { bankDto ->
                    // Create a Bank entity from the DTO.
                    val bankEntity = Bank(
                        name = bankDto.bank_name,
                        bank_code = bankDto.bank_code
                    )
                    // Insert the Bank entity and get its generated ID.
                    val bankId = bankDao.insertBank(bankEntity)
                    Log.d(
                        "BankRepository",
                        "Inserted Bank with bankCode: ${bankDto.bank_code} (id: $bankId)"
                    )

                    // For each secret associated with the bank, create and insert a BankSecrets entity.
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
                // Log the count of local banks after synchronization.
                val banksLocal = bankDao.getAllBanksWithSecrets()
                Log.d("BankRepository", "Local banks count: ${banksLocal.size}")
            } catch (e: Exception) {
                Log.e("BankRepository", "Exception during bank sync", e)
            }
        }
    }

    /**
     * Retrieves detailed bank information, including sensitive secrets, by bank code.
     *
     * @param bankCode The unique bank code.
     * @return A BankWithSecrets object if available; otherwise, null.
     */
    override suspend fun getBankWithSecrets(bankCode: String): BankWithSecrets? {
        return bankDao.getBankWithSecrets(bankCode)
    }
}
