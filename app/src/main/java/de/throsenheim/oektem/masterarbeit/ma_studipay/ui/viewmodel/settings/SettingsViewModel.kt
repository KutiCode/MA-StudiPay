package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    fun loadUserName(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val userDao = AppDatabase.getDatabase(context).userDao()
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            _userName.value = user?.let { "${it.firstName} ${it.lastName}" } ?: "Hallo, Benutzer"
        }
    }

    fun logoutUser(context: Context) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}