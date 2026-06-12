package com.protato.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupRulesTest {
    @Test
    fun backupRulesExcludeLocalStateFile() {
        val backupRules = File("src/main/res/xml/backup_rules.xml").readText()
        val dataExtractionRules = File("src/main/res/xml/data_extraction_rules.xml").readText()

        assertTrue(backupRules.contains("""path="$STATE_FILE_NAME""""))
        assertTrue(backupRules.contains("""path="$SECRET_FILE_NAME""""))
        assertTrue(dataExtractionRules.contains("""path="$STATE_FILE_NAME""""))
        assertTrue(dataExtractionRules.contains("""path="$SECRET_FILE_NAME""""))
    }

    @Test
    fun manifestReferencesBackupRules() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("""android:fullBackupContent="@xml/backup_rules""""))
        assertTrue(manifest.contains("""android:dataExtractionRules="@xml/data_extraction_rules""""))
    }
}
