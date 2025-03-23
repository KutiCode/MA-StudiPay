package de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.model.BankWithSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.Bank

interface BankRepository{
    suspend fun getBankByCode(bankCode: String): Bank?

    suspend fun syncBanksFromBackend()

    suspend fun getBankWithSecrets(bankCode: String): BankWithSecrets?
}