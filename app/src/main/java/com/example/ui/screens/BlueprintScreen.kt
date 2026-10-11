package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CloudProvider
import com.example.model.GpuEconomics
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioEmeraldGreen
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BlueprintScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Economics & ROI", "AI Pipeline", "Tech Stack", "MVP Roadmap")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .padding(bottom = 90.dp)
            .testTag("blueprint_screen")
    ) {
        // Blueprint Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = null,
                        tint = StudioElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PM & AI Architect Blueprint",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "System Architecture, GPU Economics & Product Strategy",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioElectricCyan.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioElectricCyan)
            ) {
                Text(
                    text = "SPEC v2.4",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioElectricCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tab Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF101726),
            contentColor = StudioElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = StudioElectricCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF222E46), RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> EconomicsTabContent()
            1 -> PipelineTabContent()
            2 -> TechStackTabContent()
            3 -> RoadmapTabContent()
        }
    }
}

@Composable
private fun EconomicsTabContent() {
    var mau by remember { mutableFloatStateOf(50_000f) }
    var imagesPerUser by remember { mutableFloatStateOf(14f) }
    var selectedProvider by remember { mutableStateOf(CloudProvider.AWS_SELF_HOSTED_A10G) }
    var conversionRate by remember { mutableFloatStateOf(4.5f) }
    val weeklyPrice = 4.99f

    // Calculations
    val totalImagesPerMonth = mau * imagesPerUser
    val payingSubscribers = mau * (conversionRate / 100f)
    val monthlyRevenue = payingSubscribers * (weeklyPrice * 4.33f)
    val gpuComputeCost = totalImagesPerMonth * selectedProvider.costPerImage
    val grossProfit = monthlyRevenue - gpuComputeCost
    val grossMargin = if (monthlyRevenue > 0) (grossProfit / monthlyRevenue) * 100f else 0f

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Summary Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F1523),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioElectricCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Projected Monthly P&L at Scale",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Gross Revenue", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = currencyFormatter.format(monthlyRevenue.toDouble()),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioEmeraldGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "GPU Infra Cost", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = currencyFormatter.format(gpuComputeCost.toDouble()),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = StudioNeonPink,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Gross Margin", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "${grossMargin.toInt()}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = if (grossMargin > 80) StudioElectricCyan else StudioAmberGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF19253C)
                    ) {
                        Text(
                            text = "${currencyFormatter.format(selectedProvider.costPerImage)} / img",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Interactive Sliders
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF101726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Interactive Economic Drivers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // MAU Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Monthly Active Users (MAU)", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = numberFormatter.format(mau.toLong()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = mau,
                        onValueChange = { mau = it },
                        valueRange = 10_000f..250_000f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioElectricCyan,
                            activeTrackColor = StudioElectricCyan
                        )
                    )
                }

                // Enhancements per user
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Avg Enhancements / User / Month", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${imagesPerUser.toInt()} images",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmberGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = imagesPerUser,
                        onValueChange = { imagesPerUser = it },
                        valueRange = 5f..40f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioAmberGold,
                            activeTrackColor = StudioAmberGold
                        )
                    )
                }

                // Conversion Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Free-to-Paid Subscription Conversion", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${String.format("%.1f", conversionRate)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioEmeraldGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = conversionRate,
                        onValueChange = { conversionRate = it },
                        valueRange = 1.0f..10.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioEmeraldGreen,
                            activeTrackColor = StudioEmeraldGreen
                        )
                    )
                }
            }
        }

        // Provider Comparison Selector
        Text(
            text = "Inference Infrastructure Strategy",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CloudProvider.values().forEach { provider ->
                val isSel = selectedProvider == provider
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProvider = provider }
                        .border(
                            1.dp,
                            if (isSel) StudioElectricCyan else Color(0xFF222E46),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) Color(0xFF0D253B) else Color(0xFF101726)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = provider.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Avg latency: ${provider.latencySec}s per image",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "$${String.format("%.4f", provider.costPerImage)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSel) StudioElectricCyan else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(text = "per image", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ArchitectureStepCard(
            step = "01",
            title = "Face Detection & 5-Point Affine Alignment",
            model = "RetinaFace / InsightFace (ResNet-50 backbone)",
            description = "Locates facial bounding boxes with 68 3D landmarks. Warps face into canonical 512x512 space so eye centers and mouth corners strictly align to coordinate template.",
            inputTensor = "1080x1920 RGBA",
            outputTensor = "512x512 RGB Aligned Crop",
            latency = "180ms"
        )

        ArchitectureStepCard(
            step = "02",
            title = "Face Detail Hallucination & Codebook Prior",
            model = "CodeFormer (VQGAN Codebook, w=0.75) or GFPGAN v1.4",
            description = "Unlike pure diffusion which hallucinates different faces, CodeFormer maps degraded features to a discrete codebook of real human face priors to faithfully restore pupils, lashes, and teeth.",
            inputTensor = "512x512 degraded",
            outputTensor = "512x512 HQ restored",
            latency = "740ms"
        )

        ArchitectureStepCard(
            step = "03",
            title = "Full-Image Background Super-Resolution",
            model = "Real-ESRGAN_x4plus (RRDBNet 16.7M params)",
            description = "Processes hair, clothing, jewelry, and background scene context. Uses pure convolutional residual blocks optimized with TensorRT FP16.",
            inputTensor = "540x960 low-res",
            outputTensor = "2160x3840 4K UHD",
            latency = "620ms"
        )

        ArchitectureStepCard(
            step = "04",
            title = "Poisson Boundary Masking & Seamless Clone",
            model = "Pyramid Blending + Bilateral Soft Alpha",
            description = "Inverse-transforms restored face back to the original angle and resolution, followed by gradient-domain Poisson blending to avoid visible seam edges around the forehead and jaw.",
            inputTensor = "Restored Face + 4K Upscale",
            outputTensor = "Harmonized 4K Composite",
            latency = "110ms"
        )

        ArchitectureStepCard(
            step = "05",
            title = "Contrast Limited Adaptive Histogram Equalization",
            model = "CLAHE + Neural White Balance",
            description = "Eliminates JPEG blocking artifacts, balances skin warmth, and outputs a 10-bit color-profile master image ready for instant mobile download.",
            inputTensor = "Composite 4K",
            outputTensor = "Final Enhanced Master",
            latency = "90ms"
        )
    }
}

