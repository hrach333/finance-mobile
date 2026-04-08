package com.hrach.financeapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = PurplePrimary,
    secondary = BlueAccent,
    surface = CardBackground,
    background = SoftBackground
)

@Composable
fun FinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = AppTypography, content = content)
}
