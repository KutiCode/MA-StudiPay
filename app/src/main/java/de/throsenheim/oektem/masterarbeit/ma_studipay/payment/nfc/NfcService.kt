package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.provider.Settings
import android.util.Log
import java.util.Locale

/**
 * Diese Klasse kapselt alle NFC-bezogenen Operationen.
 * Sie startet den Reader-Modus, verarbeitet Tags und sendet den SELECT-APDU-Befehl.
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
     */
    private fun processTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        isoDep?.let {
            try {
                it.connect()
                val selectApdu = hexStringToByteArray("00A4040007A0000002471001")
                Log.d(
                    "NFC",
                    "Sende SELECT-APDU: ${selectApdu.toHexString()}"
                )  // ✅ Logging hinzugefügt
                val response = it.transceive(selectApdu)
                val responseHex = response.toHexString()
                Log.d("NFC", "Antwort vom Sender: $responseHex")  // ✅ Logging der Antwort

                if (responseHex.startsWith("494E4954")) { // "INIT"
                    onInitResponseReceived?.invoke(responseHex)
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


    // Wandelt einen Hex-String (z.B. SELECT-APDU) in ein ByteArray um.
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
}
