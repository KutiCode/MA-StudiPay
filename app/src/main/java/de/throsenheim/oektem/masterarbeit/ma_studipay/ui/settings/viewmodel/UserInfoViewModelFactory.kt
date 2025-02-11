package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

class UserInfoViewModelFactory(
    private val userRepository: UserRepository,
    private val bankRepository: BankRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserInfoViewModel(userRepository, bankRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}