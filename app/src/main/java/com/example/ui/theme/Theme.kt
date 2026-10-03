package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimarySoftBlue,
    onPrimary = PetrolBlueDark,
    secondary = SecondarySoftGreen,
    onSecondary = PetrolBlueDark,
    tertiary = TertiarySoftLavender,
    onTertiary = PetrolBlueDark,
    background = PetrolBlueDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = PetrolBlueDark,
    onSurfaceVariant = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = PetrolBlueLight,
    onPrimary = WarmWhite,
    secondary = SageGreenLight,
    onSecondary = WarmWhite,
    tertiary = LavenderAccentLight,
    onTertiary = WarmWhite,
    background = WarmWhite,
    onBackground = TextDark,
    surface = SurfaceCream,
    onSurface = TextDark,
    surfaceVariant = SurfaceCream,
    onSurfaceVariant = TextDark
)

@Composable
fun SendaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // We use our warm brand colors by default to preserve the sage-petrol identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
