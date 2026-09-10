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

private val CyberDarkColorScheme = darkColorScheme(
  primary = CyberPrimary,
  onPrimary = CyberBackground,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = CyberPrimary,
  secondary = CyberAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF1E284A),
  onSecondaryContainer = Color(0xFFB5C9FF),
  tertiary = CyberNeonTeal,
  onTertiary = CyberBackground,
  background = CyberBackground,
  onBackground = CyberText,
  surface = CyberSurface,
  onSurface = CyberText,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = CyberTextSecondary,
  outline = CyberBorder,
  outlineVariant = Color(0xFF0C1322)
)

private val CyberLightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F7FC),
  onPrimaryContainer = Color(0xFF00495C),
  secondary = LightAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE5ECFD),
  onSecondaryContainer = Color(0xFF1E357F),
  tertiary = Color(0xFF009688),
  onTertiary = Color.White,
  background = LightBackground,
  onBackground = LightText,
  surface = LightSurface,
  onSurface = LightText,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun NexiumTheme(
  themeMode: String = "dark", // "dark", "light", "system"
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val darkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> isSystemDark
  }

  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> CyberDarkColorScheme
    else -> CyberLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  NexiumTheme(
    themeMode = if (darkTheme) "dark" else "light",
    dynamicColor = dynamicColor,
    content = content
  )
}
