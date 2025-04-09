package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

sealed class TransactionOutcome {
    data object Success : TransactionOutcome()
    data object Rejection : TransactionOutcome()
}