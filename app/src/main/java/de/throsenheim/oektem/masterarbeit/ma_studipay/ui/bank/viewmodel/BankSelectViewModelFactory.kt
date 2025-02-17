package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel.BankSelectViewModel

class BankSelectViewModelFactory(
    private val context: Context,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankSelectViewModel(context, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}