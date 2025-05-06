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

// ViewModel handling user balance operations such as fetching, adding, and deducting balance.
// Uses LiveData to expose the current balance as a string.
class UserTransactionViewModel : ViewModel() {

    // Private mutable LiveData to hold the current balance as a formatted string.
    private val _balance = MutableLiveData<String>()

    // Public immutable LiveData for the UI to observe balance changes.
    val balance: LiveData<String> get() = _balance

    /**
     * Fetches the user's balance using their matriculation number.
     *
     * Uses the UiHelper to load the user and updates the LiveData with the formatted balance.
     * If no value is found, it displays a default message ("Fehlender Wert").
     *
     * @param context The context used to access resources and storage.
     */
    fun fetchUserBalance(context: Context) {
        viewModelScope.launch {
            val user = UiHelper.loadUser(context)
            // If user and balance exist, append the euro symbol; else, display a fallback message.
            _balance.value = user?.balance?.let { "$it €" } ?: "Fehlender Wert"
        }
    }

    /**
     * Adds the specified amount to the user's balance.
     *
     * Calls the BalanceService to add funds. If the operation is successful,
     * the new balance is loaded and updated in the LiveData. A Snackbar is used to notify success.
     *
     * @param context The context used for UI display and resource access.
     * @param matriculationNumber The unique identifier of the user.
     * @param amount The amount to add to the account.
     */
    fun addBalance(context: Context, matriculationNumber: String, amount: Double) {
        // Attempt to add balance through BalanceService.
        val balanceResponse = BalanceService.addBalanceService(context, amount)
        if (balanceResponse) {
            // If successful, launch a coroutine to update balance and show a Snackbar.
            viewModelScope.launch {
                val user = UiHelper.loadUser(context)
                if (user != null) {
                    // Post the updated balance (appended with the euro symbol).
                    _balance.postValue("${user.balance} €")
                }
                // Display a success message using a Snackbar.
                Snackbar.make(
                    (context as Activity).findViewById(android.R.id.content),
                    "Du hast erfolgreich $amount € auf dein Konto eingezahlt.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else {
            // If the operation fails, inform the user with a Toast message.
            Toast.makeText(context, "Du konntest kein Geld einzahlen", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Deducts the specified amount from the user's balance.
     *
     * Calls the BalanceService to reduce the balance. On success, the LiveData is updated with the new balance,
     * and a Snackbar is displayed to inform the user of the successful deduction.
     *
     * @param context The context used for UI and resource access.
     * @param matriculationNumber The unique identifier of the user.
     * @param amount The amount to deduct from the account.
     */
    fun deductBalance(context: Context, matriculationNumber: String, amount: Double) {
        viewModelScope.launch {
            val user = UiHelper.loadUser(context)
            if (user == null) {
                Toast.makeText(context, "Du konntest kein Geld abheben", Toast.LENGTH_SHORT).show()
            } else if (user.bank_code == null) {
                Toast.makeText(context, "Du konntest kein Geld abheben", Toast.LENGTH_SHORT).show()
            }
            // Attempt to reduce balance using BalanceService.
            val balanceResponse =
                BalanceService.reduceBalanceService(context, amount)
            if (balanceResponse) {
                // On success, launch a coroutine to update the balance and display success message.
                if (user != null) {
                    // Update LiveData with the new balance and append the euro symbol.
                    _balance.postValue("${user.balance} €")
                }
                // Display a Snackbar notifying successful deduction.
                Snackbar.make(
                    (context as Activity).findViewById(android.R.id.content),
                    "Du hast erfolgreich $amount € von deinem Konto abgehoben.",
                    Snackbar.LENGTH_SHORT
                ).show()

            } else {
                // Inform the user of failure to deduct balance with a Toast message.
                Toast.makeText(context, "Du konntest kein Geld abheben", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
