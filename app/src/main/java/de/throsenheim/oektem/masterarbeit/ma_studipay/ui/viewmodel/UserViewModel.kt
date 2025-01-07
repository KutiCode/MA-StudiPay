package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun addUser(name: String, vorname: String, benutzername: String, passwort: String) {
        val user = User(name = name, vorname = vorname, benutzername = benutzername, passwort = passwort)
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun getUserByBenutzername(benutzername: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByBenutzername(benutzername)
            onResult(user)
        }
    }
}
