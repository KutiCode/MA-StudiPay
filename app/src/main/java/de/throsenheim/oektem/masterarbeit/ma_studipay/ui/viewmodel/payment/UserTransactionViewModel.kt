package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.BalanceService
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.launch

class UserTransactionViewModel : ViewModel() {

    private val _balance = MutableLiveData<String>()
    val balance: LiveData<String> get() = _balance

    fun fetchUserBalance(context: Context, matrikelnumber: String) {
        viewModelScope.launch {
            val user = UiHelper.loadUser(context, matrikelnumber)
            _balance.value = user?.balance?.let { "$it €" } ?: "Fehlender Wert"
        }
    }

    fun addBalance(context: Context, matriculationNumber: String, amount: Double) {
        val balanceReponse =
            BalanceService.addBalanceService(context, matriculationNumber, amount)
        if (balanceReponse) {
            viewModelScope.launch {
                val user = UiHelper.loadUser(context, matriculationNumber)

                if (user != null) {
                    _balance.postValue("${user.balance} €")
                }
                Snackbar.make(
                    (context as Activity).findViewById(android.R.id.content),
                    "Du hast erfolgreich $amount € auf dein Konto eingezahlt.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(context, "Du konntest kein Geld einzahlen", Toast.LENGTH_SHORT)
                .show()
            }
        }


    fun deductBalance(context: Context, matriculationNumber: String, amount: Double) {
        val balanceReponse =
            BalanceService.reduceBalanceService(context, matriculationNumber, amount)
        if (balanceReponse) {
            viewModelScope.launch {
                val user = UiHelper.loadUser(context, matriculationNumber)
                if (user != null) {
                    _balance.postValue("${user.balance} €")
                }
                Snackbar.make(
                    (context as Activity).findViewById(android.R.id.content),
                    "Du hast erfolgreich $amount € von deinem Konto abgehoben.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(context, "Du konntest kein Geld abheben", Toast.LENGTH_SHORT)
                .show()
        }
    }
}