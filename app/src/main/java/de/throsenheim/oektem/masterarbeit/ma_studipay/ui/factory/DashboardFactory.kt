package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.DashboardViewModel

/**
 * Factory class for creating instances of [DashboardViewModel].
 *
 * This factory provides the required dependencies (bankRepository and userRepository)
 * to the DashboardViewModel.
 *
 * @property bankRepositoryImpl The repository handling bank-related operations.
 * @property userRepositoryImpl The repository handling user-related operations.
 */
class DashboardFactory(
    private val bankRepositoryImpl: BankRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of DashboardViewModel.
     * @throws IllegalArgumentException if the ViewModel class is not [DashboardViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(bankRepositoryImpl, userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
