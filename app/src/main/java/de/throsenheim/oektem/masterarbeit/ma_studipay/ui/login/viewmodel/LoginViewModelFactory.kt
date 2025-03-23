package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

/**
 * Factory for creating instances of [LoginViewModel].
 *
 * This factory provides the necessary dependencies (Application and UserRepository)
 * to the LoginViewModel.
 *
 * @property application The application instance.
 * @property userRepository The repository that handles user-related operations.
 */
class LoginViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository
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
            return LoginViewModel(application, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
