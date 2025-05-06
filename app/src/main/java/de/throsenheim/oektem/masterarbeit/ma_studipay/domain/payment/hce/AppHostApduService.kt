package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.hce

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TokenGenerator
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionStatusHolder
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.security.EccHybridEncryptionHelper

import kotlinx.coroutines.runBlocking


/**
 * AppHostApduService provides Host Card Emulation (HCE) functionality for payment transactions.
 * It processes incoming APDU commands, handles key exchange and encryption/decryption of payment tokens,
 * fragments large tokens for transmission, and navigates based on transaction outcomes.
 */
class AppHostApduService : HostApduService() {
    // Tag for logging purposes.
    private val TAG = "HCEService"

    // Response APDUs indicating success (0x90, 0x00) or conflict/error (0x6A, 0x82).
    private val okayMessage: ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())
    private val conflictMessage: ByteArray = byteArrayOf(0x6A.toByte(), 0x82.toByte())



    // Maximum allowed payload size for each fragment.
    private val MAX_FRAGMENT_SIZE = 240

    // List of token fragments generated when the encrypted token exceeds the max fragment size.
    private var encryptedFragments: List<ByteArray> = emptyList()

    // Current index for fragment transmission.
    private var currentFragmentIndex = 0

    companion object {
        @Volatile
        var isTokenTransmissionAllowed: Boolean = false

        // Tag for logging is reused from above if needed.
        private const val TAG = "NfcPaymentReceiver"

        // Predefined Application Identifier (AID) for the payment application.
        private val PAYMENT_AID = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )



    }

    /**
     * Processes incoming APDU commands from the NFC reader.
     *
     * Depending on the command, the service:
     * - Checks if transmission is allowed.
     * - Handles selection of the payment application.
     * - Receives a public key from the sender.
     * - Generates a payment token, encrypts it, and sends it in fragments if needed.
     * - Handles requests for subsequent fragments.
     * - Returns a conflict message for unrecognized commands.
     */
    override fun processCommandApdu(apdu: ByteArray, extras: Bundle?): ByteArray {
        // If token transmission is not allowed, immediately return a conflict response.
        if (!isTokenTransmissionAllowed) {
            Log.i(TAG, "Tokenübertragung nicht erlaubt, Sender nicht im Sende-Modus")
            return conflictMessage
        }

        // If the received APDU is a SELECT command (to choose the payment application).
        if (selectAidApdu(apdu)) {
            Log.i(TAG, "Application selected")
            return okayMessage
        }

        // Attempt to receive a public key from the sender.
        val publicKeyBytes = receivePublicKeyApdu(apdu)
        if (publicKeyBytes != null) {
            val publicKeyString = String(publicKeyBytes, Charsets.UTF_8)
            Log.i(TAG, "Received public key: $publicKeyString")
            // Generate a payment token, encrypt it, and handle fragmentation if necessary.
            return runBlocking {
                try {

                    // Generate a new payment token using TokenGenerator.
                    val paymentToken = TokenGenerator.generateToken(applicationContext)

                    Log.i(TAG, "Generated token: $paymentToken")

                    // Encrypt the payment token using ECC hybrid encryption.
                    val encryptedTokenString = EccHybridEncryptionHelper.encryptForReceiver(
                        paymentToken,
                        publicKeyString
                    )
                    Log.i(TAG, "Encrypted token: $encryptedTokenString")

                    val tokenBytes = encryptedTokenString.toByteArray(Charsets.UTF_8)
                    // If the token bytes exceed the allowed fragment size, split them into fragments.
                    if (tokenBytes.size > MAX_FRAGMENT_SIZE) {
                        encryptedFragments = fragmentData(tokenBytes)
                        currentFragmentIndex = 0
                        Log.i(TAG, "Token fragmented into ${encryptedFragments.size} fragments")
                        return@runBlocking encryptedFragments[currentFragmentIndex++]
                    } else {
                        // Otherwise, return the full encrypted token.
                        return@runBlocking tokenBytes
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating token: ${e.message}")
                    return@runBlocking conflictMessage
                }
            }
        }

        // If the incoming APDU is a request for the next token fragment.
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

        // If the APDU indicates a conflict, return the conflict message.
        if (conflictApdu(apdu)) {
            Log.i(TAG, "Conflict")
            return conflictMessage
        }

        // Default return in case of unrecognized command.
        return conflictMessage
    }

    /**
     * Fragments the given data into a list of byte arrays, each with a header indicating its index and total.
     *
     * @param data The data to be fragmented.
     * @return A list of fragments, where each fragment begins with a 2-byte header:
     *         - The first byte is the fragment index (starting at 1).
     *         - The second byte is the total number of fragments.
     */
    private fun fragmentData(data: ByteArray): List<ByteArray> {
        val fragments = mutableListOf<ByteArray>()
        var offset = 0
        val totalLength = data.size
        // Calculate total number of fragments needed.
        val totalFragments = (totalLength + MAX_FRAGMENT_SIZE - 1) / MAX_FRAGMENT_SIZE
        while (offset < totalLength) {
            // Determine the size of the current fragment.
            val fragmentSize = minOf(MAX_FRAGMENT_SIZE, totalLength - offset)
            // Fragment index starts at 1.
            val fragmentIndex = fragments.size + 1
            // Create a header with fragment index and total fragments.
            val header = byteArrayOf(fragmentIndex.toByte(), totalFragments.toByte())
            // Extract the payload for the current fragment.
            val fragmentPayload = data.copyOfRange(offset, offset + fragmentSize)
            // Combine header and payload.
            fragments.add(header + fragmentPayload)
            offset += fragmentSize
        }
        return fragments
    }

    /**
     * Checks if the APDU is a "next fragment request".
     * It returns true if the command matches the expected signature (0x80, 0x11).
     *
     * @param apdu The APDU command received.
     * @return True if the APDU requests the next fragment.
     */
    private fun isNextFragmentRequest(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x80.toByte() && apdu[1] == 0x11.toByte()
    }

    /**
     * Determines if the received APDU is a SELECT AID command.
     * Checks if the first two bytes match the SELECT command signature.
     *
     * @param apdu The APDU command received.
     * @return True if it is a SELECT AID command.
     */
    private fun selectAidApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x00.toByte() && apdu[1] == 0xA4.toByte()
    }

    /**
     * Checks if the APDU is a conflict command.
     * Returns true if the first two bytes match the conflict signature (0x6A, 0x82).
     *
     * @param apdu The APDU command received.
     * @return True if it indicates a conflict.
     */
    private fun conflictApdu(apdu: ByteArray): Boolean {
        return apdu.size >= 2 && apdu[0] == 0x6A.toByte() && apdu[1] == 0x82.toByte()
    }

    /**
     * Extracts the public key bytes from an APDU that contains a public key.
     *
     * The APDU must have at least 5 bytes:
     *   - The first 4 bytes are command headers.
     *   - The 5th byte indicates the length of the public key.
     *
     * @param apdu The APDU command containing the public key.
     * @return A ByteArray containing the public key bytes, or null if the APDU format is invalid.
     */
    private fun receivePublicKeyApdu(apdu: ByteArray): ByteArray? {
        if (apdu.size < 5) return null
        val cla = apdu[0]
        val ins = apdu[1]
        // Verify that the command class and instruction bytes match the expected public key command.
        if (cla != 0x80.toByte() || ins != 0x10.toByte()) return null
        val lc = apdu[4].toInt() and 0xFF
        if (apdu.size < 5 + lc) return null
        return apdu.copyOfRange(5, 5 + lc)
    }

    /**
     * Called when the NFC reader is deactivated.
     *
     * Logs the deactivation reason and sets the transaction status to FINISHED.
     *
     * @param reason The reason code for deactivation.
     */
    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "Deactivated: $reason")
        TransactionStatusHolder.setTransactionStatus()
    }
}
