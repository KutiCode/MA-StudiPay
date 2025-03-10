package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.provider.Settings
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.RsaEncryptionHelper
import java.io.IOException
import java.util.Locale

/**
 * Diese Klasse kapselt alle NFC-bezogenen Operationen.
 * Sie startet den Reader-Modus, verarbeitet Tags und sendet den SELECT-APDU-Befehl.
 */
class NfcService(private val context: Context, private val activity: Activity) {

    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null
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
        closeAllTechnologies(tag) // Schließe zuerst alle Technologien
        currentTag = tag

        try {
            val isoDep = IsoDep.get(tag) ?: run {
                onError?.invoke("Tag unterstützt keine IsoDep")
                return
            }

            isoDep.connect() // Verbinde erst NACH dem Schließen
            if (!isoDep.isConnected) {
                onError?.invoke("Verbindung fehlgeschlagen")
                return
            }

            // Sende SELECT-APDU
            val selectApdu = hexStringToByteArray("00A4040006F0123456789A")
            val response = isoDep.transceive(selectApdu)
            Log.d("NFC", "SELECT-APDU-Antwort: ${response.toHexString()}")

            // Verarbeite INIT-Antwort
            if (response.toHexString().startsWith("494E4954")) { // "INIT"
                val publicKey = RsaEncryptionHelper.getPublicKeyAsString()
                sendPublicKeyToSender(publicKey)
            }
        } catch (e: Exception) {
            onError?.invoke("Kritischer Fehler: ${e.message}")
        }
    }

    private fun closeAllTechnologies(tag: Tag) {
        val techList = tag.techList
        techList.forEach { tech ->
            try {
                when (tech) {
                    IsoDep::class.java.name -> {
                        val isoDep = IsoDep.get(tag)
                        isoDep?.close()
                    }

                    NfcA::class.java.name -> NfcA.get(tag)?.close()
                    NfcB::class.java.name -> NfcB.get(tag)?.close()
                    NfcF::class.java.name -> NfcF.get(tag)?.close()
                    NfcV::class.java.name -> NfcV.get(tag)?.close()
                    // Füge alle unterstützten Technologien hinzu
                }
            } catch (e: Exception) {
                Log.e("NFC", "Fehler beim Schließen von $tech: ${e.message}")
            }
        }
    }

    fun sendPublicKeyToSender(publicKey: String) {
        val tag = currentTag ?: run {
            onError?.invoke("Kein aktives Tag")
            return
        }

        closeAllTechnologies(tag) // Erneut schließen vor der Nutzung
        val isoDep = IsoDep.get(tag) ?: run {
            onError?.invoke("IsoDep nicht verfügbar")
            return
        }

        try {
            isoDep.connect()
            val publicKeyBytes = publicKey.toByteArray(Charsets.UTF_8)

            // APDU-Konstruktion mit korrektem Lc-Feld (Länge als Hex)
            val header = "00D00000${String.format("%02X", publicKeyBytes.size)}"
            val command = hexStringToByteArray(header) + publicKeyBytes

            // Sende APDU und prüfe Antwort
            val response = isoDep.transceive(command)
            Log.d("NFC", "Antwort auf Schlüsselübertragung: ${response.toHexString()}")

            if (response.size < 2 || response[response.size - 2] != 0x90.toByte()) {
                onError?.invoke("Fehlerhafte Antwort: ${response.toHexString()}")
            }
        } catch (e: IOException) {
            onError?.invoke("I/O-Fehler: ${e.message}")
        } finally {
            isoDep.close()
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