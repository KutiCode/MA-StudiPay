package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankConnectionViewModel

// Factory for creating an instance of BankConnectionViewModel with the required context.
class BankConnectionFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass The class of the ViewModel to be created.
     * @return A new instance of BankConnectionViewModel.
     * @throws IllegalArgumentException if the ViewModel class is not recognized.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel type is assignable from BankConnectionViewModel.
        if (modelClass.isAssignableFrom(BankConnectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Return a new instance of BankConnectionViewModel, passing in the context dependency.
            return BankConnectionViewModel(context) as T
        }
        // Throw an exception if the modelClass is not recognized.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
