package de.throsenheim.oektem.masterarbeit.ma_studipay.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import javax.crypto.Cipher

object RsaEncryptionHelper {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "studi_pay_key"

    // Wir nutzen RSA/ECB/PKCS1Padding (alternativ: "RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    private const val RSA_MODE = "RSA/ECB/PKCS1Padding"

    /**
     * Generiert ein RSA-Schlüsselpaar, falls noch keines existiert.
     */
    fun generateKeyPairIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyPairGenerator =
                KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(1024)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()
            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
        }
    }

    /**
     * Gibt den öffentlichen Schlüssel als Base64-kodierten String zurück.
     * Dieser Schlüssel kann an den Sender übermittelt werden.
     */
    fun getPublicKeyAsString(): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val certificate = keyStore.getCertificate(KEY_ALIAS)
        val publicKey = certificate.publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
    }

    /**
     * Verschlüsselt den Klartext (z.B. Payment Token) mit einem öffentlichen Schlüssel,
     * der als Base64-kodierter String vorliegt.
     *
     * @param plainText Der zu verschlüsselnde Text.
     * @param publicKeyString Der öffentliche Schlüssel als Base64-String.
     * @return Der verschlüsselte Text als Base64-String.
     */
    fun encryptWithPublicKey(plainText: String, publicKeyString: String): String {
        // Dekodiere den öffentlichen Schlüssel
        val publicKeyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
        val keySpec = java.security.spec.X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey: PublicKey = keyFactory.generatePublic(keySpec)
        // Initialisiere den Cipher für Verschlüsselung
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    /**
     * Entschlüsselt den verschlüsselten Text (als Base64-String) mithilfe des privaten Schlüssels
     * aus dem Android Keystore.
     *
     * @param encryptedDataBase64 Der verschlüsselte Text als Base64-String.
     * @return Der entschlüsselte Klartext.
     */
    fun decryptData(encryptedDataBase64: String): String {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null)
        val cipher = Cipher.getInstance(RSA_MODE)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedDataBase64, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}