package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * EccHybridEncryptionHelper provides functionality for hybrid encryption using Elliptic Curve Cryptography (ECC)
 * for key exchange and AES-GCM for symmetric encryption. It supports two modes:
 *
 * 1. For devices running Android S (API level 31) or later, it uses the AndroidKeyStore to generate and store
 *    a persistent receiver key pair.
 * 2. For devices running an earlier version of Android, it uses a fallback in-memory key pair.
 *
 * The encryption algorithm works as follows:
 *   - A temporary (ephemeral) key pair is generated.
 *   - The sender calculates a shared secret with the receiver's public key using ECDH.
 *   - A SHA-256 hash is applied to derive a 128-bit AES key from the shared secret.
 *   - The plaintext is encrypted using AES in GCM mode with a random IV.
 *   - The resulting package contains the ephemeral public key, the IV, and the ciphertext,
 *     all encoded as Base64 strings and delimited by colons.
 *
 * Decryption reverses the above process using the receiver's private key.
 */
object EccHybridEncryptionHelper {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val RECEIVER_KEY_ALIAS = "studi_pay_ecc_key"
    private const val AES_KEY_SIZE = 16       // AES key size in bytes (16 bytes for 128-bit AES)
    private const val GCM_IV_LENGTH =
        12      // Initialization Vector (IV) length for GCM mode (12 bytes is standard)
    private const val GCM_TAG_LENGTH = 128    // Authentication tag length in bits (128 bits)

    // Fallback key pair for devices running pre-Android S (API level < 31)
    private var fallbackReceiverKeyPair: KeyPair? = null

