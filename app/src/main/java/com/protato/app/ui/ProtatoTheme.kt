package com.protato.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protato.app.ProtatoThemeChoice
import com.protato.app.R

val ProtatoGreen = Color(0xFF3ECF8E)
val ProtatoBackground = Color(0xFF171717)
val ProtatoSurface = Color(0xFF1C1C1C)
val ProtatoBorder = Color(0xFF2E2E2E)

private val ProtatoFontFamily = FontFamily(
    Font(R.font.noto_sans_cjk_regular, FontWeight.Normal),
    Font(R.font.noto_sans_cjk_regular, FontWeight.Medium),
    Font(R.font.noto_sans_cjk_regular, FontWeight.SemiBold),
    Font(R.font.noto_sans_cjk_regular, FontWeight.Bold)
)

@Composable
fun ProtatoTheme(
    fontScale: Float,
    themeChoice: ProtatoThemeChoice = ProtatoThemeChoice.Mint,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale.coerceIn(0.85f, 1.35f))
    ) {
        MaterialTheme(
            colorScheme = protatoColorScheme(themeChoice),
            typography = protatoTypography,
            shapes = protatoShapes,
            content = content
        )
    }
}

private val protatoTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 52.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ProtatoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

private val protatoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp)
)

private fun protatoColorScheme(themeChoice: ProtatoThemeChoice) = darkColorScheme(
    primary = themeChoice.accentColor(),
    onPrimary = Color(0xFF0F0F0F),
    primaryContainer = themeChoice.accentColor().copy(alpha = 0.16f),
    onPrimaryContainer = themeChoice.accentColor(),
    secondary = Color(0xFFEAB308),
    onSecondary = Color(0xFF0F0F0F),
    secondaryContainer = Color(0xFF332E18),
    onSecondaryContainer = Color(0xFFF1D35B),
    tertiary = Color(0xFFB4B4B4),
    onTertiary = Color(0xFF171717),
    tertiaryContainer = Color(0xFF292929),
    onTertiaryContainer = Color(0xFFDADADA),
    background = ProtatoBackground,
    onBackground = Color(0xFFFAFAFA),
    surface = ProtatoSurface,
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFB4B4B4),
    surfaceContainerLowest = Color(0xFF141414),
    surfaceContainerLow = ProtatoBackground,
    surfaceContainer = ProtatoSurface,
    surfaceContainerHigh = Color(0xFF242424),
    surfaceContainerHighest = Color(0xFF2A2A2A),
    outline = ProtatoBorder,
    outlineVariant = Color(0xFF242424),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF240000),
    errorContainer = Color(0xFF3A1C1C),
    onErrorContainer = Color(0xFFFFB4B4)
)

internal fun ProtatoThemeChoice.accentColor(): Color = when (this) {
    ProtatoThemeChoice.Mint -> ProtatoGreen
    ProtatoThemeChoice.SeaSalt -> Color(0xFF5EC7F2)
    ProtatoThemeChoice.Berry -> Color(0xFFF36B8A)
    ProtatoThemeChoice.Paper -> Color(0xFFD8CFAE)
}
