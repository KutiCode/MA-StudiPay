package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.DashboardUiState
import kotlinx.coroutines.launch


class DashboardViewModel(
    private val bankRepository: BankRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userData = MutableLiveData<DashboardUiState>()
    val userData: LiveData<DashboardUiState> get() = _userData

    fun loadUserData(matrikelnummer: String) {
        viewModelScope.launch {
            // Lade zunächst die lokalen Daten und zeige sie an.
            val localUser = userRepository.getUserByMatrikelnumber(matrikelnummer)
            if (localUser != null) {
                _userData.value = DashboardUiState(
                    firstName = localUser.firstName,
                    balance = "${localUser.balance} €",
                    matrikelNumber = localUser.matrikelnumber
                )
            } else {
                _userData.value = DashboardUiState(
                    firstName = "Unbekannt",
                    balance = "0 €",
                    matrikelNumber = "Nicht verfügbar"
                )
            }

            // Versuche nun, im Hintergrund die Backend-Synchronisation durchzuführen.
            try {
                bankRepository.syncBanksFromBackend()
                userRepository.syncDatabase()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Backend sync failed: ${e.message}")
            }

            // Nachdem die Synchronisation abgeschlossen ist, aktualisiere die UI ggf.
            val updatedUser = userRepository.getUserByMatrikelnumber(matrikelnummer)
            if (updatedUser != null) {
                _userData.value = DashboardUiState(
                    firstName = updatedUser.firstName,
                    balance = "${updatedUser.balance} €",
                    matrikelNumber = updatedUser.matrikelnumber
                )
            }
        }
    }
}



