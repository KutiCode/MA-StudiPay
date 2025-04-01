package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment.BeginningRecieveViewModel

class BeginningRecieveFactory(
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
