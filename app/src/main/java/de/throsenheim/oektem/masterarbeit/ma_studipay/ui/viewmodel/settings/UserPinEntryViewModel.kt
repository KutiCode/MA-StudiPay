package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import kotlinx.coroutines.launch

// ViewModel class for handling user PIN entry and verification in the settings UI.
class UserPinEntryViewModel(private val userRepositoryImpl: UserRepositoryImpl) : ViewModel() {

    // MutableLiveData to keep track of the current PIN value.
    private val _pin = MutableLiveData<String>()

    // Public immutable LiveData to observe the PIN value.
    val pin: LiveData<String> get() = _pin

    // Initialization block that sets the initial value of the PIN to an empty string.
    init {
        _pin.value = ""
    }

    /**
     * Updates the secure PIN for a user.
     *
     * @param context Context used to access shared preferences and display Toast messages.
     * @param matriculationNumber The unique identifier for the user.
     * @param newPin The new PIN to be stored.
     */
    fun updateSecurePin(context: Context, matriculationNumber: String, newPin: String) {
        viewModelScope.launch {
            // Update the secure PIN using the repository implementation.
            userRepositoryImpl.updateSecurePin(matriculationNumber, newPin)
            // Access shared preferences to store the new PIN locally.
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("secure_pin", newPin)
                apply()  // Save the changes asynchronously.
            }
        }
        Log.d("UserPinEntryVM", "Secure Pin is updated successfully")
    }

    /**
     * Verifies the entered secure PIN against the stored PIN.
     *
     * @param context Context used for displaying Toast messages.
     * @param navController NavController used to navigate between fragments upon successful PIN verification.
     * @param matriculationNumber The unique identifier for the user.
     * @param pin The PIN entered by the user for verification.
     */
    fun verifySecurePin(
        context: Context,
        navController: NavController,
        matriculationNumber: String,
        pin: String
    ) {
        viewModelScope.launch {
            // Retrieve the stored PIN from the repository.
            val storedPin = userRepositoryImpl.getSecurePin(matriculationNumber)
            if (storedPin == pin) {
                // If the entered PIN matches, build navigation options with animations.
                val navOptions = NavigationHelper.buildFadeNavOptions()
                // Navigate to the beginningSendingFragment with the specified animations.
                navController.navigate(R.id.beginningSendingFragment, null, navOptions)
                Log.d("UserPinEntryVM", "User entered correct secure pin")
            } else {
                // Show a toast message to the user if the PIN does not match.
                Toast.makeText(context, "Falsche Pin Eingabe", Toast.LENGTH_SHORT).show()

                Log.d("UserPinEntryVM", "User entered wrong secure pin")
            }
        }
    }
}
