package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TokenGenerator
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionStatusHolder
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.security.EccHybridEncryptionHelper
import kotlinx.coroutines.runBlocking

class AppHostApduService : HostApduService() {
    private val TAG = "HCEDEMO"
    private val okayMessage: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())
    private val conflictMessage: ByteArray = byteArrayOf(0x6A.toByte(), 0x82.toByte())

    private val gson = Gson()

    private val MAX_FRAGMENT_SIZE = 240 // Maximale Payload-Größe pro Fragment (anpassbar)
    private var encryptedFragments: List<ByteArray> = emptyList()
    private var currentFragmentIndex = 0


    companion object {

        @Volatile
        var isTokenTransmissionAllowed: Boolean = false
    }



    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {

        if (!isTokenTransmissionAllowed) {
            Log.i(TAG, "Tokenübertragung nicht erlaubt, Sender nicht im Sende-Modus")
            return conflictMessage
        }


        if (selectAidApdu(apdu)) {
            Log.i(TAG, "Application selected")
            return okayMessage
        }

        val publicKeyBytes = receivePublicKeyApdu(apdu)
        if (publicKeyBytes != null) {
            val publicKeyString = String(publicKeyBytes, Charsets.UTF_8)
            Log.i(TAG, "Received public key: $publicKeyString")
            return runBlocking {
                try {
                    val paymentToken = TokenGenerator.generateToken(applicationContext)
                    val paymentTokenJson = gson.toJson(paymentToken)
                    Log.i(TAG, "Generated token: $paymentTokenJson")

                    val encryptedTokenString = EccHybridEncryptionHelper.encryptForReceiver(
                        paymentTokenJson,
                        publicKeyString
                    )
                    Log.i(TAG, "Encrypted token: $encryptedTokenString")

                    val tokenBytes = encryptedTokenString.toByteArray(Charsets.UTF_8)
                    if (tokenBytes.size > MAX_FRAGMENT_SIZE) {
                        encryptedFragments = fragmentData(tokenBytes)
                        currentFragmentIndex = 0
                        Log.i(TAG, "Token fragmented into ${encryptedFragments.size} fragments")
                        return@runBlocking encryptedFragments[currentFragmentIndex++]
                    } else {
                        return@runBlocking tokenBytes
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error generating token: ${e.message}")
                    return@runBlocking conflictMessage
                }

            }
        }

        if (isNextFragmentRequest(apdu)) {
            return if (currentFragmentIndex < encryptedFragments.size) {
                Log.i(
                    TAG,
                    "Sending fragment ${currentFragmentIndex + 1} of ${encryptedFragments.size}"
                )
                encryptedFragments[currentFragmentIndex++]
            } else {
                Log.i(TAG, "No more fragments, sending okay message")
                okayMessage
            }
        }

        if (conflictApdu(apdu)) {
            Log.i(TAG, "Conflict")
            return conflictMessage
        }

        return conflictMessage
    }


    private fun fragmentData(data: ByteArray): List<ByteArray> {
        val fragments = mutableListOf<ByteArray>()
        var offset = 0
        val totalLength = data.size
        val totalFragments = (totalLength + MAX_FRAGMENT_SIZE - 1) / MAX_FRAGMENT_SIZE
        while (offset < totalLength) {
            val fragmentSize = minOf(MAX_FRAGMENT_SIZE, totalLength - offset)
            val fragmentIndex = fragments.size + 1 // beginnt bei 1
            val header = byteArrayOf(fragmentIndex.toByte(), totalFragments.toByte())
            val fragmentPayload = data.copyOfRange(offset, offset + fragmentSize)
            fragments.add(header + fragmentPayload)
            offset += fragmentSize
        }
        return fragments
    }


    private fun isNextFragmentRequest(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x80.toByte() && apdu[1] == 0x11.toByte()
    }


    private fun selectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x00.toByte() && apdu[1] == 0xA4.toByte()
    }


    private fun conflictApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x6A.toByte() && apdu[1] == 0x82.toByte()
    }


    private fun receivePublicKeyApdu(apdu: ByteArray): ByteArray? {
        if (apdu.size < 5) return null
        val cla = apdu[0]
        val ins = apdu[1]
        if (cla != 0x80.toByte() || ins != 0x10.toByte()) return null
        val lc = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + lc) return null
        return apdu.copyOfRange(5, 5 + lc)
    }


    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "Deactivated: $reason")
        TransactionStatusHolder.setTransactionStatus()
    }
}
