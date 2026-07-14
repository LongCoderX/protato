package com.protato.app

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONObject

internal data class EncryptedSecretPayload(
    val iv: ByteArray,
    val ciphertext: ByteArray
)

internal class SecretDecryptionException(cause: Throwable) :
    IllegalStateException("密钥不可解密", cause)

internal interface SecretCipher {
    fun encrypt(plaintext: ByteArray): EncryptedSecretPayload

    fun decrypt(payload: EncryptedSecretPayload): ByteArray
}

internal class AndroidKeystoreSecretCipher : SecretCipher {
    override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return EncryptedSecretPayload(cipher.iv, cipher.doFinal(plaintext))
    }

    override fun decrypt(payload: EncryptedSecretPayload): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, payload.iv)
        )
        return cipher.doFinal(payload.ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "protato.secret-store.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}

internal class EncryptedSecretStore(
    private val encryptedFile: File,
    private val legacyPlaintextFile: File,
    private val cipher: SecretCipher
) : SecretStore {
    private var writesBlocked = false
    private var encryptedFileVerified = !encryptedFile.exists()

    companion object {
        fun inDirectory(filesDir: File): EncryptedSecretStore {
            return EncryptedSecretStore(
                encryptedFile = File(filesDir, ENCRYPTED_SECRET_FILE_NAME),
                legacyPlaintextFile = File(filesDir, SECRET_FILE_NAME),
                cipher = AndroidKeystoreSecretCipher()
            )
        }
    }

    override fun load(): ProtatoSecrets {
        if (encryptedFile.exists()) {
            return runCatching { readEncrypted() }
                .onSuccess {
                    writesBlocked = false
                    encryptedFileVerified = true
                    legacyPlaintextFile.delete()
                }
                .getOrElse {
                    writesBlocked = true
                    throw SecretDecryptionException(it)
                }
        }
        if (!legacyPlaintextFile.exists()) return ProtatoSecrets()

        val legacySecrets = readPlaintextSecrets(legacyPlaintextFile)
        runCatching {
            writeEncrypted(legacySecrets)
            check(legacyPlaintextFile.delete()) {
                "Unable to delete migrated plaintext secret file."
            }
        }
        return legacySecrets
    }

    override fun save(secrets: ProtatoSecrets) {
        if (encryptedFile.exists() && !encryptedFileVerified) load()
        check(!writesBlocked) {
            "Encrypted secrets could not be decrypted; refusing to overwrite them."
        }
        writeEncrypted(secrets)
        encryptedFileVerified = true
        if (legacyPlaintextFile.exists()) {
            check(legacyPlaintextFile.delete()) {
                "Unable to delete migrated plaintext secret file."
            }
        }
    }

    private fun readEncrypted(): ProtatoSecrets {
        val root = JSONObject(encryptedFile.readText())
        require(root.getInt("version") == ENCRYPTED_SECRET_VERSION) {
            "Unsupported encrypted secret version."
        }
        val payload = EncryptedSecretPayload(
            iv = Base64.getDecoder().decode(root.getString("iv")),
            ciphertext = Base64.getDecoder().decode(root.getString("ciphertext"))
        )
        return decodeSecrets(cipher.decrypt(payload).toString(Charsets.UTF_8))
    }

    private fun writeEncrypted(secrets: ProtatoSecrets) {
        val payload = cipher.encrypt(secrets.toJson().toByteArray(Charsets.UTF_8))
        val envelope = JSONObject()
            .put("version", ENCRYPTED_SECRET_VERSION)
            .put("iv", Base64.getEncoder().encodeToString(payload.iv))
            .put("ciphertext", Base64.getEncoder().encodeToString(payload.ciphertext))
            .toString(2)
        encryptedFile.writeAtomically(envelope)
    }
}

private fun File.writeAtomically(content: String) {
    val directory = parentFile ?: error("Secret file must have a parent directory.")
    check(directory.exists() || directory.mkdirs()) { "Unable to create secret directory: $directory" }
    val temporary = File(directory, "$name.tmp")
    try {
        FileOutputStream(temporary).use { output ->
            output.write(content.toByteArray(Charsets.UTF_8))
            output.fd.sync()
        }
        try {
            Files.move(
                temporary.toPath(),
                toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary.toPath(), toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        temporary.delete()
    }
}

private const val ENCRYPTED_SECRET_VERSION = 1
