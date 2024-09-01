package com.example.projekat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.unit.dp

private val DarkColorPalette = darkColors(
    primary = teal500,
    primaryVariant = teal700,
    secondary = orange500,
    background = darkGray,
    surface = black,
    onPrimary = white,
    onSecondary = white,
    onBackground = white,
    onSurface = white
)
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

private val LightColorPalette = lightColors(
    primary = teal500,
    primaryVariant = teal700,
    secondary = orange500,
    background = white,
    surface = lightGray,
    onPrimary = black,
    onSecondary = black,
    onBackground = black,
    onSurface = black
)


@Composable
fun ProjekatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = CustomTypography,
        shapes = Shapes,
        content = content
    )
}
