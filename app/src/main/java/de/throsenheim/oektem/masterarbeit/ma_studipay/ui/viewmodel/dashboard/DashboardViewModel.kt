package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.dashboard.DashboardUiState
import kotlinx.coroutines.launch

/**
 * DashboardViewModel connects the UI with the underlying data.
 *
 * It loads and synchronizes user data, then updates the UI state.
 */
class DashboardViewModel(
    private val bankRepositoryImpl: BankRepositoryImpl,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {

    private val _userData = MutableLiveData<DashboardUiState>()
    val userData: LiveData<DashboardUiState> get() = _userData

    /**
     * Helper function to map a user object to DashboardUiState.
     */
    private fun mapUserToDashboardUiState(user: User?): DashboardUiState {
        return if (user != null) {
            DashboardUiState(
                firstName = user.firstName,
                balance = "${user.balance} €",
                matriculationNumber = user.matriculationNumber
            )
        } else {
            DashboardUiState(
                firstName = "Unbekannt",
                balance = "0 €",
                matriculationNumber = "Nicht verfügbar"
            )
        }
    }

    /**
     * Loads user data by matriculation number, first displaying local data and then
     * updating the state after attempting backend synchronization.
     *
     * @param matriculationNumber The matriculation number of the user.
     */
    fun loadUserData(matriculationNumber: String) {
        viewModelScope.launch {
            val updatedUser =
                UiHelper.userUpdater(matriculationNumber, bankRepositoryImpl, userRepositoryImpl)
            _userData.value = mapUserToDashboardUiState(updatedUser)

            Log.d("DashboardViewModel", "User data loaded: $updatedUser")
        }
    }

    fun pinManagement(context: Context, navController: NavController) {
        viewModelScope.launch {
            val user = UiHelper.loadUser(context) // Replace with actual matriculation number

            val securePin = user?.securePin
            val navOptions = NavigationHelper.buildFadeNavOptions()
            if (securePin == null) {
                Log.d("DashboardViewModel", "Secure Pin is not set,navigating to PIN change")
                val args = Bundle().apply {
                    putBoolean("isChangePin", true)
                }
                navController.navigate(
                    R.id.userPinEntryFragment, args,
                    navOptions
                )

            } else {
                val args = Bundle().apply {
                    putBoolean("isChangePin", false)
                }
                navController.navigate(
                    R.id.userPinEntryFragment, args,
                    navOptions
                )
                Log.d("DashboardViewModel", "Secure Pin is set")

            }


        }
    }
}
