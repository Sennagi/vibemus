package com.sennagi.vibemus.ui.theme

import androidx.compose.runtime.Composable
import com.sennagi.senui.theme.SenUITheme

/**
 * VibeMus 主题
 * 基于 SenUI 主题系统
 */
@Composable
fun VibeMusTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SenUITheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
