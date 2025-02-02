package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BankConnectionViewModel : ViewModel() {

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    fun onSettingsClicked() {
        _navigateToSettings.value = true
    }

    fun onNavigatedToSettings() {
        _navigateToSettings.value = false
    }
}