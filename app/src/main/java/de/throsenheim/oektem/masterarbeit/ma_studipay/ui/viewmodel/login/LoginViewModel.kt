package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.LoginService

/**
 * LoginViewModel handles user login.
 *
 * It validates input, checks user credentials, and manages login retries.
 */
class LoginViewModel(application: Application, private val userRepositoryImpl: UserRepositoryImpl) : AndroidViewModel(application) {

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
        if (LoginService.loginService(matrikelnummer, password, userRepositoryImpl)) {
            saveUserToPreferences(matrikelnummer)
            _loginResult.value = true
            _errorCount = 0 // Reset error counter
        } else {
                _errorCount++
                when (_errorCount) {
                    in 1..2 -> {
                        // On the first two failed attempts:
                        // Try to update the data with syncDatabase() and then call login() again.
                        LoginService.loginService(matrikelnummer, password, userRepositoryImpl)
                    }
                    3 -> {
                        _errorMessage.value =
                            "Login fehlgeschlagen - Bitte überprüfe deine Eingabe."
                    }
                    4 -> {
                        // On the fourth attempt (optional):
                        // Try again after synchronizing the data.
                        LoginService.loginService(matrikelnummer, password, userRepositoryImpl)
                    }
                    5 -> {
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
