package de.throsenheim.oektem.masterarbeit.ma_studipay.security

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

object EccHybridEncryptionHelper {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val RECEIVER_KEY_ALIAS = "studi_pay_ecc_key"

    // Parameter für AES-GCM
    private const val AES_KEY_SIZE = 16       // 128 Bit
    private const val GCM_IV_LENGTH = 12      // 12 Bytes
    private const val GCM_TAG_LENGTH = 128    // in Bits

    // Fallback-KeyPair, wenn kein Keystore verwendet wird (für Geräte unter API 31)
    private var fallbackReceiverKeyPair: KeyPair? = null

    /**
     * Generiert ein ECC-Schlüsselpaar für den Empfänger.
     * Auf API ≥ 31 wird das KeyPair im Android Keystore generiert,
     * auf älteren Geräten wird es außerhalb des Keystores erzeugt.
     */
    fun generateReceiverKeyPairIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(RECEIVER_KEY_ALIAS)) {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE
                )
                val parameterSpec = KeyGenParameterSpec.Builder(
                    RECEIVER_KEY_ALIAS,
                    KeyProperties.PURPOSE_AGREE_KEY
                )
                    .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
                keyPairGenerator.initialize(parameterSpec)
                keyPairGenerator.generateKeyPair()
            }
        } else {
            // Fallback: Schlüssel außerhalb des Keystores generieren und im Speicher halten
            if (fallbackReceiverKeyPair == null) {
                val keyPairGenerator = KeyPairGenerator.getInstance("EC")
                keyPairGenerator.initialize(ECGenParameterSpec("secp256r1"))
                fallbackReceiverKeyPair = keyPairGenerator.generateKeyPair()
            }
        }
    }

    /**
     * Gibt den öffentlichen Schlüssel des Empfängers als Base64-kodierten String zurück.
     * Wenn der Schlüssel im Keystore generiert wurde (API ≥ 31), wird dieser verwendet.
     * Andernfalls wird der Fallback-Schlüssel genutzt.
     */
    fun getReceiverPublicKeyAsString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val certificate = keyStore.getCertificate(RECEIVER_KEY_ALIAS)
            val publicKey = certificate.publicKey
            Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        } else {
            fallbackReceiverKeyPair?.let {
                Base64.encodeToString(it.public.encoded, Base64.NO_WRAP)
            } ?: throw IllegalStateException("Key pair not generated")
        }
    }

    /**
     * Sender-seitige Verschlüsselung:
     * - Dekodiert den Base64-kodierten öffentlichen Schlüssel des Empfängers.
     * - Generiert ein ephemeral (flüchtiges) ECC-Schlüsselpaar.
     * - Leitet mittels ECDH einen gemeinsamen Geheimwert ab.
     * - Leitet daraus einen AES-Schlüssel ab (SHA-256, erste 16 Byte für AES-128).
     * - Verschlüsselt den Payment Token mit AES-GCM.
     *
     * Das zurückgegebene Paket enthält den ephemeral öffentlichen Schlüssel, den IV und den Ciphertext,
     * getrennt durch Doppelpunkte (Format: Base64(ephemeralPublicKey):Base64(iv):Base64(ciphertext)).
     */
    fun encryptForReceiver(plainText: String, receiverPublicKeyString: String): String {
        // Receiver Public Key dekodieren
        val receiverPublicKey = decodePublicKey(receiverPublicKeyString)

        // Erzeuge ein ephemeral ECC-Schlüsselpaar (nicht im Keystore, nur temporär)
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val ephemeralKeyPair: KeyPair = kpg.generateKeyPair()
        val ephemeralPrivateKey = ephemeralKeyPair.private
        val ephemeralPublicKey = ephemeralKeyPair.public

        // ECDH: Ableiten des gemeinsamen Geheimwerts
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(ephemeralPrivateKey)
        keyAgreement.doPhase(receiverPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Ableiten eines AES-Schlüssels: SHA-256 des gemeinsamen Geheimwerts; die ersten 16 Byte für AES-128
        val digest = MessageDigest.getInstance("SHA-256")
        val aesKeyBytesFull = digest.digest(sharedSecret)
        val aesKeyBytes = aesKeyBytesFull.copyOf(AES_KEY_SIZE)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // AES-GCM-Verschlüsselung: Generiere einen zufälligen IV
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Verpacke ephemeralPublicKey, IV und Ciphertext als Base64-Strings, getrennt durch ':'
        val ephemeralPublicKeyString =
            Base64.encodeToString(ephemeralPublicKey.encoded, Base64.NO_WRAP)
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherTextString = Base64.encodeToString(cipherText, Base64.NO_WRAP)

        return "$ephemeralPublicKeyString:$ivString:$cipherTextString"
    }

    /**
     * Receiver-seitige Entschlüsselung:
     * Erwartet ein Paket im Format:
     * Base64(ephemeralPublicKey):Base64(iv):Base64(ciphertext)
     *
     * Der Empfänger nutzt seinen privaten Schlüssel (aus dem Keystore oder Fallback),
     * leitet mittels ECDH den gemeinsamen Geheimwert ab, erzeugt den AES-Schlüssel
     * und entschlüsselt den Payment Token.
     */
    fun decryptFromSender(encryptedPackage: String): String {
        val parts = encryptedPackage.split(":")
        if (parts.size != 3) {
            throw IllegalArgumentException("Ungültiges verschlüsseltes Paketformat.")
        }
        val ephemeralPublicKeyEncoded = parts[0]
        val ivEncoded = parts[1]
        val cipherTextEncoded = parts[2]

        // Dekodiere den ephemeral öffentlichen Schlüssel
        val ephemeralPublicKey = decodePublicKey(ephemeralPublicKeyEncoded)
        val iv = Base64.decode(ivEncoded, Base64.NO_WRAP)
        val cipherText = Base64.decode(cipherTextEncoded, Base64.NO_WRAP)

        // Hole den privaten Schlüssel des Empfängers
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val privateKey: PrivateKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            keyStore.getKey(RECEIVER_KEY_ALIAS, null) as PrivateKey
        } else {
            fallbackReceiverKeyPair?.private
                ?: throw IllegalStateException("Key pair not generated")
        }

        // ECDH: Ableiten des gemeinsamen Geheimwerts
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(ephemeralPublicKey, true)
        val sharedSecret = keyAgreement.generateSecret()

        // Ableiten eines AES-Schlüssels
        val digest = MessageDigest.getInstance("SHA-256")
        val aesKeyBytesFull = digest.digest(sharedSecret)
        val aesKeyBytes = aesKeyBytesFull.copyOf(AES_KEY_SIZE)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")

        // AES-GCM Entschlüsselung
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }

    /**
     * Dekodiert einen Base64-kodierten EC-öffentlichen Schlüssel in ein PublicKey-Objekt.
     */
    fun decodePublicKey(publicKeyString: String): PublicKey {
        val publicKeyBytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(keySpec)
    }
}
