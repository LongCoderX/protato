package com.protato.app.ai

import java.nio.file.Files
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiDiagnosticsTest {
    @Test
    fun localLogRedactsApiKeysAndCanBeCleared() {
        val directory = Files.createTempDirectory("ai-diagnostics-test").toFile()
        AiDiagnostics.initialize(directory)

        AiDiagnostics.record("CHAT failure Authorization: Bearer secret-token sk-test-secret-value")

        val recorded = AiDiagnostics.readText()
        assertFalse(recorded.contains("secret-token"))
        assertFalse(recorded.contains("sk-test-secret-value"))
        assertTrue(recorded.contains("<redacted>"))

        AiDiagnostics.clear()
        assertTrue(AiDiagnostics.readText().contains("暂无"))
    }
}
