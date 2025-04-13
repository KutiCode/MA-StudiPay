package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.RegisterService
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

// ViewModel that handles user registration, including input validation and calling the register service.
class RegisterViewModel(private val userRepositoryImpl: UserRepositoryImpl) : ViewModel() {

    // Private mutable LiveData indicating whether registration was successful.
    private val _registrationResult = MutableLiveData<Boolean>()

    // Public immutable LiveData to be observed by the UI to react to registration success.
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    // Private mutable LiveData to hold any error messages during registration.
    private val _errorMessage = MutableLiveData<String>()

    // Public immutable LiveData for displaying error messages in the UI.
    val errorMessage: LiveData<String> get() = _errorMessage

    /**
     * Registers a user using the provided details.
     *
     * This function performs input validation on the matriculation number, first name,
     * last name, and password. It checks for blank fields, minimum password length,
     * and password strength (requiring at least one uppercase letter and one digit).
     *
     * @param matriculationNumber The unique matriculation number of the user.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param password The password for the new account.
     */
    fun registerUser(

        matriculationNumber: String,
        firstName: String,
        lastName: String,
        password: String
    ) {

        // Validation: Check that no fields are blank.
        if (matriculationNumber.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte alle Felder ausfüllen"
            return
        }
        viewModelScope.launch {

            if (!UiHelper.isHostReachableWithSocket()) {
                _errorMessage.value = "Verbindung zum Backend nicht möglich"
                return@launch
            }
        }
        // Validation: Ensure the password is at least 8 characters long.
        if (password.length < 8) {
            _errorMessage.value = "Das Passwort muss mindestens 8 Zeichen lang sein"
            return
        }
        // Validation: Ensure the password contains at least one uppercase letter and one digit.
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}\$")
        if (!regex.containsMatchIn(password)) {
            _errorMessage.value =
                "Das Passwort muss mindestens einen Großbuchstaben und eine Zahl enthalten"
            return
        }
        // Call the RegisterService to perform the registration.
        val registerResponse = RegisterService.registerService(
            matriculationNumber,
            firstName,
            lastName,
            password,
            userRepositoryImpl
        )
        // Check the response message to determine if registration was successful.
        if (registerResponse == "User erfolgreich registriert") {
            _registrationResult.value = true  // Registration succeeded.
        } else {
            _errorMessage.value = registerResponse  // Update error message with the response.
        }
    }
}
