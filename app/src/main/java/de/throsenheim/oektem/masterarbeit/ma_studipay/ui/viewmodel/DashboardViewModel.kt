package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.dashboard.DashboardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                matrikelNumber = user.matrikelnumber
            )
        } else {
            DashboardUiState(
                firstName = "Unbekannt",
                balance = "0 €",
                matrikelNumber = "Nicht verfügbar"
            )
        }
    }

    /**
     * Loads user data by matriculation number, first displaying local data and then
     * updating the state after attempting backend synchronization.
     *
     * @param matrikelnummer The matriculation number of the user.
     */
    fun loadUserData(matrikelnummer: String) {
        viewModelScope.launch {
            // Fetch local user data on the IO dispatcher
            val localUser = withContext(Dispatchers.IO) {
                userRepositoryImpl.getUserByImmatriculationNumber(matrikelnummer)
            }
            _userData.value = mapUserToDashboardUiState(localUser)

            // Attempt backend synchronization on the IO dispatcher
            try {
                withContext(Dispatchers.IO) {
                    bankRepositoryImpl.syncBanksFromBackend()
                    userRepositoryImpl.syncDatabase()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Backend sync failed: ${e.message}")
            }

            // Fetch updated user data after synchronization
            val updatedUser = withContext(Dispatchers.IO) {
                userRepositoryImpl.getUserByImmatriculationNumber(matrikelnummer)
            }
            _userData.value = mapUserToDashboardUiState(updatedUser)
        }
    }
}
