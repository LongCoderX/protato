package com.protato.app.ai

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object AiDiagnostics {
    private const val LOG_FILE_NAME = "ai-diagnostics.log"
    private const val MAX_LOG_BYTES = 96 * 1024L
    private var logFile: File? = null
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Synchronized
    fun initialize(filesDir: File) {
        logFile = File(filesDir, LOG_FILE_NAME)
    }

    @Synchronized
    fun record(message: String) {
        val target = logFile ?: return
        runCatching {
            target.parentFile?.mkdirs()
            if (target.exists() && target.length() > MAX_LOG_BYTES) {
                val retained = target.readLines().takeLast(200).joinToString("\n")
                target.writeText(if (retained.isBlank()) "" else "$retained\n")
            }
            target.appendText("${timestampFormat.format(Date())} ${sanitize(message)}\n")
        }
    }

    @Synchronized
    fun readText(): String {
        val target = logFile ?: return "诊断日志尚未初始化。"
        return runCatching {
            if (!target.exists() || target.length() == 0L) {
                "暂无 AI 请求日志。请先测试 Provider 或发送一条消息。"
            } else {
                target.readLines().takeLast(200).joinToString("\n")
            }
        }.getOrElse { "读取诊断日志失败：${it.message.orEmpty()}" }
    }

    @Synchronized
    fun clear() {
        logFile?.let { runCatching { it.writeText("") } }
    }

    private fun sanitize(message: String): String {
        return message
            .replace(Regex("Bearer\\s+\\S+", RegexOption.IGNORE_CASE), "Bearer <redacted>")
            .replace(Regex("sk-[A-Za-z0-9_-]{8,}"), "sk-<redacted>")
            .take(1200)
    }
}
