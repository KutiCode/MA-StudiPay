package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.register.RegisterViewModel

// Factory for creating instances of RegisterViewModel with the required UserRepositoryImpl dependency.
class RegisterFactory(
    // The repository instance needed to perform registration operations in the ViewModel.
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass The class of the ViewModel that is requested.
     * @return An instance of RegisterViewModel.
     * @throws IllegalArgumentException if the ViewModel class is not supported by this factory.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel is of type RegisterViewModel.
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            // Return a new instance of RegisterViewModel with the provided UserRepositoryImpl dependency.
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(userRepositoryImpl) as T
        }
        // Throw an exception if an unsupported ViewModel class is requested.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
