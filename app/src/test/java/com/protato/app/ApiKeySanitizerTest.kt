package com.protato.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeySanitizerTest {
    @Test
    fun removesRepeatedLegacyMaskSuffixes() {
        assertEquals("secret-key", sanitizeLegacyApiKey(" secret-key****** "))
    }

    @Test
    fun detectsMaskCharactersAnywhereInKey() {
        assertTrue("secret***key".containsApiKeyMask())
        assertFalse("secret-key".containsApiKeyMask())
    }
}
