package com.protato.app

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EncryptedSecretStoreTest {
    @Test
    fun encryptedFileDoesNotContainRawSecrets() {
        val fixture = fixture()
        val secrets = testSecrets()

        fixture.store.save(secrets)

        val encryptedJson = fixture.encryptedFile.readText()
        assertFalse(encryptedJson.contains("provider-secret"))
        assertFalse(encryptedJson.contains("webdav-secret"))
    }

    @Test
    fun encryptedSecretsRoundTrip() {
        val fixture = fixture()
        val secrets = testSecrets()
        fixture.store.save(secrets)

        val reloaded = EncryptedSecretStore(
            encryptedFile = fixture.encryptedFile,
            legacyPlaintextFile = fixture.legacyFile,
            cipher = XorSecretCipher()
        ).load()

        assertEquals(secrets, reloaded)
    }

    @Test
    fun legacyPlaintextSecretsMigrateAndDeleteOldFile() {
        val fixture = fixture()
        val secrets = testSecrets()
        FileSecretStore(fixture.legacyFile).save(secrets)

        val loaded = fixture.store.load()

        assertEquals(secrets, loaded)
        assertTrue(fixture.encryptedFile.exists())
        assertFalse(fixture.legacyFile.exists())
    }

    @Test
    fun failedLegacyMigrationKeepsPlaintextData() {
        val fixture = fixture(cipher = FailingEncryptCipher())
        val secrets = testSecrets()
        FileSecretStore(fixture.legacyFile).save(secrets)

        val loaded = fixture.store.load()

        assertEquals(secrets, loaded)
        assertTrue(fixture.legacyFile.exists())
        assertFalse(fixture.encryptedFile.exists())
    }

    @Test
    fun decryptionFailureCannotOverwriteExistingEncryptedSecrets() {
        val fixture = fixture()
        fixture.store.save(testSecrets())
        val originalEncryptedJson = fixture.encryptedFile.readText()
        val failingStore = EncryptedSecretStore(
            encryptedFile = fixture.encryptedFile,
            legacyPlaintextFile = fixture.legacyFile,
            cipher = FailingDecryptCipher()
        )

        assertThrows(SecretDecryptionException::class.java) { failingStore.load() }
        assertThrows(IllegalStateException::class.java) {
            failingStore.save(ProtatoSecrets(webDavAuthToken = "replacement"))
        }
        assertEquals(originalEncryptedJson, fixture.encryptedFile.readText())
    }

    @Test
    fun unverifiedEncryptedFileCannotBeOverwrittenAfterDecryptionFailure() {
        val fixture = fixture()
        fixture.store.save(testSecrets())
        val originalEncryptedJson = fixture.encryptedFile.readText()
        val failingStore = EncryptedSecretStore(
            encryptedFile = fixture.encryptedFile,
            legacyPlaintextFile = fixture.legacyFile,
            cipher = FailingDecryptCipher()
        )

        assertThrows(SecretDecryptionException::class.java) {
            failingStore.save(ProtatoSecrets(webDavAuthToken = "replacement"))
        }
        assertEquals(originalEncryptedJson, fixture.encryptedFile.readText())
    }

    private fun fixture(cipher: SecretCipher = XorSecretCipher()): SecretStoreFixture {
        val directory = Files.createTempDirectory("encrypted-secret-store-test").toFile()
        val encryptedFile = File(directory, ENCRYPTED_SECRET_FILE_NAME)
        val legacyFile = File(directory, SECRET_FILE_NAME)
        return SecretStoreFixture(
            encryptedFile = encryptedFile,
            legacyFile = legacyFile,
            store = EncryptedSecretStore(encryptedFile, legacyFile, cipher)
        )
    }

    private fun testSecrets(): ProtatoSecrets {
        return ProtatoSecrets(
            llmProviderApiKeys = mapOf("provider-1" to "provider-secret"),
            webDavAuthToken = "webdav-secret"
        )
    }
}

private data class SecretStoreFixture(
    val encryptedFile: File,
    val legacyFile: File,
    val store: EncryptedSecretStore
)

private class XorSecretCipher : SecretCipher {
    override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload {
        return EncryptedSecretPayload(byteArrayOf(1, 2, 3), plaintext.xor())
    }

    override fun decrypt(payload: EncryptedSecretPayload): ByteArray = payload.ciphertext.xor()

    private fun ByteArray.xor(): ByteArray = map { byte ->
        (byte.toInt() xor 0x5A).toByte()
    }.toByteArray()
}

private class FailingEncryptCipher : SecretCipher {
    override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload = error("keystore unavailable")

    override fun decrypt(payload: EncryptedSecretPayload): ByteArray = error("not used")
}

private class FailingDecryptCipher : SecretCipher {
    override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload = error("must remain blocked")

    override fun decrypt(payload: EncryptedSecretPayload): ByteArray = error("invalid key")
}
