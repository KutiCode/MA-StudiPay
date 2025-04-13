package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance

// ViewModel for managing bank connection details and navigation events related to bank settings.
class BankConnectionViewModel(context: Context) : ViewModel() {

    // MutableLiveData to hold the name of the current user's bank connection.
    private val _currentUserBank = MutableLiveData<String?>()

    // Public LiveData to expose the current user's bank name.
    val currentUserBank: LiveData<String?> get() = _currentUserBank

    // MutableLiveData used to signal that navigation to the settings screen should occur.
    private val _navigateToSettings = MutableLiveData<Boolean>()

    // Public LiveData to observe navigation events.
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    // Create a BankRepositoryImpl instance using the database's bankDao.
    private var bankRepositoryImpl: BankRepositoryImpl =
        BankRepositoryImpl(AppDatabase.getDatabase(context).bankDao())

    // Create a UserRepositoryImpl instance using the database's userDao and the Retrofit API.
    private var userRepositoryImpl = UserRepositoryImpl(
        userDao = AppDatabase.getDatabase(context).userDao(),
        apiService = RetrofitInstance.api,
        context = context
    )

    /**
     * Called when the settings button is clicked.
     * Updates the LiveData to signal the UI to navigate to the settings screen.
     */
    fun onSettingsClicked() {
        _navigateToSettings.value = true
    }

    /**
     * Resets the navigation flag after navigation is completed.
     */
    fun onNavigatedToSettings() {
        _navigateToSettings.value = false
    }

    /**
     * Synchronizes bank data from the backend.
     *
     * This suspend function calls the repository to sync all bank data.
     */
    suspend fun onBankConnectionClicked() {
        bankRepositoryImpl.syncBanksFromBackend()
    }

    /**
     * Loads the current user's bank information.
     *
     * This suspend function retrieves the current user from the repository, then uses the bank code
     * to get the corresponding bank name from the bank repository. The result is stored in LiveData.
     */
    suspend fun loadCurrentUserBank() {
        // Retrieve the current user's bank code from the user repository.
        val bankCode = userRepositoryImpl.getCurrentUser()?.bank_code
        // Use the bank code to get the bank name from the bank repository.
        val bankName = bankCode?.let { bankRepositoryImpl.getBankByCode(it)?.name }
        // Update LiveData with the bank name (or null if not found).
        _currentUserBank.value = bankName

        Log.d("BankConnectionViewModel", "Current User Bank: $bankName")
    }
}
