package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _userData = MutableLiveData<DashboardUiState>()
    val userData: LiveData<DashboardUiState> get() = _userData

    fun loadUserData(matrikelnummer: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByMatrikelnumber(matrikelnummer)
            if (user != null) {
                _userData.value = DashboardUiState(
                    firstName = user.firstName,
                    balance = "${user.balance} €",
                    matrikelNumber = user.matrikelnumber
                )
            } else {
                _userData.value = DashboardUiState(
                    firstName = "Unbekannt",
                    balance = "0 €",
                    matrikelNumber = "Nicht verfügbar"
                )
            }
        }
    }
}

data class DashboardUiState(
    val firstName: String,
    val balance: String,
    val matrikelNumber: String
)
