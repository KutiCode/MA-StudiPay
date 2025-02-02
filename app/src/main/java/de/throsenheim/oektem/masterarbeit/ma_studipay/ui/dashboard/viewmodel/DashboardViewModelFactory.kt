package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

class DashboardViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
