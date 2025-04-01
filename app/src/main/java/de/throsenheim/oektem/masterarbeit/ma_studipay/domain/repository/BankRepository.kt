package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.repository

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.BankWithSecrets
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank

interface BankRepository{
    suspend fun getBankByCode(bankCode: String): Bank?

    suspend fun syncBanksFromBackend()

    suspend fun getBankWithSecrets(bankCode: String): BankWithSecrets?
}