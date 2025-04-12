package de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao

import androidx.room.*
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankWithSecrets

/**
 * BankDao defines all database operations related to banks and their secrets.
 *
 * It provides methods to insert banks and secrets, delete all records,
 * and retrieve bank details along with their associated secrets.
 */
@Dao
interface BankDao {

    /**
     * Inserts a Bank entity into the "banks" table.
     *
     * Uses REPLACE as conflict strategy to update an existing record if needed.
     *
     * @param bank The Bank object to insert.
     * @return The row ID of the newly inserted bank.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: Bank): Long

    /**
     * Inserts a BankSecrets entity into the "bank_secrets" table.
     *
     * Uses REPLACE as conflict strategy to ensure that secrets are updated if the same key exists.
     *
     * @param secret The BankSecrets object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecret(secret: BankSecrets)

    /**
     * Deletes all records from the "banks" table.
     */
    @Query("DELETE FROM banks")
    suspend fun deleteAllBanks()

    /**
     * Deletes all records from the "bank_secrets" table.
     */
    @Query("DELETE FROM bank_secrets")
    suspend fun deleteAllSecrets()

    /**
     * Retrieves a list of all banks along with their associated secrets.
     *
     * Uses a Room @Transaction to ensure that the relationship between banks and their secrets
     * is maintained consistently.
     *
     * @return A list of BankWithSecrets objects.
     */
    @Transaction
    @Query("SELECT * FROM banks")
    suspend fun getAllBanksWithSecrets(): List<BankWithSecrets>

    /**
     * Retrieves a single Bank entity by its bank code.
     *
     * Limits the query to a single record.
     *
     * @param bank_code The unique code identifying the bank.
     * @return A Bank object if found, otherwise null.
     */
    @Query("SELECT * FROM banks WHERE bank_code = :bank_code LIMIT 1")
    suspend fun getBankByCode(bank_code: String): Bank?

    /**
     * Retrieves a Bank entity along with its secrets by its bank code.
     *
     * Uses a Room @Transaction to fetch the associated secrets.
     *
     * @param bank_code The unique code identifying the bank.
     * @return A BankWithSecrets object if found, otherwise null.
     */
    @Transaction
    @Query("SELECT * FROM banks WHERE bank_code = :bank_code")
    suspend fun getBankWithSecrets(bank_code: String): BankWithSecrets?

    /**
     * Retrieves the bank's details by bank code.
     *
     * This method is similar to getBankByCode, but may be used specifically for obtaining the bank name.
     *
     * @param bank_code The unique code identifying the bank.
     * @return A Bank object if found, otherwise null.
     */
    @Query("SELECT * FROM banks WHERE bank_code = :bank_code")
    suspend fun getBankNameByCode(bank_code: String): Bank?
}