    /**
     * Generates a receiver's ECC key pair if it does not already exist.
     *
     * For Android S and above, the key pair is generated and stored in the AndroidKeyStore for persistent use.
     * For older Android versions, an in-memory fallback key pair is generated.
     */
    fun generateReceiverKeyPairIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For devices running Android S or later, use the AndroidKeyStore.
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            // Check if a key with the specified alias already exists.
            if (!keyStore.containsAlias(RECEIVER_KEY_ALIAS)) {
                // Create an EC key pair generator instance specifying the AndroidKeyStore.
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE
                )
                // Prepare key generation parameters: using EC with curve "secp256r1" and SHA-256 digest.
                val parameterSpec = KeyGenParameterSpec.Builder(
                    RECEIVER_KEY_ALIAS,
                    KeyProperties.PURPOSE_AGREE_KEY
                )
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
                // Initialize and generate the key pair.
                keyPairGenerator.initialize(parameterSpec)
                keyPairGenerator.generateKeyPair()
            }
        } else {
            // For devices running pre-Android S, generate an ephemeral fallback key pair if not already generated.
            if (fallbackReceiverKeyPair == null) {
                val keyPairGenerator = KeyPairGenerator.getInstance("EC")
                keyPairGenerator.initialize(ECGenParameterSpec("secp256r1"))
                fallbackReceiverKeyPair = keyPairGenerator.generateKeyPair()
            }
        }
    }

    /**
     * Retrieves the receiver's public key as a Base64-encoded string.
     *
     * For Android S and above, the key is retrieved from the AndroidKeyStore.
     * For older versions, the fallback key pair is used.
     *
     * @return The Base64 encoded public key without line breaks.
     * @throws IllegalStateException if the key pair is not generated.
     */
    fun getReceiverPublicKeyAsString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Load the AndroidKeyStore and get the certificate corresponding to the alias.
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val certificate = keyStore.getCertificate(RECEIVER_KEY_ALIAS)
            val publicKey = certificate.publicKey
            // Encode the public key in Base64 format.
            Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        } else {
            // Use the fallback key pair on older devices.
            fallbackReceiverKeyPair?.let {
                Base64.encodeToString(it.public.encoded, Base64.NO_WRAP)
            } ?: throw IllegalStateException("Key pair not generated")
        }
    }

    /**
     * Encrypts a given plaintext for the receiver using hybrid encryption.
     *
     * The method generates an ephemeral EC key pair for the sender and computes a shared secret
     * using ECDH with the receiver's public key, which is provided as a Base64 string.
     * The shared secret is hashed via SHA-256 to derive a 128-bit AES key.
     * The plaintext is then encrypted with AES/GCM/NoPadding.
     *
     * @param plainText The plaintext message to be encrypted.
     * @param receiverPublicKeyString The receiver's public key as a Base64 encoded string.
     * @return A string containing the Base64-encoded ephemeral public key, IV, and ciphertext, separated by colons.
     */
    fun encryptForReceiver(plainText: String, receiverPublicKeyString: String): String {
        // Decode the receiver's public key from its Base64-encoded string.
        val receiverPublicKey = decodePublicKey(receiverPublicKeyString)

        // Generate an ephemeral EC key pair for the sender.
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val ephemeralKeyPair: KeyPair = kpg.generateKeyPair()
        val ephemeralPrivateKey = ephemeralKeyPair.private
        val ephemeralPublicKey = ephemeralKeyPair.public

        // Perform Elliptic Curve Diffie-Hellman (ECDH) key agreement to generate a shared secret.
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(ephemeralPrivateKey)
        keyAgreement.doPhase(receiverPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Derive a 128-bit AES key from the shared secret using SHA-256.
        val digest = MessageDigest.getInstance("SHA-256")
        val aesKeyBytesFull = digest.digest(sharedSecret)
        val aesKeyBytes = aesKeyBytesFull.copyOf(AES_KEY_SIZE)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // Initialize the AES cipher in GCM mode with no padding.
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Generate a random IV of standard length (12 bytes).
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
        // Encrypt the plaintext.
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Encode the ephemeral public key, IV, and ciphertext into Base64 strings.
        val ephemeralPublicKeyString =
            Base64.encodeToString(ephemeralPublicKey.encoded, Base64.NO_WRAP)
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherTextString = Base64.encodeToString(cipherText, Base64.NO_WRAP)

        // Return the three components separated by colons.
        return "$ephemeralPublicKeyString:$ivString:$cipherTextString"
    }

    /**
     * Decrypts an encrypted package received from the sender.
     *
     * The encrypted package should contain three Base64-encoded parts: the ephemeral public key,
     * the IV, and the ciphertext, separated by colons.
     *
     * The method decodes the ephemeral public key, computes the shared secret via ECDH using the receiver's
     * private key, derives the AES key via SHA-256, and then decrypts the ciphertext using AES/GCM/NoPadding.
     *
     * @param encryptedPackage The encrypted package string.
     * @return The decrypted plaintext message.
     * @throws IllegalArgumentException if the encrypted package format is invalid.
     */
    fun decryptFromSender(encryptedPackage: String): String {
        // Split the package into its three components.
        val parts = encryptedPackage.split(":")
        if (parts.size != 3) {
            throw IllegalArgumentException("Ungültiges verschlüsseltes Paketformat.")
        }
        // Extract the Base64-encoded ephemeral public key, IV, and ciphertext.
        val ephemeralPublicKeyEncoded = parts[0]
        val ivEncoded = parts[1]
        val cipherTextEncoded = parts[2]

        // Decode the ephemeral public key and convert it to a PublicKey object.
        val ephemeralPublicKey = decodePublicKey(ephemeralPublicKeyEncoded)
        // Decode the IV and ciphertext from Base64.
        val iv = Base64.decode(ivEncoded, Base64.NO_WRAP)
        val cipherText = Base64.decode(cipherTextEncoded, Base64.NO_WRAP)

        // Load the AndroidKeyStore to retrieve the receiver's private key.
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        // Get the receiver's private key depending on Android version.
        val privateKey: PrivateKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            keyStore.getKey(RECEIVER_KEY_ALIAS, null) as PrivateKey
        } else {
            fallbackReceiverKeyPair?.private
                ?: throw IllegalStateException("Key pair not generated")
        }

        // Perform ECDH key agreement using the receiver's private key and the sender's ephemeral public key.
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(ephemeralPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Derive the AES key by applying SHA-256 to the shared secret.
        val digest = MessageDigest.getInstance("SHA-256")
        val aesKeyBytesFull = digest.digest(sharedSecret)
        val aesKeyBytes = aesKeyBytesFull.copyOf(AES_KEY_SIZE)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // Initialize the AES cipher in GCM mode with the provided IV.
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
        // Decrypt the ciphertext.
        val plainTextBytes = cipher.doFinal(cipherText)
        // Convert the decrypted bytes to a string using UTF-8.
        return String(plainTextBytes, Charsets.UTF_8)
    }

    /**
     * Decodes a Base64-encoded public key string to a PublicKey object.
     *
     * @param publicKeyString The Base64 encoded public key.
     * @return The PublicKey object corresponding to the encoded string.
     */
    private fun decodePublicKey(publicKeyString: String): PublicKey {
        // Decode the public key bytes from Base64.
        val publicKeyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        // Create an X509EncodedKeySpec from the byte array.
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        // Get an EC KeyFactory instance.
        val keyFactory = KeyFactory.getInstance("EC")
        // Generate and return the public key.
        return keyFactory.generatePublic(keySpec)
    }
}
