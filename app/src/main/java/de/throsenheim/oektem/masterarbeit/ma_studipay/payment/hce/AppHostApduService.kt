package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token.TokenGenerator
import kotlinx.coroutines.runBlocking

public class AppHostApduService : HostApduService() {
    private var messageCounter = 0
    private val okayMessage: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())
    private val conflictMessage: ByteArray = byteArrayOf(0x6A.toByte(), 0x82.toByte())
    private val neutrumMessage: ByteArray = byteArrayOf(0x00.toByte(), 0x00.toByte())

    private val gson = Gson()
    private var paymentTokenString: String = ""


    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {
        if (selectAidApdu(apdu)) {
            Log.i("HCEDEMO", "Application selected")
            return okayMessage
        }

        val publicKeyBytes = receivePublicKeyApdu(apdu)
        if (publicKeyBytes != null) {
            val publicKeyString = String(publicKeyBytes, Charsets.UTF_8)
            Log.i("HCEDEMO", "Received public key: $publicKeyString")
            runBlocking {
                try {
                    val paymentToken = TokenGenerator.generateToken(applicationContext)
                    paymentTokenString = gson.toJson(paymentToken)
                    Log.i("HCEDEMO", "Generated token: $paymentTokenString")
                    // Hier kannst du den Token weiter verarbeiten oder verschicken
                } catch (e: Exception) {
                    Log.e("HCEDEMO", "Fehler beim Generieren des Tokens: ${e.message}")
                }
            }
            // Hier kannst du den Schlüssel speichern oder weiterverarbeiten
            return okayMessage
        }


        if (conflictApdu(apdu)) {
            Log.i("HCEDEMO", "Conflict")
            return conflictMessage
        }



        return conflictMessage

    }

    // Hilfsmethode zum Extrahieren des öffentlichen Schlüssels
    private fun receivePublicKeyApdu(apdu: ByteArray): ByteArray? {
        // Mindestens 5 Bytes für den Header (CLA, INS, P1, P2, Lc)
        if (apdu.size < 5) return null
        val cla = apdu[0]
        val ins = apdu[1]
        // Definieren: CLA = 0x80, INS = 0x10 signalisiert Public-Key-Übertragung
        if (cla != 0x80.toByte() || ins != 0x10.toByte()) return null

        // Das Lc-Feld steht an Position 4 und gibt die Länge der Daten an
        val lc = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + lc) return null

        // Extrahiere den Datenbereich, der den öffentlichen Schlüssel enthält
        return apdu.copyOfRange(5, 5 + lc)
    }


    private fun selectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0.toByte() && apdu[1] == 0xa4.toByte()
    }
    private fun conflictApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x6A.toByte() && apdu[1] == 0x82.toByte()
    }
    override fun onDeactivated(reason: Int) {
        Log.i("HCEDEMO", "Deactivated: $reason")
    }
}