@Composable
private fun TechStackTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TechComponentCard(
            category = "Mobile Client Strategy",
            technology = "Jetpack Compose (Android) + Swift / SwiftUI (iOS)",
            rationale = "CameraX, NPU hardware acceleration (Qualcomm Snapdragon NPU / Apple Neural Engine via CoreML), and Metal/Vulkan shaders require native bridges. Cross-platform Flutter/RN introduces texture bridge overhead on 4K bitstreams."
        )

        TechComponentCard(
            category = "Inference API Microservice",
            technology = "Python FastAPI + NVIDIA Triton Inference Server",
            rationale = "Triton provides dynamic request batching, concurrent model execution across multiple GPUs, and TensorRT FP16 compiled engines, achieving 3.8x lower latency than standard TorchServe."
        )

        TechComponentCard(
            category = "Asynchronous Task Orchestration",
            technology = "Redis + Celery + Server-Sent Events (SSE)",
            rationale = "For heavy 4K videos or batch processing, mobile clients upload directly to S3/Cloud Storage via pre-signed URLs, trigger async Celery workers, and receive real-time stage progress over SSE."
        )

        TechComponentCard(
            category = "Cloud GPU Infrastructure",
            technology = "AWS EC2 g5.xlarge (NVIDIA A10G 24GB) with K8s HPA",
            rationale = "Horizontal Pod Autoscaling (KEDA) based on Redis queue depth. Spot instances reduce GPU hourly costs from $1.00/hr to $0.32/hr during non-peak hours."
        )

        TechComponentCard(
            category = "On-Device vs Cloud Hybrid Strategy",
            technology = "MediaPipe on-device (<200ms preview) + Cloud (4K master)",
            rationale = "Device performs zero-latency face cropping and interactive low-res preview filter on-device. The heavy 4K CodeFormer + Real-ESRGAN render runs on cloud A10G."
        )
    }
}

@Composable
private fun RoadmapTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MilestoneCard(
            phase = "PHASE 1 (MVP)",
            timeline = "Weeks 1 - 6",
            title = "Core Photo Upscale & Face Restoration",
            items = listOf(
                "CodeFormer + Real-ESRGAN pipeline on FastAPI + A10G",
                "Mobile before/after interactive comparison slider",
                "Direct S3 upload via pre-signed URL + progress webhooks",
                "Google Play Billing with Weekly VIP and 3-day trial",
                "Free tier daily quota (3 enhancements / day with watermark)"
            ),
            isCurrent = true
        )

        MilestoneCard(
            phase = "PHASE 2",
            timeline = "Weeks 7 - 14",
            title = "Video Upscaling & Generative Styles",
            items = listOf(
                "RealBasicVSR + RIFE 60fps frame-by-frame video enhancer",
                "FFmpeg chunking & parallel multi-GPU frame batching",
                "AI Studio Avatars using SDXL + InstantID + ControlNet",
                "DDColor neural historical colorizer for black & white photos",
                "Push notifications on async video completion"
            ),
            isCurrent = false
        )

        MilestoneCard(
            phase = "PHASE 3",
            timeline = "Weeks 15 - 24",
            title = "Scale, On-Device NPU & Global Expansion",
            items = listOf(
                "Quantized INT8 on-device model deployment (Snapdragon NPU / Apple ANE)",
                "Global multi-region inference clusters (US-East, EU-Central, AP-Southeast)",
                "Enterprise API & Web SDK licensing for photo restoration",
                "Annual Pro bundle optimization ($39.99/yr) with 84% margin"
            ),
            isCurrent = false
        )
    }
}

@Composable
private fun ArchitectureStepCard(
    step: String,
    title: String,
    model: String,
    description: String,
    inputTensor: String,
    outputTensor: String,
    latency: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF101726),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(StudioElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = latency,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = StudioAmberGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Model: $model",
                fontSize = 11.sp,
                color = StudioElectricCyan,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tensor: $inputTensor ➔ $outputTensor",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = Color(0xFF7DD3FC)
                )
            }
        }
    }
}

@Composable
private fun TechComponentCard(
    category: String,
    technology: String,
    rationale: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF101726),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = category, fontSize = 11.sp, color = StudioElectricCyan, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = technology, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = rationale, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun MilestoneCard(
    phase: String,
    timeline: String,
    title: String,
    items: List<String>,
    isCurrent: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) Color(0xFF0C243B) else Color(0xFF101726),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrent) StudioElectricCyan else Color(0xFF222E46)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrent) StudioElectricCyan else Color(0xFF1E293B)
                    ) {
                        Text(
                            text = phase,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.Black else TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(text = timeline, fontSize = 11.sp, color = StudioAmberGold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "• ", color = StudioElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = item, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
                }
            }
        }
    }
}
