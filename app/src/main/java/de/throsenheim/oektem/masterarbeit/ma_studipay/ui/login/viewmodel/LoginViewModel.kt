package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val userRepository: UserRepository) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private var _errorCount: Int = 0


    fun login(matrikelnummer: String, password: String) {
        if (matrikelnummer.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte alle Felder ausfüllen"
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserByMatrikelnumber(matrikelnummer)

            if (user != null && verifyPassword(password, user.password)) {
                // Login erfolgreich
                saveUserToPreferences(matrikelnummer)
                _loginResult.value = true
                _errorCount = 0 // Fehlerzähler zurücksetzen
            } else {
                _errorCount++
                when (_errorCount) {
                    in 1..2 -> {
                        // Bei den ersten beiden Fehlversuchen:
                        // Versuche, die Daten mit syncDatabase() zu aktualisieren und
                        // rufe dann erneut die Login-Funktion auf.
                        userRepository.syncDatabase()
                        login(matrikelnummer, password)
                    }

                    3 -> {
                        userRepository
                        _errorMessage.value =
                            "Login fehlgeschlagen – bitte überprüfe deine Eingaben."
                    }

                    4 -> {
                        // Versuch nach 4 Fehlversuchen nochmal (optional):
                        userRepository.syncDatabase()
                        login(matrikelnummer, password)
                    }

                    5 -> {
                        userRepository.syncDatabase()
                        _errorMessage.value =
                            "Login fehlgeschlagen – eine Internetverbindung ist nötig zum Einloggen."
                    }

                    else -> {
                        // Bei mehr als 5 Fehlversuchen:
                        _errorMessage.value = "Login fehlgeschlagen."
                    }
                }
            }
        }
    }



    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
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
