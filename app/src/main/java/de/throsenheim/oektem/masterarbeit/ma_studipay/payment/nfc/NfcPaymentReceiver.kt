package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc

import android.app.Activity
import android.nfc.*
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.EccHybridEncryptionHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.RsaEncryptionHelper
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

        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(),  // CLA
            0xA4.toByte(),  // INS
            0x04.toByte(),  // P1
            0x00.toByte(),  // P2
            0x07.toByte(),  // Lc (Länge der AID)
            *PAYMENT_AID
        )

        private val CONFLICT_APDU = byteArrayOf(
            0x6A.toByte(), 0x82.toByte()
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
                Log.d(TAG, "APDU Response: ${response}")

                if (response.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                    Log.d(TAG, "Verbindung erfolgreich mit Sender")
                    EccHybridEncryptionHelper.generateReceiverKeyPairIfNeeded()
                    val publicKeyString = EccHybridEncryptionHelper.getReceiverPublicKeyAsString()
                    Log.d(TAG, "Öffentlicher Schlüssel generiert: $publicKeyString")
                    val publicKeyBytes = publicKeyString.toByteArray(Charsets.UTF_8)
                    val CLA: Byte = 0x80.toByte()
                    val INS: Byte = 0x10.toByte()
                    val P1: Byte = 0x00
                    val P2: Byte = 0x00
                    val Lc: Byte = publicKeyBytes.size.toByte()
                    val sendPublicKeyApdu = byteArrayOf(CLA, INS, P1, P2, Lc) + publicKeyBytes
                    isoDep.transceive(sendPublicKeyApdu)

                } else {
                    isoDep.transceive(CONFLICT_APDU)
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