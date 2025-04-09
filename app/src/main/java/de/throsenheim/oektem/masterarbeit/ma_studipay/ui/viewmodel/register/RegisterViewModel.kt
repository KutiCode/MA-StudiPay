package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.RegisterService




class RegisterViewModel(private val userRepositoryImpl: UserRepositoryImpl) : ViewModel() {

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun registerUser(matrikelnummer: String, firstName: String, lastName: String, password: String) {
        if (matrikelnummer.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte alle Felder ausfüllen"
            return
        }

        // Mindestanforderung: Passwort muss mindestens 8 Zeichen lang sein
        if (password.length < 8) {
            _errorMessage.value = "Das Passwort muss mindestens 8 Zeichen lang sein"
            return
        }

        // Optional: Weitere Prüfungen, z.B. ob das Passwort mindestens eine Zahl und einen Großbuchstaben enthält
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}\$")
        if (!regex.containsMatchIn(password)) {
            _errorMessage.value =
                "Das Passwort muss mindestens einen Großbuchstaben und eine Zahl enthalten"
            return
        }
        val registerResponse = RegisterService.registerService(
            matrikelnummer,
            firstName,
            lastName,
            password,
            userRepositoryImpl
        )
        if (registerResponse == "User erfolgreich registriert") {
            _registrationResult.value = true
        } else {
            _errorMessage.value = registerResponse
        }
    }

}
