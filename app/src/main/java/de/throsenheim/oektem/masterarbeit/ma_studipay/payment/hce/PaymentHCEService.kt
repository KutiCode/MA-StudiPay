package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.content.Context
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.os.PowerManager
import android.util.Log

class PaymentHCEService : HostApduService() {

    // Der SELECT-APDU-Befehl, der vom NFC-Leser gesendet wird.
    private val SELECT_APDU = "00A4040006F0123456789A"
    // Initialer Wert, den wir als Handshake senden – hier "INIT"
    private val INITIAL_RESPONSE = "INIT"
    // Variable zum Speichern des tatsächlichen PaymentTokens (als JSON-String)
    private var paymentToken: String = ""
    // Zustandsvariable, um zu wissen, ob der Initialwert schon gesendet wurde
    private var initialResponseSent = false


    private val SEND_PUBLIC_KEY_APDU = "00D00000"

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d("HCE", "processCommandApdu wurde aufgerufen!")
        // Prüfe, ob der Bildschirm aktiv ist und ob unser Service als aktiv markiert ist.
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager == null || !powerManager.isInteractive || !isActive) {
            Log.d("HCE", "Gerät nicht aktiv oder HCE nicht aktiviert. Rückgabe 6982.")
            return byteArrayOf(
                0x69.toByte(),
                0x82.toByte()
            )  // 6982: Security condition not satisfied
        }

        val commandStr = commandApdu?.toHexString() ?: ""
        Log.d("HCE", "Empfangener APDU-Befehl: $commandStr")
        if (commandStr.startsWith(SELECT_APDU)) {
            Log.d("HCE", "SELECT-APDU erkannt, sende INIT...")
            return if (!initialResponseSent) {
                initialResponseSent = true
                val responseBytes = INITIAL_RESPONSE.toByteArray(Charsets.UTF_8)
                Log.d("HCE", "Sende INIT-Antwort: ${responseBytes.toHexString()}")
                responseBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            } else {
                val tokenBytes = paymentToken.toByteArray(Charsets.UTF_8)
                Log.d("HCE", "Sende Payment-Token: ${tokenBytes.toHexString()}")
                tokenBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            }
        }
        if (commandStr.startsWith(SEND_PUBLIC_KEY_APDU)) {
            Log.d("HCE", "SEND_PUBLIC_KEY_APDU erkannt, öffentlichen Schlüssel erhalten.")
            // Hier müsste der öffentliche Schlüssel als Byte-Array gesendet werden
            return byteArrayOf(0x90.toByte(), 0x00.toByte())
        }
        Log.d("HCE", "Unbekannter APDU-Befehl erhalten.")
        return byteArrayOf(0x6F.toByte(), 0x00.toByte())  // 6F00: Fehler
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "onDeactivated: Grund: $reason. Zurücksetzen des Zustands.")
        initialResponseSent = false
        paymentToken = ""
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { String.format("%02X", it) }

    // Methode, um den PaymentToken zu setzen (wird z. B. nach PIN-Validierung aufgerufen)
    fun setPaymentToken(token: String) {
        paymentToken = token
    }

    companion object {
        // Flag, das angibt, ob der HCE-Service aktiv reagieren soll.
        var isActive: Boolean = false
    }
}