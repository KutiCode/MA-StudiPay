package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

// ViewModel for the beginning of a sending process.
// It is responsible for loading and exposing the user's full name.
class BeginningSendingViewModel : ViewModel() {

    // Private mutable LiveData that holds the user's full name.
    private val _userName = MutableLiveData<String>()

    // Public immutable LiveData to expose the userName data to the UI.
    val userName: LiveData<String> get() = _userName

    /**
     * Loads the user's full name using the provided matriculation number.
     *
     * Launches a coroutine in the viewModelScope to fetch the user details asynchronously.
     * If the user data is retrieved successfully, it sets the value to a concatenation of
     * the user's firstName and lastName. If no user is found, it sets a default greeting.
     *
     * @param context The context used to access resources and data storage.
     * @param matriculationNumber The unique identifier for the user.
     */
    fun loadUserName(context: Context) {
        viewModelScope.launch {
            // Load the user from storage using a helper function.
            val user = UiHelper.loadUser(context)
            // Update LiveData with the user's full name or a default greeting if null.
            _userName.value = user?.let { "${it.firstName} ${it.lastName}" } ?: "Hallo, Benutzer"
        }
    }
}
