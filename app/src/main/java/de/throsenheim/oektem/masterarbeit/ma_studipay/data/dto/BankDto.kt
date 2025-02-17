package de.throsenheim.oektem.masterarbeit.ma_studipay.data.dto

data class BankDto(
    val bank_name: String,
    val bank_code: String,
    val secrets: List<SecretDto>
)