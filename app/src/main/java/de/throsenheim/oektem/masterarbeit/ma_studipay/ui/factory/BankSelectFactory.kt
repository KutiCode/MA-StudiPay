package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankSelectViewModel

// Factory to create instances of BankSelectViewModel with the required dependencies.
class BankSelectFactory(
    // Application context used for operations like accessing SharedPreferences.
    private val context: Context,
    // The repository needed to perform user-related operations and assign banks.
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass the class type of the requested ViewModel.
     * @return an instance of BankSelectViewModel.
     * @throws IllegalArgumentException if the requested ViewModel is not BankSelectViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is assignable from BankSelectViewModel.
        if (modelClass.isAssignableFrom(BankSelectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Return a new instance of BankSelectViewModel with the provided context and repository.
            return BankSelectViewModel(context, userRepositoryImpl) as T
        }
        // Throw an exception if the ViewModel class is unknown.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
