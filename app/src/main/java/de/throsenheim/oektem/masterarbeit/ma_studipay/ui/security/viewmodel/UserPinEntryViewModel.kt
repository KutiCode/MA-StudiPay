
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
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserPinEntryViewModel(private val userRepository: UserRepository) : ViewModel() {

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
            userRepository.updateSecurePin(matrikelnumber, newPin)
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
            val storedPin = userRepository.getSecurePin(matrikelnummer)
            if (storedPin == pin) {
                Toast.makeText(context, "PIN is correct", Toast.LENGTH_SHORT).show()
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build()
                navController.navigate(R.id.navigation_dashboard, null, navOptions)
            } else {
                Toast.makeText(context, "PIN is incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }
}