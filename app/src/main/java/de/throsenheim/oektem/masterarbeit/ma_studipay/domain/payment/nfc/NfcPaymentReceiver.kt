package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.nfc

import android.app.Activity
import android.content.Context
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.google.gson.Gson
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TokenExtractor
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionOutcome
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.security.EccHybridEncryptionHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * NfcPaymentReceiver handles NFC operations in the context of payment reception.
 * It listens for NFC tags, exchanges commands, obtains an encrypted token,
 * decrypts it, and triggers navigation based on the transaction outcome.
 */
class NfcPaymentReceiver(private val activity: Activity) {

    // The amount to be transacted is set externally.
    var amount: Double = 0.0

    // The default NFC adapter used to interact with NFC hardware.
    private val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(activity)

    // LiveData that indicates whether a payment token was received and processed.
    val tokenReceivedLiveData = MutableLiveData<Boolean>()

    // Gson instance to convert between JSON strings and PaymentToken objects.
    val gson = Gson()

    // Define a CoroutineScope for IO-bound operations.
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        // Tag used for logging.
        private const val TAG = "NfcPaymentReceiver"

        // Predefined Application Identifier (AID) for the payment application.
        private val PAYMENT_AID = byteArrayOf(
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        // APDU command to select the payment AID.
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(),
            *PAYMENT_AID
        )

        // APDU command to request the next fragment of data.
        private val NEXT_FRAGMENT_APDU = byteArrayOf(0x80.toByte(), 0x11.toByte())

