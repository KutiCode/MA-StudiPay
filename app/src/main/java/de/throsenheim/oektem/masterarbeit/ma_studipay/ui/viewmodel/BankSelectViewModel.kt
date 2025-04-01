package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.launch

class BankSelectViewModel(
    private val context: Context,
    private val userRepositoryImpl: UserRepositoryImpl
) : ViewModel() {


    fun assignBankToCurrentUser(bank: Bank) {
        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val currentUsername = sharedPreferences.getString("current_username", null)
            if (currentUsername != null) {
                val user = userRepositoryImpl.getUserByImmatriculationNumber(currentUsername)
                if (user != null) {
                    // Protokolliere den neuen bankCode
                    Log.d(
                        "BankSelectVM",
                        "Aktualisiere User ${user.matrikelnumber} mit bankCode: ${bank.bank_code}"
                    )
                    user.bank_code = bank.bank_code

                    try {
                        // Aktualisiere den User in der lokalen DB und im Backend
                        userRepositoryImpl.syncUserWithBackend(user)
                        // Speichere den neuen Bankcode in SharedPreferences (falls genutzt)
                        sharedPreferences.edit().putString("current_user_bank", bank.bank_code)
                            .apply()
                        Log.d("BankSelectVM", "User erfolgreich aktualisiert: ${user.toString()}")
                    } catch (e: Exception) {
                        Log.e("BankSelectVM", "Fehler beim Aktualisieren des Users", e)
                    }
                } else {
                    Log.e("BankSelectVM", "Kein User mit Matrikelnummer $currentUsername gefunden")
                }
            } else {
                Log.e("BankSelectVM", "current_username in SharedPreferences nicht gefunden")
            }
        }
    }


}

