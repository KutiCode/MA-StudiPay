package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.BankService
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
// ViewModel responsible for handling bank selection and updating the current user's bank details.
class BankSelectViewModel(
    // Application context used for accessing shared preferences.
    private val context: Context,
    // User repository for performing user-related operations, such as updating bank info.
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    /**
     * Assigns the selected bank to the current user.
     *
     * This method retrieves the current username from shared preferences, then calls the BankService
     * to update the user's bank connection in the repository. It logs whether the update was successful or not.
     *
     * @param bank The selected Bank object to assign to the user.
     */
    fun assignBankToCurrentUser(bank: Bank) {
        viewModelScope.launch {
            // Retrieve shared preferences where user information is stored.
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            // Fetch the current username (e.g., matriculation number) from shared preferences.
            val currentUsername = sharedPreferences.getString("current_username", null)
            // Proceed only if a valid username is retrieved.
            if (currentUsername != null) {
                // Call the BankService to assign the bank to the current user.
                if (BankService.assignBankService(
                        userRepositoryImpl,
                        currentUsername,
                        bank
                    )
                ) {
                    // Log a debug message on successful bank assignment.
                    Log.d("BankSelectVM", "User successfully updated with bank: ${bank.name}")
                } else {
                    // Log an error message if the bank assignment fails.
                    Log.e("BankSelectVM", "Failed to update user with bank: ${bank.name}")
                }
            }
        }
    }
}
