package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services.BankService
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class BankSelectViewModel(
    application: Application,
    private val userRepositoryImpl: UserRepositoryImpl
) : AndroidViewModel(application) {

    private val context: Context = getApplication<Application>().applicationContext

    fun assignBankToCurrentUser(bank: Bank) {

        viewModelScope.launch {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val currentUsername = sharedPreferences.getString("current_username", null)
            if (currentUsername != null) {
                if (BankService.assignBankService(
                        userRepositoryImpl,
                        currentUsername,
                        bank
                    )
                ) {
                    Log.d("BankSelectVM", "User erfolgreich aktualisiert")
                } else {
                    Log.e("BankSelectVM", "Fehler beim Aktualisieren des Users")
                }

            }
        }


    }
}

