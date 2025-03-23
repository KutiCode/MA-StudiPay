
package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import kotlinx.coroutines.launch

class UserPinEntryViewModel(private val userRepositoryImpl: UserRepositoryImpl) : ViewModel() {

    private val _pin = MutableLiveData<String>()
    val pin: LiveData<String> get() = _pin

    init {
        _pin.value = ""
    }

    fun appendDigit(digit: String) {
        _pin.value = _pin.value + digit
    }

    fun clearPin() {
        _pin.value = ""
    }

    fun deleteLastDigit() {
        _pin.value = _pin.value?.dropLast(1)
    }

    fun updateSecurePin(context: Context, matrikelnumber: String, newPin: String) {
        viewModelScope.launch {
            userRepositoryImpl.updateSecurePin(matrikelnumber, newPin)
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("secure_pin", newPin)
                apply()
            }
        }
    }

    fun verifySecurePin(
        context: Context,
        navController: NavController,
        matrikelnummer: String,
        pin: String
    ) {
        viewModelScope.launch {
            val storedPin = userRepositoryImpl.getSecurePin(matrikelnummer)
            if (storedPin == pin) {

                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()
                navController.navigate(R.id.beginningSendingFragment, null, navOptions)
            } else {
                Toast.makeText(context, "Falsche Pin Eingabe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}