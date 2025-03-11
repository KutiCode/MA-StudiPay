package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class AppHostApduService : HostApduService() {
    companion object {
        private const val TAG = "AppHostApduService"

        // AID für unser Zahlungssystem
        private val PAYMENT_AID = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        // APDU Antworten
        private val SELECT_OK_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val UNKNOWN_CMD_SW = byteArrayOf(0x6A.toByte(), 0x82.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d(TAG, "Empfangenes APDU: ${bytesToHex(commandApdu)}")

        // Überprüfen auf SELECT-Befehl mit unserer AID
        if (isSelectAidApdu(commandApdu, PAYMENT_AID)) {
            Log.d(TAG, "Korrekte AID erkannt")
            return SELECT_OK_SW
        }

        Log.w(TAG, "Unbekanntes APDU-Kommando")
        return UNKNOWN_CMD_SW
    }

    private fun isSelectAidApdu(apdu: ByteArray, aid: ByteArray): Boolean {
        // Überprüfen auf SELECT Befehl gemäß ISO 7816-4
        // Header: CLA=00, INS=A4, P1=04, P2=00
        val headerCorrect = apdu.size >= 5 &&
                apdu[0] == 0x00.toByte() &&  // CLA
                apdu[1] == 0xA4.toByte() &&  // INS
                apdu[2] == 0x04.toByte() &&   // P1
                apdu[3] == 0x00.toByte()      // P2

        // Überprüfen ob die AID übereinstimmt
        val aidLength = apdu[4].toInt() and 0xFF  // Lc-Byte
        val aidCorrect = apdu.size >= 5 + aidLength &&
                apdu.copyOfRange(5, 5 + aidLength).contentEquals(aid)

        return headerCorrect && aidCorrect
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCEService deaktiviert. Grund: $reason")
    }

    // Hilfsfunktion zur Byte-Array-Darstellung
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }
}