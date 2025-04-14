package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model

data class PaymentToken(
    val firstName: String,
    val lastName: String,
    val matriculationNumber: String,
    val accountNumber: String,
    val balance: Double,
    val bankCode: String,
    val bankSecrets: List<String>,
    val date: String,
    val dailyTransactionCount: Int,
    val lastTransactionDate: String?,
    val highRiskAbortedCount: Int,
    var lastTransactionRiskValue: Int

)
