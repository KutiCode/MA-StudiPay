package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

/**
 * Factory class for creating instances of DashboardViewModel.
 *
 * This factory provides the required dependencies (bankRepository and userRepository)
 * to the DashboardViewModel.
 *
 * @property bankRepository The repository handling bank-related operations.
 * @property userRepository The repository handling user-related operations.
 */
class DashboardViewModelFactory(
    private val bankRepository: BankRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of DashboardViewModel.
     * @throws IllegalArgumentException if the ViewModel class is not DashboardViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(bankRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
