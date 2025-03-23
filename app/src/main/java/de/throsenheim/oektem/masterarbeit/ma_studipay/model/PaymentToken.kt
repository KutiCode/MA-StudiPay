package de.throsenheim.oektem.masterarbeit.ma_studipay.model

data class PaymentToken(
    val firstName: String,
    val lastName: String,
    val matrikelNumber: String,
    val accountNumber: String,
    val balance: Double,
    val bankCode: String,
    val bankSecret: String,
    val date: String,
    val dailyTransactionCount: Int,
    val lastTransactionDate: String?,
    val highRiskAbortedCount: Int,
    var lastTransactionRiskValue: Int

)
