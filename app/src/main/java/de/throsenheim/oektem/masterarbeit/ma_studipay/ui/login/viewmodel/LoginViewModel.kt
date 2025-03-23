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

/**
 * LoginViewModel handles user login.
 *
 * It validates input, checks user credentials, and manages login retries.
 */
class LoginViewModel(application: Application, private val userRepository: UserRepository) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private var _errorCount: Int = 0

    /**
     * Attempts to log in using the provided matriculation number and password.
     *
     * If any field is blank, an error message is set immediately.
     * Otherwise, it checks the credentials and may retry login after synchronizing the database.
     *
     * @param matrikelnummer The matriculation number.
     * @param password The user's password.
     */
    fun login(matrikelnummer: String, password: String) {
        if (matrikelnummer.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte fülle alle Felder aus"
            return
        }

        viewModelScope.launch {
            val user = userRepository.getUserByMatrikelnumber(matrikelnummer)

            if (user != null && verifyPassword(password, user.password)) {
                // Login successful
                saveUserToPreferences(matrikelnummer)
                _loginResult.value = true
                _errorCount = 0 // Reset error counter
            } else {
                _errorCount++
                when (_errorCount) {
                    in 1..2 -> {
                        // On the first two failed attempts:
                        // Try to update the data with syncDatabase() and then call login() again.
                        userRepository.syncDatabase()
                        login(matrikelnummer, password)
                    }
                    3 -> {
                        _errorMessage.value =
                            "Login fehlgeschlagen - Bitte überprüfe deine Eingabe."
                    }
                    4 -> {
                        // On the fourth attempt (optional):
                        // Try again after synchronizing the data.
                        userRepository.syncDatabase()
                        login(matrikelnummer, password)
                    }
                    5 -> {
                        userRepository.syncDatabase()
                        _errorMessage.value =
                            "Login fehlgeschlagen - Eine Internetverbindung ist erforderlich."
                    }
                    else -> {
                        // After more than 5 failed attempts:
                        _errorMessage.value = "Login fehlgeschlagen."
                    }
                }
            }
        }
    }

    /**
     * Verifies that the provided plain text password matches the hashed password.
     *
     * @param password The plain text password.
     * @param hashedPassword The hashed password stored for the user.
     * @return True if the password matches, false otherwise.
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }


    /**
     * Saves the user's matriculation number to SharedPreferences and marks the user as logged in.
     *
     * @param matrikelnummer The matriculation number.
     */
    fun saveUserToPreferences(matrikelnummer: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("current_username", matrikelnummer)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}
