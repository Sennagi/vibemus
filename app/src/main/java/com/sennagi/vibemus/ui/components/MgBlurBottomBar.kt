package com.sennagi.vibemus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import kotlin.math.abs

data class MgBottomBarItem(
    val id: Int,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MgBlurBottomBar(
    items: List<MgBottomBarItem>,
    selectedId: Int,
    selectedPosition: Float? = null,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    barHeight: Dp = 64.dp,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val selectedIndex = remember(items, selectedId) {
        items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    }
    val position = selectedPosition ?: selectedIndex.toFloat()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + navBarPadding)
            .clip(shape)
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val distance = abs(position - index.toFloat()).coerceIn(0f, 1f)
                val fraction = FastOutSlowInEasing.transform(1f - distance)
                val interactionSource = remember(item.id) { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onItemClick(item.id) },
                    contentAlignment = Alignment.Center
                ) {
                    MgBlurBottomBarItem(
                        icon = item.icon,
                        label = item.label,
                        selected = item.id == selectedId,
                        activeFraction = fraction,
                        interactionSource = interactionSource
                    )
                }
            }
        }
    }
}

@Composable
private fun MgBlurBottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeFraction: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "scale"
    )

    val contentColor = lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        MaterialTheme.colorScheme.primary,
        activeFraction
    )
    
    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
