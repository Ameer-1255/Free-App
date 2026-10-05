package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = Color(0xFF00363D),
  primaryContainer = Color(0xFF004F58),
  onPrimaryContainer = Color(0xFF97F0FF),
  secondary = NeonPurple,
  onSecondary = Color(0xFF381E72),
  secondaryContainer = Color(0xFF4F378B),
  onSecondaryContainer = Color(0xFFEADDFF),
  tertiary = PlatformMovieBox,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkOutline,
  error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF006876),
  onPrimary = Color.White,
  primaryContainer = Color(0xFF97F0FF),
  onPrimaryContainer = Color(0xFF001F24),
  secondary = Color(0xFF6750A4),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFEADDFF),
  onSecondaryContainer = Color(0xFF21005D),
  tertiary = Color(0xFFB45309),
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = Color(0xFFDC2626)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek multimedia dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
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
