package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc

import android.app.Activity
import android.nfc.*
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import java.io.IOException


class NfcPaymentReceiver(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(activity)
    }

    companion object {
        private const val TAG = "NfcPaymentReceiver"
        private val PAYMENT_AID = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        // SELECT APDU gemäß ISO 7816-4
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(),  // CLA
            0xA4.toByte(),  // INS
            0x04.toByte(), // P1
            0x00.toByte(),  // P2
            0x07.toByte(),  // Lc (Länge der AID)
            *PAYMENT_AID,
            0x00.toByte()   // Le
        )
    }

    fun enableNfcReader() {
        nfcAdapter?.let {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            it.enableReaderMode(
                activity,
                { tag -> handleTagDiscovered(tag) },
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    fun disableNfcReader() {
        nfcAdapter?.disableReaderMode(activity)
    }

    private fun handleTagDiscovered(tag: Tag) {
        Log.d(TAG, "NFC Tag entdeckt: ${tag.id.toHexString()}")

        IsoDep.get(tag)?.use { isoDep ->
            try {
                isoDep.connect()
                val response = isoDep.transceive(SELECT_APDU)
                Log.d(TAG, "APDU Response: ${response.toHexString()}")

                if (response.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                    handleValidPaymentTag()
                } else {
                    handleInvalidResponse()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Kommunikationsfehler: ${e.message}")
            }
        }
    }

    private fun handleValidPaymentTag() {
        activity.runOnUiThread {
            // UI-Update für erfolgreiche Zahlung
            // Beispiel: Toast.makeText(activity, "Zahlung erfolgreich!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleInvalidResponse() {
        activity.runOnUiThread {
            // Fehlerbehandlung
        }
    }

    // Hilfsfunktionen
    private fun ByteArray.toHexString() = joinToString("") { "%02X".format(it) }
}