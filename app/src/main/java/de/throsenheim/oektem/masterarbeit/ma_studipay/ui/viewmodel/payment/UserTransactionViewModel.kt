package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.uiHelper
import kotlinx.coroutines.launch
import retrofit2.Response

class UserTransactionViewModel : ViewModel() {

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> get() = _balance

    fun fetchUserBalance(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val user = uiHelper.loadUser(context, matrikelnumber)
            _balance.value = user?.balance?.let { "$it €" } ?: "Fehlender Wert"
        }
    }

    fun addBalance(context: Context, matrikelnumber: String, amount: Double) {
        viewModelScope.launch {
            try {
                val request = BalanceUpdateRequest(matrikelnumber, amount)
                val response: Response<Unit> = RetrofitInstance.api.addBalance(request)
                if (response.isSuccessful) {
                    // Aktualisiere den Betrag des Nutzers in der Datenbank
                    val userDao = AppDatabase.getDatabase(context).userDao()
                    val user = userDao.getUserByMatriculationNumber(matrikelnumber)
                    user?.let {
                        it.balance += amount
                        userDao.updateUserBalance(matrikelnumber, it.balance)
                        _balance.postValue("${it.balance} €") // Aktualisiere die LiveData-Variable
                    }
                    Snackbar.make(
                        (context as Activity).findViewById(android.R.id.content),
                        "Du hast erfolgreich $amount € auf dein Konto eingezahlt.",
                        Snackbar.LENGTH_SHORT
                    ).show()
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
                val response: Response<Unit> = RetrofitInstance.api.deductBalance(request)
                if (response.isSuccessful) {
                    // Aktualisiere den Betrag des Nutzers in der Datenbank
                    val userDao = AppDatabase.getDatabase(context).userDao()
                    val user = userDao.getUserByMatriculationNumber(matrikelnumber)
                    user?.let {
                        it.balance -= amount
                        userDao.updateUserBalance(matrikelnumber, it.balance)
                        _balance.postValue("${it.balance} €") // Aktualisiere die LiveData-Variable
                    }
                } else {
                    Snackbar.make(
                        (context as Activity).findViewById(android.R.id.content),
                        "Failed to update balance",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Snackbar.make(
                    (context as Activity).findViewById(android.R.id.content),
                    "Error: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}