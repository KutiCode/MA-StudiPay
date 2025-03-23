package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.bank.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance

class BankConnectionViewModel(private val context: Context) : ViewModel() {
    private val _currentUserBank = MutableLiveData<String?>()
    val currentUserBank: LiveData<String?> get() = _currentUserBank
    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings
    private var bankRepository: BankRepository =
        BankRepository(AppDatabase.getDatabase(context).bankDao())
    private var userRepositoryImpl = UserRepositoryImpl(
        userDao = AppDatabase.getDatabase(context).userDao(),
        apiService = RetrofitInstance.api,
        context = context
    )

    fun onSettingsClicked() {
        _navigateToSettings.value = true
    }

    fun onNavigatedToSettings() {
        _navigateToSettings.value = false
    }

    suspend fun onBankConnectionClicked() {
        bankRepository.syncBanksFromBackend()
    }

    suspend fun loadCurrentUserBank() {
        // Hole den aktuellen Nutzer aus der DB
        val bankCode = userRepositoryImpl.getCurrentUser()?.bank_code
        // Hole den Banknamen anhand des bankCode
        val bankName = bankCode?.let { bankRepository.getBankByCode(it)?.name }
        _currentUserBank.value = bankName
    }

}