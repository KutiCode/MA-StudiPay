package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token

sealed class TransactionOutcome {
    object Success : TransactionOutcome()
    object Rejection : TransactionOutcome()
}