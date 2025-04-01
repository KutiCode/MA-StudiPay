package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.UserRegistrationRequest
import kotlinx.coroutines.launch


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

        viewModelScope.launch {
            val userExists = userRepositoryImpl.getUserByImmatriculationNumber(matrikelnummer) != null
            if (userExists) {
                _errorMessage.value = "Matrikelnummer existiert bereits"
            } else {
                val hashedPassword = hashPassword(password)
                val user = User(
                    matrikelnumber = matrikelnummer,
                    firstName = firstName,
                    lastName = lastName,
                    password = hashedPassword,
                    accountNumber = generateUniqueKontonummer(),
                    balance = 0.0,
                    securePin = "0000",
                    bank_code = null
                )

                // Sende den neuen User an das Backend
                val request = UserRegistrationRequest(
                    matrikelnumber = matrikelnummer,
                    firstName = firstName,
                    lastName = lastName,
                    password = hashedPassword,
                    accountNumber = user.accountNumber,
                    balance = user.balance,
                    securePin = user.securePin
                )
                try {
                    val response = RetrofitInstance.api.registerUser(request)
                    if (response.isSuccessful) {
                        userRepositoryImpl.insertUser(user)
                        userRepositoryImpl.syncDatabase()
                        _registrationResult.value = true
                    } else {
                        _errorMessage.value = "Registrierung beim Backend fehlgeschlagen"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Fehler: ${e.message}"
                }
            }
        }
    }


    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }
    private suspend fun generateUniqueKontonummer(): String {
        var kontonummer: String
        do {
            kontonummer = (100000..999999).random().toString()
        } while (userRepositoryImpl.userDao.getAllUsers().any { it.accountNumber == kontonummer })
        return kontonummer
    }
}
