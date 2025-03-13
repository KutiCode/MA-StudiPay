package de.throsenheim.oektem.masterarbeit.ma_studipay.service

data class RiskValueUpdateRequest(
    val matrikelnumber: String,
    val dailyTransactionCount: Int,
    val lastTransactionDate: String,
    val highRiskAbortedCount: Int,
    val lastTransactionRiskValue: Int
)

