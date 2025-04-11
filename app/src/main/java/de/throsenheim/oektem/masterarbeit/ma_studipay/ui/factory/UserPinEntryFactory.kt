package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserPinEntryViewModel

// Factory class for creating instances of UserPinEntryViewModel with the required UserRepositoryImpl dependency.
class UserPinEntryFactory(private val userRepositoryImpl: UserRepositoryImpl) :
    ViewModelProvider.Factory {

    /**
     * Create a new instance of the given ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return An instance of the ViewModel.
     * @throws IllegalArgumentException if the given modelClass is not assignable to UserPinEntryViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel is an instance of UserPinEntryViewModel.
        if (modelClass.isAssignableFrom(UserPinEntryViewModel::class.java)) {
            // Suppress unchecked cast warning as we know the type is correct and return the instance.
            @Suppress("UNCHECKED_CAST")
            return UserPinEntryViewModel(userRepositoryImpl) as T
        }
        // If the ViewModel type is unknown, throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
