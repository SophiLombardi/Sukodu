package com.example.myaplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF3949AB)
val IndigoLight = Color(0xFFF9FA8DA)
val Teal = Color(0xFF00897B)
val ErrorRed = Color(0xFFFD32F2F)

private val LightColors = lightColorScheme(
    primary = Indigo,
    secondary = Teal,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    secondary = Color(0xFF80CBC4),
    error = Color(0xFFFEF9A9A)
)

@Composable
fun SudokuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}