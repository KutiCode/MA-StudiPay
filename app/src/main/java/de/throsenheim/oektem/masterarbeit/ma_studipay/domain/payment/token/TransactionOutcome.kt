package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

sealed class TransactionOutcome {
    object Success : TransactionOutcome()
    object Rejection : TransactionOutcome()
}