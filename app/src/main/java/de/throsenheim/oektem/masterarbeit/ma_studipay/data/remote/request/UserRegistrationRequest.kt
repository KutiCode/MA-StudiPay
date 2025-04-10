package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request

data class UserRegistrationRequest(
    val matrikelnumber: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val accountNumber: String,
    val balance: Double,
    val securePin: String
)

