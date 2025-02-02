package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LastTransactionsViewModel : ViewModel() {

    private val _navigateToOrangeDetails = MutableLiveData<Boolean>()
    val navigateToOrangeDetails: LiveData<Boolean> get() = _navigateToOrangeDetails

    fun onOrangeDetailsClicked() {
        _navigateToOrangeDetails.value = true
    }

    fun onNavigatedToOrangeDetails() {
        _navigateToOrangeDetails.value = false
    }
}