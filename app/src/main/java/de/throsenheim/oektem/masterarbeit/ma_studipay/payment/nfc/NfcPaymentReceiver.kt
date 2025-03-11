package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc

import android.app.Activity
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.security.EccHybridEncryptionHelper
import java.io.IOException

class NfcPaymentReceiver(private val activity: Activity) {
    private val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(activity)

    companion object {
        private const val TAG = "NfcPaymentReceiver"
        private val PAYMENT_AID = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        // APDU zum Starten der Kommunikation (SELECT)
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(),
            *PAYMENT_AID
        )

        // APDU, um das nächste Fragment anzufordern: CLA = 0x80, INS = 0x11
        private val NEXT_FRAGMENT_APDU = byteArrayOf(0x80.toByte(), 0x11.toByte())

        // Konflikt-APDU
        private val CONFLICT_APDU = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        // APDU zum Senden des Public Keys: CLA = 0x80, INS = 0x10
        // Aufbau: [CLA, INS, P1, P2, Lc] + PublicKeyBytes
        private const val PUBLIC_KEY_CLA: Byte = 0x80.toByte()
        private const val PUBLIC_KEY_INS: Byte = 0x10.toByte()
        private const val PUBLIC_KEY_P1: Byte = 0x00
        private const val PUBLIC_KEY_P2: Byte = 0x00
    }

    fun enableNfcReader() {
        nfcAdapter?.let {
            val options = Bundle()
            options.putInt(android.nfc.NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            it.enableReaderMode(
                activity,
                { tag -> handleTagDiscovered(tag) },
                android.nfc.NfcAdapter.FLAG_READER_NFC_A or android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
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
                // Zunächst SELECT_APDU senden, um den HCE-Dienst zu aktivieren
                val selectResponse = isoDep.transceive(SELECT_APDU)
                Log.d(TAG, "Select Response: ${selectResponse.toHexString()}")
                if (!selectResponse.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                    isoDep.transceive(CONFLICT_APDU)
                    return
                }
                Log.d(TAG, "Verbindung erfolgreich – Public Key senden")
                EccHybridEncryptionHelper.generateReceiverKeyPairIfNeeded()
                // Hole deinen öffentlichen Schlüssel (aus deinem HCE-Token-Generator, z. B. aus EccHybridEncryptionHelper)
                val publicKeyString = EccHybridEncryptionHelper.getReceiverPublicKeyAsString()
                Log.d(TAG, "Öffentlicher Schlüssel: $publicKeyString")
                val publicKeyBytes = publicKeyString.toByteArray(Charsets.UTF_8)
                val Lc: Byte = publicKeyBytes.size.toByte()
                val sendPublicKeyApdu = byteArrayOf(
                    PUBLIC_KEY_CLA,
                    PUBLIC_KEY_INS,
                    PUBLIC_KEY_P1,
                    PUBLIC_KEY_P2,
                    Lc
                ) + publicKeyBytes
                // Sende den Public-Key-APDU-Befehl und erhalte das erste Fragment des verschlüsselten Tokens
                val firstFragment = isoDep.transceive(sendPublicKeyApdu)
                Log.d(TAG, "Erstes Fragment empfangen: ${firstFragment.toHexString()}")
                // Empfange alle Fragmente und setze sie zusammen:
                val fullEncryptedToken = reassembleFragments(isoDep, firstFragment)
                Log.d(TAG, "Vollständiger verschlüsselter Token: $fullEncryptedToken")
                // Optional: Entschlüsseln
                val decryptedToken = EccHybridEncryptionHelper.decryptFromSender(fullEncryptedToken)
                Log.d(TAG, "Entschlüsselter Token: $decryptedToken")
            } catch (e: IOException) {
                Log.e(TAG, "Kommunikationsfehler: ${e.message}")
            }
        }
    }

    /**
     * Liest alle Fragmente des verschlüsselten Tokens vom HCE-Dienst ein und fügt sie zusammen.
     * Jedes Fragment hat einen 2-Byte-Header:
     * - Byte 0: Fragmentindex (beginnend bei 1)
     * - Byte 1: Gesamtzahl der Fragmente
     */
    private fun reassembleFragments(isoDep: IsoDep, firstFragment: ByteArray): String {
        if (firstFragment.size < 2) throw IllegalArgumentException("Fragment zu kurz")
        val totalFragments = firstFragment[1].toInt() and 0xFF
        val payloadList = mutableListOf<ByteArray>()
        // Füge Payload des ersten Fragments hinzu (Header überspringen)
        payloadList.add(firstFragment.copyOfRange(2, firstFragment.size))
        Log.d(
            TAG,
            "Fragment 1 von $totalFragments empfangen, Payload-Länge: ${firstFragment.size - 2}"
        )

        // Fordere alle weiteren Fragmente an, falls erforderlich
        for (i in 2..totalFragments) {
            val fragResponse = isoDep.transceive(NEXT_FRAGMENT_APDU)
            if (fragResponse.size < 2) throw IllegalArgumentException("Fragment $i zu kurz")
            val fragIndex = fragResponse[0].toInt() and 0xFF
            val fragTotal = fragResponse[1].toInt() and 0xFF
            if (fragTotal != totalFragments) {
                throw IllegalArgumentException("Inkonsistente Gesamtfragmente: erwartet $totalFragments, erhalten $fragTotal")
            }
            if (fragIndex != i) {
                throw IllegalArgumentException("Erwartetes Fragment $i, aber erhalten: $fragIndex")
            }
            payloadList.add(fragResponse.copyOfRange(2, fragResponse.size))
            Log.d(TAG, "Fragment $i empfangen, Payload-Länge: ${fragResponse.size - 2}")
        }
        // Füge alle Payloads zusammen
        val fullPayload = payloadList.fold(byteArrayOf()) { acc, bytes -> acc + bytes }
        return String(fullPayload, Charsets.UTF_8)
    }

    // Hilfsfunktion, um ein Byte-Array als Hexadezimal-String darzustellen.
    private fun ByteArray.toHexString() = joinToString("") { "%02X".format(it) }
}
