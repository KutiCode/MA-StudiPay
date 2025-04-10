package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.carddetails

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

/**
 * OrangeDetailsViewModel is responsible for loading user details for the orange card details screen.
 */
class OrangeDetailsViewModel : ViewModel() {

    // Private MutableLiveData that holds the user details.
    private val _userDetails = MutableLiveData<User?>()

    // Public LiveData to expose the user details.
    val userDetails: LiveData<User?> get() = _userDetails

    /**
     * Loads user details based on the given matriculation number.
     *
     * This method retrieves user information from the local database using the UserDao.
     *
     * @param context The context required to access the database.
     * @param matriculationNumber The matriculation number of the user.
     */
    fun loadUserDetails(context: Context, matriculationNumber: String) {
        viewModelScope.launch {

            _userDetails.value = UiHelper.loadUser(context, matriculationNumber)
        }
        Log.d("OrangeDetailsViewModel", "User details loaded: ${_userDetails.value}")
    }
}
