package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings.UserInfoViewModel

// Factory for creating UserInfoViewModel instances with the necessary BankRepositoryImpl dependency.
class UserInfoFactory(
    // The BankRepositoryImpl instance needed for UserInfoViewModel.
    private val bankRepositoryImpl: BankRepositoryImpl
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param modelClass The ViewModel class requested.
     * @return An instance of the requested ViewModel.
     * @throws IllegalArgumentException if the requested ViewModel is not UserInfoViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is assignable from UserInfoViewModel.
        if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Return a new instance of UserInfoViewModel, passing in the BankRepositoryImpl dependency.
            return UserInfoViewModel(bankRepositoryImpl) as T
        }
        // Throw an exception if the modelClass is not supported by this factory.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
