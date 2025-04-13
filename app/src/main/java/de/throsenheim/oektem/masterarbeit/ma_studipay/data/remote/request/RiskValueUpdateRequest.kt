package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request

data class RiskValueUpdateRequest(
    val matriculationNumber: String,
    val dailyTransactionCount: Int,
    val lastTransactionDate: String,
    val highRiskAbortedCount: Int,
    val lastTransactionRiskValue: Int
)

