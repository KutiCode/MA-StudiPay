package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

class LoginViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(application, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

