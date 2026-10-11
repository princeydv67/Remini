package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SplitOrientation {
    VERTICAL, HORIZONTAL
}

/**
 * High-performance, gesture-driven Split-Screen Comparison Slider.
 *
 * Features:
 * - Fluid horizontal or vertical swipe with immediate 1:1 finger tracking.
 * - Tap-to-glide: Tapping anywhere glides the divider with spring physics.
 * - Double-tap to re-center to 50%.
 * - Hold-to-compare: Long-press reveals full original image.
 * - Animated auto-sweep demo toggle.
 * - Vertical / Horizontal orientation switcher.
 * - Live percentage indicator floating during drag.
 * - Haptic feedback when crossing the 50% threshold.
 */
@Composable
fun BeforeAfterSlider(
    beforeBitmap: Bitmap?,
    afterBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    initialSplit: Float = 0.5f,
    beforeLabel: String = "BEFORE",
    afterLabel: String = "ENHANCED 4K",
    orientation: SplitOrientation = SplitOrientation.VERTICAL,
    showControls: Boolean = true,
    autoSweep: Boolean = false,
    onSplitChanged: ((Float) -> Unit)? = null
) {
    var splitFraction by remember { mutableFloatStateOf(initialSplit) }
    var isDragging by remember { mutableStateOf(false) }
    var isHoldingOriginal by remember { mutableStateOf(false) }
    var currentOrientation by remember { mutableStateOf(orientation) }
    var isAutoSweeping by remember { mutableStateOf(autoSweep) }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val sweepAnim = remember { Animatable(initialSplit) }

    // Spring animation for smooth programmatic repositioning
    val animatedSplit = remember { Animatable(initialSplit) }

    // Auto sweep loop
    LaunchedEffect(isAutoSweeping) {
        if (isAutoSweeping) {
            while (isAutoSweeping) {
                sweepAnim.animateTo(
                    targetValue = 0.88f,
                    animationSpec = tween(2400, easing = FastOutSlowInEasing)
                ) {
                    if (!isDragging && isAutoSweeping) {
                        splitFraction = value
                        onSplitChanged?.invoke(value)
                    }
                }
                sweepAnim.animateTo(
                    targetValue = 0.12f,
                    animationSpec = tween(2400, easing = FastOutSlowInEasing)
                ) {
                    if (!isDragging && isAutoSweeping) {
                        splitFraction = value
                        onSplitChanged?.invoke(value)
                    }
                }
            }
        }
    }

    // Interactive grab handle scale spring on drag
    val handleScale by animateFloatAsState(
        targetValue = if (isDragging) 1.22f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "handleScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF070A11))
            .border(1.dp, Color(0xFF26334D), RoundedCornerShape(22.dp))
            .testTag("before_after_slider")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Effective fraction: If user is holding to preview original, show 100% before
        val effectiveFraction = if (isHoldingOriginal) 1.0f else splitFraction.coerceIn(0.01f, 0.99f)
        val splitCoordPx = if (currentOrientation == SplitOrientation.VERTICAL) {
            widthPx * effectiveFraction
        } else {
            heightPx * effectiveFraction
        }

        // Dual-Layer Canvas with Clip Rects
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(currentOrientation) {
                    detectTapGestures(
                        onDoubleTap = {
                            isAutoSweeping = false
                            scope.launch {
                                animatedSplit.snapTo(splitFraction)
                                animatedSplit.animateTo(
                                    targetValue = 0.5f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                ) {
                                    splitFraction = value
                                    onSplitChanged?.invoke(value)
                                }
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onTap = { offset ->
                            isAutoSweeping = false
                            val targetFraction = if (currentOrientation == SplitOrientation.VERTICAL) {
                                (offset.x / widthPx).coerceIn(0.02f, 0.98f)
                            } else {
                                (offset.y / heightPx).coerceIn(0.02f, 0.98f)
                            }
                            scope.launch {
                                animatedSplit.snapTo(splitFraction)
                                animatedSplit.animateTo(
                                    targetValue = targetFraction,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                ) {
                                    splitFraction = value
                                    onSplitChanged?.invoke(value)
                                }
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onLongPress = {
                            isHoldingOriginal = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
                .pointerInput(currentOrientation) {
                    detectDragGestures(
                        onDragStart = {
                            isAutoSweeping = false
                            isDragging = true
                            isHoldingOriginal = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDragEnd = {
                            isDragging = false
                            isHoldingOriginal = false
                        },
                        onDragCancel = {
                            isDragging = false
                            isHoldingOriginal = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            isAutoSweeping = false
                            val deltaFraction = if (currentOrientation == SplitOrientation.VERTICAL) {
                                if (widthPx > 0) dragAmount.x / widthPx else 0f
                            } else {
                                if (heightPx > 0) dragAmount.y / heightPx else 0f
                            }
                            val prevFraction = splitFraction
                            val newFraction = (splitFraction + deltaFraction).coerceIn(0.01f, 0.99f)
                            splitFraction = newFraction
                            onSplitChanged?.invoke(newFraction)

                            // Haptic tick when passing the exact 50% midpoint
                            if ((prevFraction < 0.5f && newFraction >= 0.5f) || (prevFraction > 0.5f && newFraction <= 0.5f)) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val targetSize = androidx.compose.ui.unit.IntSize(canvasW.toInt(), canvasH.toInt())

            // 1. Draw Layer 1 (Full Enhanced / After Bitmap in background)
            afterBitmap?.let { bitmap ->
                val img = bitmap.asImageBitmap()
                drawImage(image = img, dstSize = targetSize)
            }

            // 2. Draw Layer 2 (Before Bitmap clipped to split boundary)
            beforeBitmap?.let { bitmap ->
                val img = bitmap.asImageBitmap()
                if (currentOrientation == SplitOrientation.VERTICAL) {
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = splitCoordPx,
                        bottom = canvasH,
                        clipOp = ClipOp.Intersect
                    ) {
                        drawImage(image = img, dstSize = targetSize)
                    }
                } else {
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = canvasW,
                        bottom = splitCoordPx,
                        clipOp = ClipOp.Intersect
                    ) {
                        drawImage(image = img, dstSize = targetSize)
                    }
                }
            }

            // 3. Draw High-Precision Divider Line with Multi-layered Glow
            if (!isHoldingOriginal) {
                if (currentOrientation == SplitOrientation.VERTICAL) {
                    // Outer diffuse glow
                    drawLine(
                        color = StudioElectricCyan.copy(alpha = 0.35f),
                        start = Offset(splitCoordPx, 0f),
                        end = Offset(splitCoordPx, canvasH),
                        strokeWidth = 8.dp.toPx()
                    )
                    // Inner electric neon glow
                    drawLine(
                        color = StudioElectricCyan,
                        start = Offset(splitCoordPx, 0f),
                        end = Offset(splitCoordPx, canvasH),
                        strokeWidth = 3.dp.toPx()
                    )
                    // Core white beam
                    drawLine(
                        color = Color.White,
                        start = Offset(splitCoordPx, 0f),
                        end = Offset(splitCoordPx, canvasH),
                        strokeWidth = 1.5.dp.toPx()
                    )
                } else {
                    // Horizontal split lines
                    drawLine(
                        color = StudioElectricCyan.copy(alpha = 0.35f),
                        start = Offset(0f, splitCoordPx),
                        end = Offset(canvasW, splitCoordPx),
                        strokeWidth = 8.dp.toPx()
                    )
                    drawLine(
                        color = StudioElectricCyan,
                        start = Offset(0f, splitCoordPx),
                        end = Offset(canvasW, splitCoordPx),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, splitCoordPx),
                        end = Offset(canvasW, splitCoordPx),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }

        // Draggable Central Thumb Handle & Live Percentage Display
        val handleSize = 46.dp
        if (!isHoldingOriginal) {
            Box(
                modifier = Modifier
                    .offset {
                        val handleSizePx = handleSize.toPx()
                        if (currentOrientation == SplitOrientation.VERTICAL) {
                            IntOffset(
                                x = (splitCoordPx - (handleSizePx / 2)).roundToInt(),
                                y = (heightPx / 2 - (handleSizePx / 2)).roundToInt()
                            )
                        } else {
                            IntOffset(
                                x = (widthPx / 2 - (handleSizePx / 2)).roundToInt(),
                                y = (splitCoordPx - (handleSizePx / 2)).roundToInt()
                            )
                        }
                    }
                    .scale(handleScale)
                    .size(handleSize)
                    .shadow(16.dp, CircleShape, spotColor = StudioElectricCyan)
                    .background(Color(0xFF0C1322).copy(alpha = 0.95f), CircleShape)
                    .border(2.dp, StudioElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (currentOrientation == SplitOrientation.VERTICAL) {
                        Icons.Default.CompareArrows
                    } else {
                        Icons.Default.Height
                    },
                    contentDescription = "Comparison Split Handle",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Floating Percentage Indicator when user drags
            AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .offset {
                        val indicatorHalfW = 32.dp.toPx()
                        val indicatorOffsetH = 58.dp.toPx()
                        if (currentOrientation == SplitOrientation.VERTICAL) {
                            IntOffset(
                                x = (splitCoordPx - indicatorHalfW).roundToInt(),
                                y = (heightPx / 2 - indicatorOffsetH).roundToInt()
                            )
                        } else {
                            IntOffset(
                                x = (widthPx / 2 - indicatorHalfW).roundToInt(),
                                y = (splitCoordPx - indicatorOffsetH).roundToInt()
                            )
                        }
                    }
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Black.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioElectricCyan)
                ) {
                    Text(
                        text = "${(splitFraction * 100).toInt()}%",
                        color = StudioElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Header Badges: "BEFORE" on top-left, "ENHANCED 4K" on top-right
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Before Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.70f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Text(
                    text = if (isHoldingOriginal) "SHOWING 100% ORIGINAL" else beforeLabel,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // After Badge with Electric Cyan Accent
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF03262C).copy(alpha = 0.90f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioElectricCyan)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = StudioElectricCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = afterLabel,
                        color = StudioElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Bottom Controls Toolbar
        if (showControls) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334D)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Re-center button (50%)
                    IconButton(
                        onClick = {
                            isAutoSweeping = false
                            scope.launch {
                                animatedSplit.snapTo(splitFraction)
                                animatedSplit.animateTo(
                                    targetValue = 0.5f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                ) {
                                    splitFraction = value
                                    onSplitChanged?.invoke(value)
                                }
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Center 50%",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Auto Sweep Demo Toggle
                    IconButton(
                        onClick = {
                            isAutoSweeping = !isAutoSweeping
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoSweeping) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Auto Sweep",
                            tint = if (isAutoSweeping) StudioAmberGold else StudioElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Orientation Flip (Vertical vs Horizontal split)
                    IconButton(
                        onClick = {
                            currentOrientation = if (currentOrientation == SplitOrientation.VERTICAL) {
                                SplitOrientation.HORIZONTAL
                            } else {
                                SplitOrientation.VERTICAL
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (currentOrientation == SplitOrientation.VERTICAL) {
                                Icons.Default.Height
                            } else {
                                Icons.Default.CompareArrows
                            },
                            contentDescription = "Switch Orientation",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "◄ Drag or Tap to Compare ►",
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
