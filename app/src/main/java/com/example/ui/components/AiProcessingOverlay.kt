package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PipelineStage
import com.example.model.StageStatus
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioEmeraldGreen
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * A futuristic, glowing circular progress indicator component that overlays
 * directly on top of the image area while AI inference or server enhancement is running.
 */
@Composable
fun AiProcessingOverlay(
    isProcessing: Boolean,
    stages: List<PipelineStage>,
    modifier: Modifier = Modifier,
    customMessage: String? = null
) {
    AnimatedVisibility(
        visible = isProcessing,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(300)),
        modifier = modifier
    ) {
        val activeStageIndex = stages.indexOfFirst { it.status == StageStatus.PROCESSING }.let {
            if (it == -1) stages.indexOfLast { s -> s.status == StageStatus.COMPLETED }.coerceAtLeast(0) else it
        }
        val activeStage = stages.getOrNull(activeStageIndex)
        val completedCount = stages.count { it.status == StageStatus.COMPLETED }
        val targetProgress = if (stages.isNotEmpty()) {
            (completedCount.toFloat() + 0.35f).coerceAtMost(stages.size.toFloat()) / stages.size.toFloat()
        } else {
            0.5f
        }

        val infiniteTransition = rememberInfiniteTransition(label = "ai_progress_anim")

        // Continuous rotation for outer glow ring
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin"
        )

        // Pulsing breathing scale for outer halo
        val haloScale by infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        // Laser scanline sweeping vertically across the image area
        val scanlineY by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scanline"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF070B14).copy(alpha = 0.85f))
                .border(1.5.dp, StudioElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .testTag("ai_processing_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Background Laser Scanline Sweep
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * scanlineY
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            StudioElectricCyan.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.8f),
                            StudioElectricCyan.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Central Card Container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F1626).copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263552)),
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Futuristic Glowing Circular Progress Indicator
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Pulsing Halo Ring
                        Box(
                            modifier = Modifier
                                .size(106.dp)
                                .scale(haloScale)
                                .clip(CircleShape)
                                .background(StudioElectricCyan.copy(alpha = 0.08f))
                                .border(1.dp, StudioElectricCyan.copy(alpha = 0.25f), CircleShape)
                        )

                        // Canvas: Multi-layer circular arcs
                        Canvas(modifier = Modifier.size(90.dp)) {
                            val strokeWidthPx = 7.dp.toPx()
                            val arcSize = size.width - strokeWidthPx
                            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

                            // 1. Background Inactive Track
                            drawArc(
                                color = Color(0xFF1E283D),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )

                            // 2. Glowing Neon Electric Gradient Progress Arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        StudioElectricCyan,
                                        StudioNeonPink,
                                        StudioElectricCyan
                                    )
                                ),
                                startAngle = rotationAngle,
                                sweepAngle = (targetProgress * 360f).coerceIn(40f, 350f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                            )
                        }

                        // Inner Center Badge (Percentage or AI Icon)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = StudioElectricCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${(targetProgress * 100).toInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Pipeline Stage Details
                    val titleText = customMessage
                        ?: activeStage?.let { "Stage ${activeStageIndex + 1}/${stages.size}: ${it.name}" }
                        ?: "Running AI Restoration Pipeline"

                    Text(
                        text = titleText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    activeStage?.let { stage ->
                        Text(
                            text = stage.modelName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioElectricCyan,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stage.description,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // GPU Infrastructure Live Status Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF141D30),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23304B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = StudioAmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AWS A10G GPU Inference • Sub-2s Latency",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioAmberGold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
