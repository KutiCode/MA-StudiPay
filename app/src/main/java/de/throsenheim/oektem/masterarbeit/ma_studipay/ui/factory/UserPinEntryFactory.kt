package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserPinEntryViewModel

class UserPinEntryFactory(private val userRepositoryImpl: UserRepositoryImpl) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserPinEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserPinEntryViewModel(userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
