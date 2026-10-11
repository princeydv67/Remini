package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PipelineStage
import com.example.model.StageStatus
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioEmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PipelineVisualizer(
    stages: List<PipelineStage>,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    val completedCount = stages.count { it.status == StageStatus.COMPLETED }
    val progress = if (stages.isNotEmpty()) completedCount.toFloat() / stages.size.toFloat() else 0f
    val totalTimeMs = stages.filter { it.status == StageStatus.COMPLETED }.sumOf { it.latencyMs }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F1523),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pipeline_visualizer")
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Header: Pipeline Title and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = StudioElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Inference Pipeline",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF192237)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = StudioAmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (totalTimeMs > 0) "${totalTimeMs}ms" else "Queue Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioAmberGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overall Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StudioElectricCyan,
                trackColor = Color(0xFF1E283D)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Each Stage Card
            stages.forEachIndexed { index, stage ->
                PipelineStageItem(
                    stage = stage,
                    stageNumber = index + 1,
                    isLast = index == stages.size - 1
                )
            }
        }
    }
}

@Composable
fun PipelineStageItem(
    stage: PipelineStage,
    stageNumber: Int,
    isLast: Boolean
) {
    val statusBg by animateColorAsState(
        when (stage.status) {
            StageStatus.COMPLETED -> StudioEmeraldGreen.copy(alpha = 0.15f)
            StageStatus.PROCESSING -> StudioElectricCyan.copy(alpha = 0.18f)
            StageStatus.PENDING -> Color(0xFF141C2D)
        }, label = "stageBg"
    )

    val borderColor by animateColorAsState(
        when (stage.status) {
            StageStatus.COMPLETED -> StudioEmeraldGreen.copy(alpha = 0.45f)
            StageStatus.PROCESSING -> StudioElectricCyan
            StageStatus.PENDING -> Color(0xFF1E293B)
        }, label = "borderColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(statusBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Number or Status Indicator
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when (stage.status) {
                            StageStatus.COMPLETED -> StudioEmeraldGreen
                            StageStatus.PROCESSING -> StudioElectricCyan
                            StageStatus.PENDING -> Color(0xFF26334D)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (stage.status) {
                    StageStatus.COMPLETED -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    StageStatus.PROCESSING -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                    StageStatus.PENDING -> Text(
                        text = "$stageNumber",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Stage Information
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stage.name,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${stage.latencyMs}ms",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (stage.status == StageStatus.COMPLETED) StudioEmeraldGreen else TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stage.modelName,
                    fontSize = 11.sp,
                    color = StudioElectricCyan,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stage.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Tensor dimensions badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Tensor: ${stage.tensorInput} → ${stage.tensorOutput}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF7DD3FC)
                    )
                }
            }
        }
    }
}
