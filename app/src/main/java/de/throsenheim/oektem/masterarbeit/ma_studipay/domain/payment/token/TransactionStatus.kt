package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

import androidx.lifecycle.MutableLiveData

// Enum defining possible statuses of a transaction.
enum class TransactionStatus {
    FINISHED,  // Indicates that the transaction has been completed.
    RESET      // Indicates that the transaction status has been reset.
}

// TransactionStatusHolder acts as a global holder for the transaction status,
// using LiveData to allow observers to react to status changes.
object TransactionStatusHolder {
    // MutableLiveData holding the current TransactionStatus.
    // Observers (e.g., UI components) can monitor this LiveData for updates.
    val transactionStatus = MutableLiveData<TransactionStatus>()

    /**
     * Sets the transaction status to FINISHED.
     * This method posts the FINISHED value to the LiveData.
     */
    fun setTransactionStatus() {
        transactionStatus.postValue(TransactionStatus.FINISHED)
    }

    /**
     * Resets the transaction status by posting the RESET value to the LiveData.
     */
    fun reset() {
        transactionStatus.postValue(TransactionStatus.RESET)
    }
}
