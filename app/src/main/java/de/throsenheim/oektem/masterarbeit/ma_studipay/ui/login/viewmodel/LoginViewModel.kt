package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val userRepository: UserRepository) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun login(matrikelnummer: String, password: String) {
        if (matrikelnummer.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte alle Felder ausfüllen"
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserByMatrikelnumber(matrikelnummer)
            if (user != null && user.password == hashPassword(password)) {
               saveUserToPreferences(matrikelnummer)
                _loginResult.value = true // Login erfolgreich
            } else {
                _errorMessage.value = "Ungültige Login-Daten"
            }
        }
    }



    private fun hashPassword(password: String): String {
        // Beispiel für eine Hashing-Methode, ersetzen durch eine echte Implementierung
        return password.hashCode().toString()
    }
    fun saveUserToPreferences( matrikelnummer: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("current_username", matrikelnummer)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

}
