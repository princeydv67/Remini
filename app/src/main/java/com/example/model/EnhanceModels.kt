package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector

enum class EnhanceMode(val title: String, val subtitle: String, val badge: String) {
    FACE_UNBLUR("Face Unblur", "CodeFormer & GFPGAN Restoration", "POPULAR"),
    SUPER_RES("4K Upscale", "Real-ESRGAN Texture Synthesis", "ULTRA HD"),
    COLORIZE("Colorize B&W", "DDColor Neural Palette Transfer", "RETRO"),
    SCRATCH_REPAIR("Scratch Repair", "Deep Inpainting & Artifact Fix", "RESTORE"),
    AI_AVATAR("AI Avatar", "SDXL & ControlNet Photorealism", "CREATIVE")
}

data class PipelineStage(
    val id: String,
    val name: String,
    val modelName: String,
    val description: String,
    val latencyMs: Int,
    val tensorInput: String,
    val tensorOutput: String,
    var status: StageStatus = StageStatus.PENDING
)

enum class StageStatus {
    PENDING, PROCESSING, COMPLETED
}

data class SamplePhoto(
    val id: String,
    val title: String,
    val category: String,
    val recommendedMode: EnhanceMode,
    @DrawableRes val drawableRes: Int,
    val tags: List<String>
)

data class StylePreset(
    val id: String,
    val title: String,
    val prompt: String,
    val description: String,
    val iconName: String,
    val colorHex: Long
)

data class GpuEconomics(
    val monthlyActiveUsers: Float = 50_000f,
    val imagesPerUserMonth: Float = 12f,
    val cloudProvider: CloudProvider = CloudProvider.AWS_SELF_HOSTED_A10G,
    val subscriptionPriceWeekly: Float = 4.99f,
    val conversionRatePercent: Float = 4.5f
)

enum class CloudProvider(val label: String, val costPerImage: Float, val latencySec: Float) {
    AWS_SELF_HOSTED_A10G("AWS EC2 A10G (FastAPI + Triton)", 0.0028f, 1.4f),
    GCP_L4_SPOT("GCP L4 GPU Spot Instances", 0.0022f, 1.6f),
    REPLICATE_SERVERLESS("Replicate Serverless API", 0.0095f, 3.2f),
    FAL_AI_SERVERLESS("Fal.ai Optimized Serverless", 0.0060f, 1.1f),
    ON_DEVICE_HYBRID("On-Device NPU + Cloud Hybrid", 0.0011f, 0.6f)
}

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceText: String,
    val billingCycleText: String,
    val discountBadge: String?,
    val isPopular: Boolean,
    val features: List<String>
)
