package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

// A sealed class representing the possible outcomes of a transaction.
sealed class TransactionOutcome {
    // Represents a successful transaction outcome.
    data object Success : TransactionOutcome()

    // Represents a transaction that has been rejected.
    data object Rejection : TransactionOutcome()
}
