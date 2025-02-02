package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.carddetails.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import kotlinx.coroutines.launch

class OrangeDetailsViewModel : ViewModel() {

    private val _userDetails = MutableLiveData<User?>()
    val userDetails: LiveData<User?> get() = _userDetails

    fun loadUserDetails(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val userDao = AppDatabase.getDatabase(context).userDao()
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            _userDetails.value = user
        }
    }

    fun clearUserDetails() {
        _userDetails.value = null
    }
}