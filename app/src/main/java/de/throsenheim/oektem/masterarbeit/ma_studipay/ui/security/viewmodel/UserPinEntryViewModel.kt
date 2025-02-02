package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserPinEntryViewModel : ViewModel() {

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
}