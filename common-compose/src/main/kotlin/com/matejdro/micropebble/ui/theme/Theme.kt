package com.matejdro.micropebble.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
   primary = Color(0xffffb694),
   secondary = Color(0xffe6bead),
   tertiary = Color(0xffd1c88f)
)

private val LightColorScheme = lightColorScheme(
   primary = Color(0xff8d4d2d),
   secondary = Color(0xff765749),
   tertiary = Color(0xff665f31)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicroPebbleTheme(
   darkTheme: Boolean = isSystemInDarkTheme(),
   // Dynamic color is available on Android 12+
   dynamicColor: Boolean = true,
   content: @Composable () -> Unit,
) {
   val colorScheme = when {
      dynamicColor -> {
         val context = LocalContext.current
         if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
   }

   MaterialTheme(
      colorScheme = colorScheme,
   ) {
      if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
         // Workaround for https://issuetracker.google.com/issues/274471576 - Ripple effects do not work on Android 13
         val defaultRippleConfig = LocalRippleConfiguration.current ?: RippleConfiguration()
         val transparencyFixRippleConfiguration = RippleConfiguration(
            color = defaultRippleConfig.color,
            rippleAlpha = defaultRippleConfig.rippleAlpha?.run {
               RippleAlpha(
                  draggedAlpha,
                  focusedAlpha,
                  hoveredAlpha,
                  pressedAlpha.coerceAtLeast(MIN_RIPPLE_ALPHA_ON_TIRAMISU)
               )
            }

         )
         CompositionLocalProvider(LocalRippleConfiguration provides transparencyFixRippleConfiguration) {
            content()
         }
      } else {
         content()
      }
   }
}

private const val MIN_RIPPLE_ALPHA_ON_TIRAMISU = 0.5f
