package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class BankConnectionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankConnectionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}