package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.LoginService
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper

/**
 * LoginViewModel handles user login.
 *
 * It validates input, checks user credentials, and manages login retries.
 */
class LoginViewModel(application: Application, private val userRepositoryImpl: UserRepositoryImpl) :
    AndroidViewModel(application) {

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
     * @param matriculationNumber The matriculation number.
     * @param password The user's password.
     */
    fun login(context: Context, matriculationNumber: String, password: String) {
        if (matriculationNumber.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte fülle alle Felder aus"
            return
        }
        val response = LoginService.loginService(matriculationNumber, password, userRepositoryImpl)
        if (response) {
            saveUserToPreferences(matriculationNumber)
            _loginResult.value = true
            _errorCount = 0 // Reset error counter
        } else {
            _errorCount++
            when (_errorCount) {
                in 1..2 -> {
                    // On the first two failed attempts:
                    // Try to update the data with syncDatabase() and then call login() again.
                    LoginService.loginService(matriculationNumber, password, userRepositoryImpl)
                    _errorMessage.value =
                        "Login fehlgeschlagen - Bitte überprüfe deine Eingabe."
                    Log.d("LoginViewModel", "Login attempt failed: $_errorCount")
                }

                3 -> {
                    if (!UiHelper.isWifiConnectedAndBackendReachable(context)) {
                        LoginService.loginService(matriculationNumber, password, userRepositoryImpl)
                        _errorMessage.value =
                            "Login fehlgeschlagen. Eine Verbindung zum Backend ist nötig."
                    } else {
                        LoginService.loginService(matriculationNumber, password, userRepositoryImpl)
                    }
                }

                else -> {
                    _errorMessage.value = "Login fehlgeschlagen."
                    Log.d("LoginViewModel", "Login attempt failed: $_errorCount")
                }
            }
        }
    }


    /**
     * Saves the user's matriculation number to SharedPreferences and marks the user as logged in.
     *
     * @param matriculationNumber The matriculation number.
     */
    private fun saveUserToPreferences(matriculationNumber: String) {
        val sharedPreferences =
            getApplication<Application>().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("current_username", matriculationNumber)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}
