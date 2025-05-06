package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

// ViewModel for managing user settings-related data, such as the user name and logout functionality.
class SettingsViewModel : ViewModel() {

    // Private mutable LiveData to store the user's name.
    private val _userName = MutableLiveData<String>()

    // Public immutable LiveData for observing the user name from the UI.
    val userName: LiveData<String> get() = _userName

    /**
     * Loads the user's name based on their matriculation number.
     *
     * Uses a coroutine launched in the viewModelScope to perform the operation asynchronously.
     * The loaded user's full name is set in the LiveData or, if user data is missing, a default greeting is used.
     *
     * @param context The context used to access resources and storage.
     */
    fun loadUserName(context: Context) {
        viewModelScope.launch {
            // Retrieve the user from persistent storage using a helper function.
            val user = UiHelper.loadUser(context)
            // Update LiveData with the user's full name if available, otherwise show a default greeting.
            _userName.value = user?.let { "${it.firstName} ${it.lastName}" } ?: "Hallo, Benutzer"
        }
    }

    /**
     * Logs out the user by clearing the stored user preferences.
     *
     * This method clears all stored shared preferences associated with "user_prefs".
     *
     * @param context The context used to access shared preferences.
     */
    fun logoutUser(context: Context) {
        // Get the SharedPreferences using "user_prefs" as key.
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        // Clear all stored preferences and apply changes asynchronously.
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        Log.d("SettingsViewModel", "User logged out and preferences cleared.")
    }
}
