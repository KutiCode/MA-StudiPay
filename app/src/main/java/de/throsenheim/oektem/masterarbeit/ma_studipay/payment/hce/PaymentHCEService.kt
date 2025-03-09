package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class PaymentHCEService : HostApduService() {

    // Der SELECT-APDU-Befehl, der vom NFC-Leser gesendet wird.
    private val SELECT_APDU = "00A4040008A0000002471001"

    // Initialer Wert, den wir als Handshake senden – hier "INIT"
    private val INITIAL_RESPONSE = "INIT"

    // Variable zum Speichern des tatsächlichen PaymentTokens (als JSON-String)
    private var paymentToken: String = ""

    // Zustandsvariable, um zu wissen, ob der Initialwert schon gesendet wurde
    private var initialResponseSent = false

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        // Wandle den empfangenen APDU-Befehl in einen Hex-String um
        val commandStr = commandApdu?.toHexString() ?: ""
        // Prüfe, ob der Befehl mit unserem SELECT-Befehl übereinstimmt
        if (commandStr.startsWith(SELECT_APDU)) {
            return if (!initialResponseSent) {
                // Noch nicht gesendet: Sende den initialen Wert
                initialResponseSent =
                    true  // Zustand ändern, damit dieser Block nur einmal durchläuft
                val responseBytes = INITIAL_RESPONSE.toByteArray(Charsets.UTF_8)
                // Hänge das Statuswort 0x9000 an, was "OK" bedeutet
                responseBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            } else {
                // Hier kannst du später den tatsächlichen PaymentToken senden
                val tokenBytes = paymentToken.toByteArray(Charsets.UTF_8)
                tokenBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            }
        }
        // Für unbekannte Befehle: Rückgabe eines Fehlercodes (0x6F00)
        return byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    override fun onDeactivated(reason: Int) {
        // Bei Deaktivierung (z. B. Verbindungsverlust) wird der Zustand zurückgesetzt
        initialResponseSent = false
        paymentToken = ""
    }

    // Hilfsmethode, um ein ByteArray als Hex-String darzustellen
    private fun ByteArray.toHexString() = joinToString(separator = "") { String.format("%02X", it) }

    // Methode, um den PaymentToken zu setzen (wird z. B. nach PIN-Validierung aufgerufen)
    fun setPaymentToken(token: String) {
        paymentToken = token
    }
}