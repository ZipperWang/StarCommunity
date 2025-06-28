package com.release.startcommunity.ui.theme


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
    primary = Color(0xFF90CAF9),        // 浅蓝
    secondary = Color(0xFF64B5F6),      // 中蓝
    tertiary = Color(0xFF42A5F5)        // 主蓝色（深）
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),        // 主蓝
    secondary = Color(0xFF42A5F5),      // 次蓝
    tertiary = Color(0xFF90CAF9),       // 辅助蓝

    background = Color(0xFFF4F6FC),     // 浅灰蓝背景
    surface = Color(0xFFE3F2FD),        // 卡片背景
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000)
)

@Composable
fun StartCommunityTheme(
    dynamicColor: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
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