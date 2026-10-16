package com.dhamma.palitranslator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SaffronPrimary = Color(0xFFC06000)
val SaffronOnPrimary = Color(0xFFFFFFFF)
val SaffronPrimaryContainer = Color(0xFFFFDCC1)
val SaffronOnPrimaryContainer = Color(0xFF3E1C00)

val DhammaBackgroundLight = Color(0xFFFFF8F4)
val DhammaSurfaceLight = Color(0xFFFFF2E9)
val DhammaOnSurfaceLight = Color(0xFF221A14)

val SaffronPrimaryDark = Color(0xFFFFB77D)
val DhammaBackgroundDark = Color(0xFF1B140F)
val DhammaSurfaceDark = Color(0xFF2B2019)
val DhammaOnSurfaceDark = Color(0xFFEDE0D8)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = SaffronOnPrimary,
    primaryContainer = SaffronPrimaryContainer,
    onPrimaryContainer = SaffronOnPrimaryContainer,
    background = DhammaBackgroundLight,
    surface = DhammaSurfaceLight,
    onSurface = DhammaOnSurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimaryDark,
    onPrimary = Color(0xFF4F2500),
    primaryContainer = Color(0xFF703700),
    onPrimaryContainer = Color(0xFFFFDCC1),
    background = DhammaBackgroundDark,
    surface = DhammaSurfaceDark,
    onSurface = DhammaOnSurfaceDark
)

@Composable
fun PaliTranslatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
