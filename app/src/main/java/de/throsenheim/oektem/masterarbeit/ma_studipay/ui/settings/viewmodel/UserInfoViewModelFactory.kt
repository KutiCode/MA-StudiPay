package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl

class UserInfoViewModelFactory(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val bankRepositoryImpl: BankRepositoryImpl
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserInfoViewModel(userRepositoryImpl, bankRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}