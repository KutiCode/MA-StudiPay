package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import kotlinx.coroutines.launch

class UserInfoViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    fun loadUser(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val userDao = AppDatabase.getDatabase(context).userDao()
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            _user.value = user
        }
    }
}