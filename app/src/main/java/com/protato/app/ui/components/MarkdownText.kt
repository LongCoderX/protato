package com.protato.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    contentColor: Color = LocalContentColor.current,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    val lines = content.lines()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isBlank() -> Spacer(Modifier.height(2.dp))
                line.matches(Regex("-{3,}|\\*{3,}|_{3,}")) -> HorizontalDivider()
                line.startsWith("### ") -> MarkdownLine(
                    text = line.removePrefix("### "),
                    weight = FontWeight.SemiBold,
                    color = accentColor
                )
                line.startsWith("## ") -> MarkdownLine(
                    text = line.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    weight = FontWeight.Bold,
                    color = accentColor
                )
                line.startsWith("# ") -> MarkdownLine(
                    text = line.removePrefix("# "),
                    style = MaterialTheme.typography.titleLarge,
                    weight = FontWeight.Bold,
                    color = accentColor
                )
                line.startsWith(">") -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    MarkdownLine(
                        text = line.removePrefix(">").trim(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = contentColor
                    )
                }
                line.isMarkdownBullet() -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = accentColor, fontWeight = FontWeight.Bold)
                    MarkdownLine(
                        text = line.removeMarkdownBullet(),
                        modifier = Modifier.weight(1f),
                        color = contentColor
                    )
                }
                line.isMarkdownNumberedItem() -> {
                    val number = line.substringBefore(".")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("$number.", color = accentColor, fontWeight = FontWeight.Bold)
                        MarkdownLine(
                            text = line.substringAfter(".").trim(),
                            modifier = Modifier.weight(1f),
                            color = contentColor
                        )
                    }
                }
                else -> MarkdownLine(text = line, color = contentColor)
            }
        }
    }
}

@Composable
private fun MarkdownLine(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    weight: FontWeight? = null,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = markdownInlineText(text),
        modifier = modifier,
        style = style,
        fontWeight = weight,
        color = color
    )
}

@Composable
private fun markdownInlineText(text: String): AnnotatedString {
    val strong = SpanStyle(fontWeight = FontWeight.Bold)
    val emphasis = SpanStyle(fontStyle = FontStyle.Italic)
    val code = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        background = MaterialTheme.colorScheme.surfaceVariant
    )
    val deleted = SpanStyle(textDecoration = TextDecoration.LineThrough)
    return buildAnnotatedString {
        appendMarkdownSegments(
            text = text,
            markers = listOf(
                MarkdownMarker("**", strong),
                MarkdownMarker("__", strong),
                MarkdownMarker("~~", deleted),
                MarkdownMarker("`", code),
                MarkdownMarker("*", emphasis),
                MarkdownMarker("_", emphasis)
            )
        )
    }
}

private data class MarkdownMarker(
    val token: String,
    val style: SpanStyle
)

private fun AnnotatedString.Builder.appendMarkdownSegments(
    text: String,
    markers: List<MarkdownMarker>
) {
    var index = 0
    while (index < text.length) {
        val marker = markers.firstOrNull { text.startsWith(it.token, index) }
        if (marker == null) {
            append(text[index].toString())
            index += 1
            continue
        }
        val start = index + marker.token.length
        val end = text.indexOf(marker.token, start)
        if (end <= start) {
            append(marker.token)
            index += marker.token.length
            continue
        }
        withStyle(marker.style) {
            append(text.substring(start, end))
        }
        index = end + marker.token.length
    }
}

private fun String.isMarkdownBullet(): Boolean {
    return startsWith("- ") || startsWith("* ") || startsWith("• ")
}

private fun String.removeMarkdownBullet(): String {
    return drop(2).trim()
}

private fun String.isMarkdownNumberedItem(): Boolean {
    return matches(Regex("\\d+\\.\\s+.+"))
}
