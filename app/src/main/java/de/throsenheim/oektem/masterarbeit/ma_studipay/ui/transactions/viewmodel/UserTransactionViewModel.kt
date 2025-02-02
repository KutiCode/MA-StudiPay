package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.launch

class UserTransactionViewModel : ViewModel() {

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> get() = _balance

    fun loadUserBalance(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val userDao = AppDatabase.getDatabase(context).userDao()
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            _balance.value = user?.balance?.let { "$it €" } ?: "Fehlender Wert"
        }
    }
}