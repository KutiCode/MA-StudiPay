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

// ViewModel class used to retrieve and manage the user's information and their associated bank data.
class UserInfoViewModel(
    private val bankRepositoryImpl: BankRepositoryImpl  // Repository instance for accessing bank-related data.
) : ViewModel() {

    // Private MutableLiveData to hold the fetched user data.
    private val _user = MutableLiveData<User?>()

    // Public immutable LiveData to allow external observation of the user data.
    val user: LiveData<User?> get() = _user

    // Private MutableLiveData to hold the bank details related to the user.
    private val _bank = MutableLiveData<Bank?>()

    // Public immutable LiveData to allow external observation of the bank data.
    val bank: LiveData<Bank?> get() = _bank

    /**
     * Fetches the user information based on a matriculation number.
     * If the user exists, loads the bank details associated with the user's bank code.
     *
     * @param context Context used for resource access.
     * @param matriculationNumber Unique identifier for the user.
     */
    fun fetchUser(context: Context, matriculationNumber: String) {
        viewModelScope.launch {
            // Load user data with a helper function.
            val user = UiHelper.loadUser(context, matriculationNumber)
            // Update LiveData with the fetched user data.
            _user.value = user
            // If a user is found and they have a bank code, load the corresponding bank data.
            user?.bank_code?.let { loadBank(it) }
        }
    }

    /**
     * Loads bank information for a given bank code.
     *
     * @param bankCode Unique code identifying the bank.
     */
    private fun loadBank(bankCode: String) {
        viewModelScope.launch {
            // Retrieve the bank details from the repository using the bank code.
            val bank = bankRepositoryImpl.getBankByCode(bankCode)
            // Update LiveData with the retrieved bank information.
            _bank.value = bank
        }
    }
}
