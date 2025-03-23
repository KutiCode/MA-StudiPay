package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
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
     * @param matrikelnumber The matriculation number of the user.
     */
    fun loadUserDetails(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            // Obtain the UserDao instance from the AppDatabase.
            val userDao = AppDatabase.getDatabase(context).userDao()
            // Retrieve the user by their matriculation number.
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            // Update the LiveData with the retrieved user.
            _userDetails.value = user
        }
    }
}
