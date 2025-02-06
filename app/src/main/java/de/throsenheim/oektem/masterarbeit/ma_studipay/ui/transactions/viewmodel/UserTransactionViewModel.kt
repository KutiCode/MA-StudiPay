package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

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

    fun addBalance(context: Context, matrikelnumber: String, amount: Double) {
        viewModelScope.launch {
            try {
                val request = BalanceUpdateRequest(matrikelnumber, amount)
                val response: Response<Unit> = RetrofitInstance.api.deductBalance(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Balance updated successfully", Toast.LENGTH_SHORT)
                        .show()

                    // Aktualisiere den Betrag des Nutzers in der Datenbank
                    val userDao = AppDatabase.getDatabase(context).userDao()
                    val user = userDao.getUserByMatrikelnumber(matrikelnumber)
                    user?.let {
                        it.balance += amount
                        userDao.updateUserBalance(matrikelnumber, it.balance)
                    }
                } else {
                    Toast.makeText(context, "Failed to update balance", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deductBalance(context: Context, matrikelnumber: String, amount: Double) {
        viewModelScope.launch {
            try {
                val request = BalanceUpdateRequest(matrikelnumber, amount)
                val response: Response<Unit> = RetrofitInstance.api.addBalance(request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Balance updated successfully", Toast.LENGTH_SHORT)
                        .show()

                    // Aktualisiere den Betrag des Nutzers in der Datenbank
                    val userDao = AppDatabase.getDatabase(context).userDao()
                    val user = userDao.getUserByMatrikelnumber(matrikelnumber)
                    user?.let {
                        it.balance -= amount
                        userDao.updateUserBalance(matrikelnumber, it.balance)
                    }
                } else {
                    Toast.makeText(context, "Failed to update balance", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



}