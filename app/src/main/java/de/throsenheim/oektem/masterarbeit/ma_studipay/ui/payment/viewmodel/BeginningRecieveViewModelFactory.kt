package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BeginningRecieveViewModelFactory(
    private val context: Context,
    private val activity: Activity
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BeginningRecieveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BeginningRecieveViewModel(context, activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
