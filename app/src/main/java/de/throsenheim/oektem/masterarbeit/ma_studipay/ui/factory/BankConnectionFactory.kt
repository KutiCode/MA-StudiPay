package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankConnectionViewModel


class BankConnectionFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankConnectionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}