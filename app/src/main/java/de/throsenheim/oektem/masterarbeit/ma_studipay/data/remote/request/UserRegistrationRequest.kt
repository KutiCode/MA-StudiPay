package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request

data class UserRegistrationRequest(
    val matriculationNumber: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val accountNumber: String,
    val balance: Double
)

