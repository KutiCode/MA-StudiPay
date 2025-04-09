package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token

import androidx.lifecycle.MutableLiveData

enum class TransactionStatus {
    FINISHED,
    RESET
}

object TransactionStatusHolder {
    val transactionStatus = MutableLiveData<TransactionStatus>()


    fun setTransactionStatus() {
        transactionStatus.postValue(TransactionStatus.FINISHED)
    }
    fun reset() {
        transactionStatus.postValue(TransactionStatus.RESET)
    }
}
