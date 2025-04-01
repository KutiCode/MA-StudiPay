package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import kotlinx.coroutines.launch

class UserInfoViewModel(
    private val userRepositoryImpl: UserRepositoryImpl,
    private val bankRepositoryImpl: BankRepositoryImpl
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _bank = MutableLiveData<Bank?>()
    val bank: LiveData<Bank?> get() = _bank

    fun loadUser(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val userDao = AppDatabase.getDatabase(context).userDao()
            val user = userDao.getUserByMatrikelnumber(matrikelnumber)
            _user.value = user
            user?.bank_code?.let { loadBank(it) }
        }
    }

    private fun loadBank(bankCode: String) {
        viewModelScope.launch {
            val bank = bankRepositoryImpl.getBankByCode(bankCode)
            _bank.value = bank
        }
    }

}