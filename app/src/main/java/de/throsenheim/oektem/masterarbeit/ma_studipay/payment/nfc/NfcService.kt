package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.provider.Settings
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.RsaEncryptionHelper
import java.util.Locale

/**
 * Diese Klasse kapselt alle NFC-bezogenen Operationen.
 * Sie startet den Reader-Modus, verarbeitet Tags und sendet APDU-Befehle.
 */
class NfcService(private val context: Context, private val activity: Activity) {

    private var nfcAdapter: NfcAdapter? = null

    // Callback, der beim Empfang der INIT-Antwort aufgerufen wird.
    var onInitResponseReceived: ((String) -> Unit)? = null

    // Callback für Fehler.
    var onError: ((String) -> Unit)? = null

    init {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    }

    /** Startet den NFC-Reader-Modus. */
    fun startReaderMode() {
        if (nfcAdapter == null) {
            onError?.invoke("Dieses Gerät unterstützt kein NFC.")
            return
        }
        if (!nfcAdapter!!.isEnabled) {
            onError?.invoke("Bitte aktivieren Sie NFC.")
            // Optional: Öffne die NFC-Einstellungen
            activity.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            return
        }
        // Wir interessieren uns hier für NFC-A-Tags.
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        nfcAdapter!!.enableReaderMode(activity, { tag -> processTag(tag) }, flags, null)
    }

    /** Deaktiviert den NFC-Reader-Modus. */
    fun stopReaderMode() {
        nfcAdapter?.disableReaderMode(activity)
    }

    /**
     * Verarbeitet ein erkanntes NFC-Tag.
     * Hier wird der SELECT-APDU-Befehl gesendet und die Antwort ausgewertet.
     * Wenn INIT empfangen wird, sendet das Terminal anschließend seinen öffentlichen Schlüssel.
     */
    private fun processTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        isoDep?.let {
            try {
                it.connect()
                // Sende den SELECT-APDU-Befehl
                val selectApdu = hexStringToByteArray("00A4040007A0000002471001")
                Log.e("NFC", "Sende SELECT-APDU: ${selectApdu.toHexString()}")
                val response = it.transceive(selectApdu)
                val responseHex = response.toHexString()
                Log.e("NFC", "Antwort vom HCE-Sender: $responseHex")

                if (responseHex.startsWith("494E4954")) { // "INIT" in Hex: 49 4E 49 54
                    // INIT empfangen; benachrichtige den Callback
                    onInitResponseReceived?.invoke(responseHex)
                    // Erstelle bzw. hole deinen Terminal-öffentlichen Schlüssel.
                    RsaEncryptionHelper.generateKeyPairIfNeeded()
                    val publicKey = RsaEncryptionHelper.getPublicKeyAsString()
                    // Sende den öffentlichen Schlüssel an den HCE-Sender.
                    sendTerminalPublicKey(tag, publicKey, isoDep)
                } else {
                    onError?.invoke("Unerwartete Antwort: $responseHex")
                }
            } catch (e: Exception) {
                onError?.invoke("Fehler bei der NFC-Kommunikation: ${e.message}")
            } finally {
                try {
                    it.close()
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * Sendet den Terminal-öffentlichen Schlüssel an den HCE-Sender.
     *
     * @param tag Das NFC-Tag.
     * @param terminalPublicKey Der öffentliche Schlüssel als String (z.B. Base64-kodiert).
     * @param isoDep Die geöffnete IsoDep-Verbindung (wird hier weiterverwendet, bevor sie geschlossen wird).
     */
    private fun sendTerminalPublicKey(tag: Tag, terminalPublicKey: String, isoDep: IsoDep) {
        try {
            // Konvertiere den öffentlichen Schlüssel in einen Hex-String.
            // Füge die Länge des öffentlichen Schlüssels im APDU-Header hinzu (z.B. Lc-Feld).
            val publicKeyHex = stringToHex(terminalPublicKey)
            val lc = String.format("%02X", publicKeyHex.length / 2) // Länge in Bytes
            val command = "00D00000${lc}$publicKeyHex" // Header + Lc + Daten
            val apduCommand = hexStringToByteArray(command)
            Log.e("NFC", "Sende Terminal Public Key APDU: ${apduCommand.toHexString()}")
            // Sende den APDU-Befehl.
            val response = isoDep.transceive(apduCommand)
            Log.e(
                "NFC",
                "Antwort vom HCE-Sender auf Terminal Public Key: ${response.toHexString()}"
            )
        } catch (e: Exception) {
            Log.e("NFC", "Fehler beim Senden des öffentlichen Schlüssels: ${e.message}")
        }
    }

    // Hilfsfunktion: Wandelt einen Hex-String (z.B. SELECT-APDU) in ein ByteArray um.
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    // Erweiterungsfunktion: Wandelt ein ByteArray in einen Hex-String um.
    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { String.format(Locale.US, "%02X", it) }
    }

    // Hilfsfunktion: Wandelt einen String in einen Hex-String um.
    private fun stringToHex(input: String): String {
        return input.toByteArray(Charsets.UTF_8).joinToString("") { String.format("%02X", it) }
    }
}
