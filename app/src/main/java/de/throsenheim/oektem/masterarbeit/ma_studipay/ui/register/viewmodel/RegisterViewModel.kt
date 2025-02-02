package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun registerUser(matrikelnummer: String, firstName: String, lastName: String, password: String) {
        if (matrikelnummer.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
            _errorMessage.value = "Bitte alle Felder ausfüllen"
            return
        }

        viewModelScope.launch {
            val userExists = userRepository.getUserByMatrikelnumber(matrikelnummer) != null
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
                    balance = 0.0
                )
                userRepository.registerUserLocally(user)
                _registrationResult.value = true
            }
        }
    }

    private fun hashPassword(password: String): String {
        // Beispiel für Hashing, nutze ggf. eine stärkere Methode
        return password.hashCode().toString()
    }
    private suspend fun generateUniqueKontonummer(): String {
        var kontonummer: String
        do {
            kontonummer = (100000..999999).random().toString()
        } while (userRepository.userDao.getAllUsers().any { it.accountNumber == kontonummer })
        return kontonummer
    }
}
