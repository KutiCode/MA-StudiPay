package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token.TokenGenerator
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.RsaEncryptionHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view.BeginningSendingFragment

class PaymentHCEService : HostApduService() {

    // Der SELECT-APDU-Befehl, der vom NFC-Leser gesendet wird.
    private val SELECT_APDU = "00A4040007A0000002471001"
    // Initialer Wert, den wir als Handshake senden – hier "INIT"
    private val INITIAL_RESPONSE = "INIT"
    // Variable zum Speichern des tatsächlichen PaymentTokens (als JSON-String)
    private var paymentToken: String = ""
    // Zustandsvariable, um zu wissen, ob der Initialwert schon gesendet wurde
    private var initialResponseSent = false

    private val SET_TERMINAL_PUBLIC_KEY_APDU = "00D00000"

    private var terminalPublicKey: String? = null

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.e("HCE", "processCommandApdu wurde aufgerufen!")
        // Prüfe, ob der Bildschirm aktiv ist und ob unser Service als aktiv markiert ist.


        val commandStr = commandApdu?.toHexString() ?: ""
        Log.e("HCE", "Empfangener APDU-Befehl: $commandStr")
        if (commandStr.startsWith(SELECT_APDU)) {
            Log.e("HCE", "SELECT-APDU erkannt, sende INIT...")
            return if (!initialResponseSent) {
                initialResponseSent = true
                val responseBytes = INITIAL_RESPONSE.toByteArray(Charsets.UTF_8)
                Log.e("HCE", "Sende INIT-Antwort: ${responseBytes.toHexString()}")
                responseBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            } else {
                val tokenBytes = paymentToken.toByteArray(Charsets.UTF_8)
                Log.e("HCE", "Sende Payment-Token: ${tokenBytes.toHexString()}")
                tokenBytes + byteArrayOf(0x90.toByte(), 0x00.toByte())
            }
        }

        if (commandStr.startsWith(SET_TERMINAL_PUBLIC_KEY_APDU)) {
            // Extrahiere Lc (Länge des öffentlichen Schlüssels in Bytes)
            val lcHex = commandStr.substring(
                SET_TERMINAL_PUBLIC_KEY_APDU.length,
                SET_TERMINAL_PUBLIC_KEY_APDU.length + 2
            )
            val lc = lcHex.toInt(16)
            val publicKeyDataHex = commandStr.substring(
                SET_TERMINAL_PUBLIC_KEY_APDU.length + 2,
                SET_TERMINAL_PUBLIC_KEY_APDU.length + 2 + lc * 2
            )
            terminalPublicKey = hexStringToString(publicKeyDataHex)

            Log.e("HCE", "Terminal-öffentlicher Schlüssel empfangen: $terminalPublicKey")
            return byteArrayOf(0x90.toByte(), 0x00.toByte()) // SW9000: Erfolg
        }


        Log.e("HCE", "Unbekannter APDU-Befehl erhalten.")
        return byteArrayOf(0x6F.toByte(), 0x00.toByte())  // 6F00: Fehler
    }

    override fun onDeactivated(reason: Int) {
        Log.e("HCE", "onDeactivated: Grund: $reason. Zurücksetzen des Zustands.")
        initialResponseSent = false
        paymentToken = ""
        terminalPublicKey = null // Schlüssel zurücksetzen
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { String.format("%02X", it) }

    // Methode, um den PaymentToken zu setzen (wird z. B. nach PIN-Validierung aufgerufen)
    fun setPaymentToken(token: String) {
        paymentToken = token
    }

    private fun stringToHex(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString("") { String.format("%02X", it) }
    }

    private fun hexStringToString(hex: String): String {
        val output = StringBuilder()
        var i = 0
        while (i < hex.length) {
            val str = hex.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }

    companion object {
        var isActive: Boolean = false


    }


}
