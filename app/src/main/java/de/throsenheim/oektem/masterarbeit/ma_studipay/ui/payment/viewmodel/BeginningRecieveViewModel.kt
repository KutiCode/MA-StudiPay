package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc.NfcService

class BeginningRecieveViewModel(private val context: Context, private val activity: Activity) :
    ViewModel() {

    private val _initResponse = MutableLiveData<String>()
    val initResponse: LiveData<String> = _initResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val nfcService = NfcService(context, activity)

    init {
        nfcService.onInitResponseReceived = { response ->
            _initResponse.postValue(response)
        }
        nfcService.onError = { errorMsg ->
            _error.postValue(errorMsg)
        }
    }

    fun startNfc() {
        nfcService.startReaderMode()
    }

    fun stopNfc() {
        nfcService.stopReaderMode()
    }

    override fun onCleared() {
        nfcService.stopReaderMode()
        super.onCleared()
    }
}
