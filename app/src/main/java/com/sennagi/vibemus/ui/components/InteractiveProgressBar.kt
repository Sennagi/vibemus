package com.sennagi.vibemus.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.unit.dp

@Composable
fun InteractiveProgressBar(
    progress: Float,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var pendingSeekProgress by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(progress, isDragging, pendingSeekProgress) {
        if (isDragging) return@LaunchedEffect

        val pending = pendingSeekProgress
        if (pending != null) {
            val diff = kotlin.math.abs(progress - pending)
            if (diff <= 0.02f) {
                pendingSeekProgress = null
                dragProgress = progress
            } else {
                dragProgress = pending
            }
        } else {
            dragProgress = progress
        }
    }

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 6.dp,
        label = "trackHeight"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(duration) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        isDragging = true
                        pendingSeekProgress = null
                    },
                    onDragEnd = {
                        isDragging = false
                        pendingSeekProgress = dragProgress
                        onSeek((dragProgress * duration).toLong())
                    },
                    onDragCancel = {
                        isDragging = false
                        pendingSeekProgress = null
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        val width = size.width
                        if (width > 0) {
                            val delta = dragAmount / width
                            dragProgress = (dragProgress + delta).coerceIn(0f, 1f)
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
        ) {
            val strokeWidth = size.height
            val centerY = strokeWidth / 2f
            val start = Offset(0f, centerY)
            val end = Offset(size.width, centerY)
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            val fraction = dragProgress.coerceIn(0f, 1f)
            if (fraction > 0f) {
                drawLine(
                    color = Color.White,
                    start = start,
                    end = Offset(size.width * fraction, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
