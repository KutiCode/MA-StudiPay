package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.LoginViewModel

/**
 * Factory for creating instances of [LoginViewModel].
 *
 * This factory provides the necessary dependencies (Application and UserRepository)
 * to the LoginViewModel.
 *
 * @property application The application instance.
 * @property userRepositoryImpl The repository that handles user-related operations.
 */
class LoginFactory(
    private val application: Application,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the specified [ViewModel] class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A new instance of [LoginViewModel].
     * @throws IllegalArgumentException if the ViewModel class is not [LoginViewModel].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(application, userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
