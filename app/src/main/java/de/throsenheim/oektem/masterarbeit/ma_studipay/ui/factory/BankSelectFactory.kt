package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankSelectViewModel

class BankSelectFactory(

    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankSelectViewModel(Application(), userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}