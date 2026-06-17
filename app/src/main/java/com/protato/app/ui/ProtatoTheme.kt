package com.protato.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protato.app.ProtatoThemeChoice

@Composable
fun ProtatoTheme(
    fontScale: Float,
    themeChoice: ProtatoThemeChoice = ProtatoThemeChoice.Mint,
    content: @Composable () -> Unit
) {
    val colors = protatoColorScheme(themeChoice)
    val typography = Typography(
        displayMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            lineHeight = 54.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 29.sp,
            lineHeight = 36.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
    val shapes = Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        small = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    )
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale)
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

private fun protatoColorScheme(themeChoice: ProtatoThemeChoice) = when (themeChoice) {
    ProtatoThemeChoice.Mint -> lightColorScheme(
        primary = Color(0xFF0D9488),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFCCFBF1),
        onPrimaryContainer = Color(0xFF134E4A),
        secondary = Color(0xFFF97316),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFEDD5),
        onSecondaryContainer = Color(0xFF7C2D12),
        tertiary = Color(0xFF334155),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE2E8F0),
        onTertiaryContainer = Color(0xFF0F172A),
        background = Color(0xFFF7FBF8),
        onBackground = Color(0xFF10201E),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF10201E),
        surfaceVariant = Color(0xFFE6F0EC),
        onSurfaceVariant = Color(0xFF53615E),
        outline = Color(0xFFB8C8C1),
        outlineVariant = Color(0xFFD8E4DF),
        error = Color(0xFFB42318),
        errorContainer = Color(0xFFFEE4E2),
        onErrorContainer = Color(0xFF7A271A)
    )

    ProtatoThemeChoice.SeaSalt -> lightColorScheme(
        primary = Color(0xFF2563EB),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDBEAFE),
        onPrimaryContainer = Color(0xFF1E3A8A),
        secondary = Color(0xFF0F766E),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCCFBF1),
        onSecondaryContainer = Color(0xFF134E4A),
        tertiary = Color(0xFF475569),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE2E8F0),
        onTertiaryContainer = Color(0xFF0F172A),
        background = Color(0xFFF6F8FB),
        onBackground = Color(0xFF101828),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF101828),
        surfaceVariant = Color(0xFFE8EDF5),
        onSurfaceVariant = Color(0xFF4B5565),
        outline = Color(0xFFB7C2D2),
        outlineVariant = Color(0xFFD7DEE9),
        error = Color(0xFFB42318),
        errorContainer = Color(0xFFFEE4E2),
        onErrorContainer = Color(0xFF7A271A)
    )

    ProtatoThemeChoice.Berry -> lightColorScheme(
        primary = Color(0xFFBE123C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDDE5),
        onPrimaryContainer = Color(0xFF881337),
        secondary = Color(0xFFB45309),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF78350F),
        tertiary = Color(0xFF365314),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFD9F99D),
        onTertiaryContainer = Color(0xFF1A2E05),
        background = Color(0xFFFFF7F8),
        onBackground = Color(0xFF231316),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF231316),
        surfaceVariant = Color(0xFFF3E2E5),
        onSurfaceVariant = Color(0xFF67565A),
        outline = Color(0xFFD0B7BD),
        outlineVariant = Color(0xFFE9D5DA),
        error = Color(0xFFB42318),
        errorContainer = Color(0xFFFEE4E2),
        onErrorContainer = Color(0xFF7A271A)
    )
}
