package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

public class AppHostApduService : HostApduService() {
    private var messageCounter = 0
    private val okayMessage: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())

    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {
        if (selectAidApdu(apdu)) {
            Log.i("HCEDEMO", "Application selected")
            return okayMessage
        } else {
            Log.i("HCEDEMO", "Received: " + String(apdu))
            return nextMessage
        }
    }


    private val nextMessage: ByteArray
        get() = ("Message from android: " + messageCounter++).toByteArray()

    private fun selectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0.toByte() && apdu[1] == 0xa4.toByte()
    }

    override fun onDeactivated(reason: Int) {
        Log.i("HCEDEMO", "Deactivated: $reason")
    }
}