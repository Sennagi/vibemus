package com.sennagi.vibemus.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sennagi.senui.components.BounceIconButton as SenUIBounceIconButton

/**
 * 弹跳图标按钮
 * 使用 SenUI 库的 BounceIconButton
 *
 * @param onClick 点击回调
 * @param icon 图标
 * @param modifier 修饰符
 * @param iconSize 图标尺寸，默认 24.dp
 * @param buttonSize 按钮尺寸，默认 48.dp
 * @param iconTint 图标颜色
 * @param backgroundColor 背景颜色，默认透明
 * @param enabled 是否可用
 * @param bounceEnabled 是否启用弹跳效果，默认 true
 */
@Composable
fun BounceIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    buttonSize: Dp = 48.dp,
    iconTint: Color = androidx.compose.material3.LocalContentColor.current,
    backgroundColor: Color = Color.Transparent,
    enabled: Boolean = true,
    bounceEnabled: Boolean = true
) {
    SenUIBounceIconButton(
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        iconSize = iconSize,
        buttonSize = buttonSize,
        iconTint = iconTint,
        backgroundColor = backgroundColor,
        enabled = enabled,
        bounceEnabled = bounceEnabled
    )
}

/**
 * 兼容旧调用方式：允许传入自定义内容，而不是固定 ImageVector。
 */
@Composable
fun BounceIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 48.dp,
    backgroundColor: Color = Color.Transparent,
    enabled: Boolean = true,
    bounceEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed, bounceEnabled) {
        if (bounceEnabled) {
            scale.animateTo(
                targetValue = if (isPressed) 0.9f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = Modifier
            .size(buttonSize)
            .then(modifier)
            .clip(CircleShape)
            .background(backgroundColor)
            .scale(scale.value)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
