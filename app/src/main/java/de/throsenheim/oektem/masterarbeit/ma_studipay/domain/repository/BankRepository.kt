package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankWithSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank

/**
 * BankRepository defines the operations related to bank data.
 *
 * It provides methods to:
 *  - Retrieve a bank by its bank code.
 *  - Synchronize bank data from the backend.
 *  - Retrieve a bank's detailed information (including secrets) by bank code.
 */
interface BankRepository {

    /**
     * Retrieves a basic Bank object using the provided bank code.
     *
     * @param bankCode The unique code identifying the bank.
     * @return A Bank object if found, or null if no matching bank exists.
     */
    suspend fun getBankByCode(bankCode: String): Bank?

    /**
     * Synchronizes bank data with the backend.
     *
     * This method is used to update the local repository of banks with the latest data
     * from a remote source.
     */
    suspend fun syncBanksFromBackend()

    /**
     * Retrieves a BankWithSecrets object for the bank identified by the given bank code.
     *
     * BankWithSecrets likely includes sensitive or additional data
     * that is not exposed by the basic Bank object.
     *
     * @param bankCode The unique code of the bank.
     * @return A BankWithSecrets object if available, otherwise null.
     */
    suspend fun getBankWithSecrets(bankCode: String): BankWithSecrets?
}