        // APDU command to indicate conflict (e.g., incorrect selection).
        private val CONFLICT_APDU = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        // Constants for constructing a command APDU to send the public key.
        private const val PUBLIC_KEY_CLA: Byte = 0x80.toByte()
        private const val PUBLIC_KEY_INS: Byte = 0x10.toByte()
        private const val PUBLIC_KEY_P1: Byte = 0x00
        private const val PUBLIC_KEY_P2: Byte = 0x00
    }

    /**
     * Enables the NFC reader mode to start listening for NFC tags.
     *
     * Configures the NFC adapter with a delay for presence checking and a callback
     * that processes discovered tags.
     */
    fun enableNfcReader() {
        nfcAdapter?.let {
            // Prepare options to check for tag presence with a specified delay.
            val options = Bundle()
            options.putInt(android.nfc.NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 50000)
            // Enable NFC reader mode with flags to detect NFC-A tags and skip NDEF checks.
            it.enableReaderMode(
                activity,
                { tag -> handleTagDiscovered(tag) }, // Callback for discovered tags.
                android.nfc.NfcAdapter.FLAG_READER_NFC_A or android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    /**
     * Disables the NFC reader mode.
     */
    fun disableNfcReader() {
        nfcAdapter?.disableReaderMode(activity)
    }

    /**
     * Handles an NFC tag when it is discovered.
     *
     * The method:
     *  - Logs tag discovery.
     *  - Connects to the tag using IsoDep.
     *  - Sends a SELECT APDU to choose the payment application.
     *  - Checks the response to ensure a successful selection.
     *  - Generates the receiver's key pair if needed and sends the public key using an APDU.
     *  - Reassembles the encrypted token fragments, decrypts the token,
     *    and posts the success state to tokenReceivedLiveData.
     *  - Depending on the token's content and amount, launches subsequent operations in a coroutine.
     *
     * @param tag The discovered NFC Tag.
     */
    private fun handleTagDiscovered(tag: Tag) {
        Log.d(TAG, "NFC Tag entdeckt: ${tag.id.toHexString()}")
        // Use IsoDep technology to communicate with ISO-DEP compliant NFC tags.
        IsoDep.get(tag)?.use { isoDep ->
            try {
                isoDep.connect()
                // Send SELECT_APDU to select the payment application.
                val selectResponse = isoDep.transceive(SELECT_APDU)
                Log.d(TAG, "Select Response: ${selectResponse.toHexString()}")
                // Check if the selection was successful (status 0x90, 0x00).
                if (!selectResponse.contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))) {
                    // In case of failure, send a conflict APDU.
                    isoDep.transceive(CONFLICT_APDU)
                    return
                }
                Log.d(TAG, "Verbindung erfolgreich – Public Key senden")
                // Ensure the receiver's key pair is generated.
                EccHybridEncryptionHelper.generateReceiverKeyPairIfNeeded()
                // Retrieve the receiver's public key as a Base64 encoded string.
                val publicKeyString = EccHybridEncryptionHelper.getReceiverPublicKeyAsString()
                Log.d(TAG, "Öffentlicher Schlüssel: $publicKeyString")
                // Prepare the public key bytes and construct an APDU command to send it.
                val publicKeyBytes = publicKeyString.toByteArray(Charsets.UTF_8)
                val keyLength: Byte = publicKeyBytes.size.toByte()
                val sendPublicKeyApdu = byteArrayOf(
                    PUBLIC_KEY_CLA,
                    PUBLIC_KEY_INS,
                    PUBLIC_KEY_P1,
                    PUBLIC_KEY_P2,
                    keyLength
                ) + publicKeyBytes
                // Send the APDU command and receive the first fragment.
                val firstFragment = isoDep.transceive(sendPublicKeyApdu)
                Log.d(TAG, "Erstes Fragment empfangen: ${firstFragment.toHexString()}")
                // Reassemble all fragments to form the full encrypted token.
                val fullEncryptedToken = reassembleFragments(isoDep, firstFragment)
                Log.d(TAG, "Vollständiger verschlüsselter Token: $fullEncryptedToken")
                // Decrypt the token using ECC hybrid decryption.
                val decryptedToken = EccHybridEncryptionHelper.decryptFromSender(fullEncryptedToken)
                Log.d(TAG, "Entschlüsselter Token: $decryptedToken")
                // Update LiveData to indicate token was successfully received.
                tokenReceivedLiveData.postValue(true)
                // Convert the decrypted JSON string to a PaymentToken object.
                val paymentToken: PaymentToken =
                    gson.fromJson(decryptedToken, PaymentToken::class.java)
                // Retrieve the current user's matriculation number from SharedPreferences.
                val sharedPreferences =
                    activity.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val matriculationNumber = sharedPreferences.getString("current_username", null)
                Log.d(TAG, "Amount:" + amount.toString())
                // Launch a coroutine on the IO dispatcher to process the token further.
                scope.launch {
                    UiHelper.updateDatabase(activity)
                    if (matriculationNumber != paymentToken.matriculationNumber) {
                        // Extract the transaction outcome (Success or Rejection) based on risk assessment.
                        val outcome =
                            TokenExtractor.extractTokenFromResponse(activity, amount, paymentToken)
                        // If the outcome is Success, navigate to the payment success screen.
                        if (outcome == TransactionOutcome.Success) {
                            withContext(Dispatchers.Main) {
                                (activity as? FragmentActivity)?.let { fragmentActivity ->
                                    val navController = Navigation.findNavController(
                                        fragmentActivity,
                                        R.id.nav_host_fragment
                                    )
                                    navController.navigate(
                                        R.id.paymentSuccessFragment,
                                        Bundle().apply {
                                            putDouble("amount", amount)
                                            putString("sender", paymentToken.firstName)
                                        }
                                    )
                                }
                            }
                        } else {
                            // Otherwise, navigate to the payment failed screen.
                            withContext(Dispatchers.Main) {
                                (activity as? FragmentActivity)?.let { fragmentActivity ->
                                    val navController = Navigation.findNavController(
                                        fragmentActivity,
                                        R.id.nav_host_fragment
                                    )
                                    navController.navigate(R.id.paymentFailedFragment)
                                }
                            }
                        }
                    } else {
                        // If the matriculation number matches, navigate to the payment failed screen.
                        withContext(Dispatchers.Main) {
                            (activity as? FragmentActivity)?.let { fragmentActivity ->
                                val navController = Navigation.findNavController(
                                    fragmentActivity,
                                    R.id.nav_host_fragment
                                )
                                navController.navigate(R.id.paymentFailedFragment)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Kommunikationsfehler: ${e.message}")
            }
        }
    }

    /**
     * Reassembles fragments of an encrypted token received via IsoDep.
     *
     * The first fragment includes header information that indicates the total number of fragments.
     * Subsequent fragments are then retrieved using a NEXT_FRAGMENT_APDU command.
     *
     * @param isoDep The IsoDep instance used for NFC communication.
     * @param firstFragment The first fragment of the encrypted token.
     * @return The full concatenated payload as a UTF-8 String.
     * @throws IllegalArgumentException if any fragment is too short or fragment counts are inconsistent.
     */
    private fun reassembleFragments(isoDep: IsoDep, firstFragment: ByteArray): String {
        if (firstFragment.size < 2) throw IllegalArgumentException("Fragment zu kurz")
        // The second byte of the first fragment indicates the total number of fragments.
        val totalFragments = firstFragment[1].toInt() and 0xFF
        val payloadList = mutableListOf<ByteArray>()
        // The payload starts after the first two bytes.
        payloadList.add(firstFragment.copyOfRange(2, firstFragment.size))
        Log.d(
            TAG,
            "Fragment 1 von $totalFragments empfangen, Payload-Länge: ${firstFragment.size - 2}"
        )
        // Request and collect all subsequent fragments.
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
        // Concatenate all payload fragments into a single byte array.
        val fullPayload = payloadList.fold(byteArrayOf()) { acc, bytes -> acc + bytes }
        // Convert the byte array to a UTF-8 encoded string.
        return String(fullPayload, Charsets.UTF_8)
    }

    /**
     * Extension function to convert a ByteArray to a hexadecimal string.
     */
    private fun ByteArray.toHexString() = joinToString("") { "%02X".format(it) }
}
