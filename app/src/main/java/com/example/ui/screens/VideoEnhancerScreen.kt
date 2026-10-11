package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EnhanceMode
import com.example.processing.ImageEnhancerEngine
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioEmeraldGreen
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun VideoEnhancerScreen() {
    val context = LocalContext.current

    // Video Player & Frame scrubber state
    var currentFrameIndex by remember { mutableIntStateOf(18) }
    val totalFrames = 120
    var isPlaying by remember { mutableStateOf(false) }

    // Models & Toggles
    var enable60FpsInterpolation by remember { mutableStateOf(true) }
    var enableTemporalDenoise by remember { mutableStateOf(true) }
    var temporalConsistencyAlpha by remember { mutableFloatStateOf(0.92f) }

    // Bitmaps
    var sampleBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var beforeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var afterBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val bmp = ImageEnhancerEngine.loadBitmapFromResource(context, R.drawable.img_sample_portrait)
        sampleBitmap = bmp
        val (b, a) = ImageEnhancerEngine.createEnhancedPair(
            source = bmp,
            mode = EnhanceMode.SUPER_RES,
            sharpness = 0.85f
        )
        beforeBitmap = b
        afterBitmap = a
    }

    // Playback loop simulation
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            currentFrameIndex = (currentFrameIndex % totalFrames) + 1
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .padding(bottom = 90.dp)
            .testTag("video_enhancer_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = StudioNeonPink,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Video Upscaler",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Temporal Recurrent Super-Resolution (RealBasicVSR)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioNeonPink.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioNeonPink)
            ) {
                Text(
                    text = "60 FPS 4K",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioNeonPink,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Video Frame Comparison View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
        ) {
            BeforeAfterSlider(
                beforeBitmap = beforeBitmap,
                afterBitmap = afterBitmap,
                modifier = Modifier.fillMaxSize(),
                beforeLabel = "720p 24fps (Noisy)",
                afterLabel = "4K 60fps (RealBasicVSR)"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Video Timeline Scrubber & Player Controls
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF101726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Time & Frame indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frame $currentFrameIndex / $totalFrames",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioElectricCyan
                    )

                    val seconds = currentFrameIndex / 24
                    val millis = (currentFrameIndex % 24) * 41
                    Text(
                        text = String.format("00:0%d:%02d", seconds, millis),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Slider Scrubber
                Slider(
                    value = currentFrameIndex.toFloat(),
                    onValueChange = {
                        currentFrameIndex = it.toInt()
                    },
                    valueRange = 1f..totalFrames.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioElectricCyan,
                        activeTrackColor = StudioElectricCyan,
                        inactiveTrackColor = Color(0xFF1E283D)
                    )
                )

                // Play / Pause / Step Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentFrameIndex = (currentFrameIndex - 5).coerceAtLeast(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Rewind",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(StudioElectricCyan)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            currentFrameIndex = (currentFrameIndex + 5).coerceAtMost(totalFrames)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Video Enhancement Pipeline Strategy
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF101726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Video Neural Stack Configuration",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 1: 60 FPS RIFE Interpolation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "60 FPS Neural Interpolation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "RIFE v4.6 optical flow fills intermediate motion vectors",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = enable60FpsInterpolation,
                        onCheckedChange = { enable60FpsInterpolation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudioElectricCyan,
                            checkedTrackColor = Color(0xFF0D3349)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle 2: Temporal Anti-Flicker Consistency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Temporal Anti-Flicker Denoise",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bidirectional propagation across 15 lookahead frames",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = enableTemporalDenoise,
                        onCheckedChange = { enableTemporalDenoise = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudioElectricCyan,
                            checkedTrackColor = Color(0xFF0D3349)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Slider: Temporal Consistency Weight
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Temporal Smoothing Factor (α)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(temporalConsistencyAlpha * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = temporalConsistencyAlpha,
                        onValueChange = { temporalConsistencyAlpha = it },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioElectricCyan,
                            activeTrackColor = StudioElectricCyan,
                            inactiveTrackColor = Color(0xFF1E283D)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GPU Render & Batching Throughput Stats
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0C1322),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D2940)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Backend Video Inference Metrics",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricPill("Throughput", "28.4 FPS", StudioEmeraldGreen)
                    MetricPill("Inference", "TensorRT FP16", StudioElectricCyan)
                    MetricPill("VRAM", "14.2 GB / 24 GB", StudioAmberGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Dispatched 4K Video Render Job to Cloud GPU Queue!", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("render_video_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioNeonPink
            )
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXPORT 4K 60FPS VIDEO (ASYNC QUEUE)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MetricPill(title: String, value: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF162035)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = TextMuted)
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
