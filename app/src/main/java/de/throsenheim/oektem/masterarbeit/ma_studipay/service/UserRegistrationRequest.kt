package de.throsenheim.oektem.masterarbeit.ma_studipay.service

data class UserRegistrationRequest(
    val matrikelnumber: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val accountNumber: String,
    val balance: Double,
    val securePin: String
)

