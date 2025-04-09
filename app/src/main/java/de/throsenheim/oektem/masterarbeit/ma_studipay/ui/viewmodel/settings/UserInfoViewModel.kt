package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

class UserInfoViewModel(
    private val bankRepositoryImpl: BankRepositoryImpl
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user
    private val _bank = MutableLiveData<Bank?>()
    val bank: LiveData<Bank?> get() = _bank

    fun fetchUser(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val user = UiHelper.loadUser(context, matrikelnumber)
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