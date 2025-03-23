package de.throsenheim.oektem.masterarbeit.ma_studipay.data.local.dao

import androidx.room.*
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.BankSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.BankWithSecrets

@Dao
interface BankDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: Bank): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecret(secret: BankSecrets)

    @Query("DELETE FROM banks")
    suspend fun deleteAllBanks()

    @Query("DELETE FROM bank_secrets")
    suspend fun deleteAllSecrets()

    @Transaction
    @Query("SELECT * FROM banks")
    suspend fun getAllBanksWithSecrets(): List<BankWithSecrets>

    @Query("SELECT * FROM banks WHERE bank_code = :bank_code LIMIT 1")
    suspend fun getBankByCode(bank_code: String): Bank?

    @Transaction
    @Query("SELECT * FROM banks WHERE bank_code = :bank_code")
    suspend fun getBankWithSecrets(bank_code: String): BankWithSecrets?

    @Query("SELECT * FROM banks WHERE bank_code = :bank_code")
    suspend fun getBankNameByCode(bank_code: String): Bank?

}

