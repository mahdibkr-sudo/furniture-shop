package ir.furnitureshop.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Brown40,
    onPrimary = Color.White,
    primaryContainer = Brown90,
    onPrimaryContainer = Brown900,
    secondary = Brown60,
    onSecondary = Color.White,
    secondaryContainer = Brown90,
    onSecondaryContainer = Brown900,
    tertiary = GoldTertiary,
    onTertiary = Color.White,
    background = WarmCream,
    onBackground = WarmOnSurface,
    surface = WarmCream,
    onSurface = WarmOnSurface,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmOnSurfaceVariant,
    outline = WarmOutline
)

private val DarkColors = darkColorScheme(
    primary = Brown90,
    onPrimary = Brown900,
    primaryContainer = Brown40,
    onPrimaryContainer = Brown100,
    secondary = Brown80,
    onSecondary = Brown900,
    secondaryContainer = Brown40,
    onSecondaryContainer = Brown100,
    tertiary = DarkTertiary,
    onTertiary = Brown900,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun FurnitureShopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
