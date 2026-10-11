package com.example.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.example.model.EnhanceMode
import com.example.model.PipelineStage
import com.example.model.StageStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.max
import kotlin.math.min

object ImageEnhancerEngine {

    fun getDefaultPipeline(mode: EnhanceMode): List<PipelineStage> {
        return when (mode) {
            EnhanceMode.FACE_UNBLUR -> listOf(
                PipelineStage(
                    id = "p1",
                    name = "Face Landmark Detection",
                    modelName = "RetinaFace / MediaPipe Iris",
                    description = "Extracts 68 3D facial landmarks and 5-point affine alignment crop",
                    latencyMs = 180,
                    tensorInput = "1x3x1080x1920",
                    tensorOutput = "512x512 aligned crop"
                ),
                PipelineStage(
                    id = "p2",
                    name = "Facial Detail Hallucination",
                    modelName = "CodeFormer (VQGAN Prior, w=0.75)",
                    description = "Restores iris reflections, eyelashes, skin pores & teeth from learned codebook",
                    latencyMs = 740,
                    tensorInput = "1x3x512x512",
                    tensorOutput = "1x3x512x512 (HQ restored)"
                ),
                PipelineStage(
                    id = "p3",
                    name = "Background Super-Resolution",
                    modelName = "Real-ESRGAN_x4plus",
                    description = "Upscales background hair, clothing, and scene context by 4x without artifacts",
                    latencyMs = 620,
                    tensorInput = "1x3x540x960",
                    tensorOutput = "1x3x2160x3840 (4K)"
                ),
                PipelineStage(
                    id = "p4",
                    name = "Poisson Boundary Blending",
                    modelName = "Seamless Clone & Convex Mask",
                    description = "Soft gaussian feathering blends restored face back into upscaled canvas",
                    latencyMs = 110,
                    tensorInput = "Restored Face + 4K Canvas",
                    tensorOutput = "1x3x2160x3840 blended"
                ),
                PipelineStage(
                    id = "p5",
                    name = "Color & Tone Harmonization",
                    modelName = "CLAHE + Neural White Balance",
                    description = "Matches skin undertones and eliminates sensor noise banding",
                    latencyMs = 90,
                    tensorInput = "RGB 4K",
                    tensorOutput = "10-bit HDR Master"
                )
            )

            EnhanceMode.SUPER_RES -> listOf(
                PipelineStage(
                    id = "sr1",
                    name = "Tiling & Patch Extraction",
                    modelName = "Overlapping Window Slicer (64px overlap)",
                    description = "Prevents GPU VRAM OOM on high megapixel inputs",
                    latencyMs = 95,
                    tensorInput = "Full Res Image",
                    tensorOutput = "16x 256x256 Tiles"
                ),
                PipelineStage(
                    id = "sr2",
                    name = "Deep Residual Super-Resolution",
                    modelName = "Real-ESRGAN (Compact 64M param)",
                    description = "Synthesizes micro-textures, text sharpness, and edge clarity",
                    latencyMs = 820,
                    tensorInput = "16x 256x256",
                    tensorOutput = "16x 1024x1024"
                ),
                PipelineStage(
                    id = "sr3",
                    name = "Seam Stitching & Multi-Scale Fusion",
                    modelName = "Cosine Weight Merging",
                    description = "Eliminates tile boundary seams and produces unified 4K canvas",
                    latencyMs = 140,
                    tensorInput = "Tiles Array",
                    tensorOutput = "3840x2160 Ultra HD"
                )
            )

            EnhanceMode.COLORIZE -> listOf(
                PipelineStage(
                    id = "col1",
                    name = "Luminance Channel Separation",
                    modelName = "LAB Space Color Transform",
                    description = "Retains sharp original L* luminance channel while creating empty a* b* channels",
                    latencyMs = 80,
                    tensorInput = "Grayscale 1x1xWxH",
                    tensorOutput = "L channel (0-100)"
                ),
                PipelineStage(
                    id = "col2",
                    name = "Semantic Palette Estimation",
                    modelName = "DDColor / DeOldify Hybrid Transformer",
                    description = "Infers authentic sky blue, foliage green, and realistic human skin pigments",
                    latencyMs = 690,
                    tensorInput = "1x1x512x512 (L)",
                    tensorOutput = "1x2x512x512 (a*, b* chrominance)"
                ),
                PipelineStage(
                    id = "col3",
                    name = "Bilateral Guided Filtering",
                    modelName = "Edge-Preserving Color Fusion",
                    description = "Prevents color bleed across fine hair edges and high-contrast boundaries",
                    latencyMs = 130,
                    tensorInput = "Colorized AB + L",
                    tensorOutput = "Full 24-bit sRGB Color"
                )
            )

            EnhanceMode.SCRATCH_REPAIR -> listOf(
                PipelineStage(
                    id = "scr1",
                    name = "Defect Segmentation Masking",
                    modelName = "U-Net Crack & Dust Detector",
                    description = "Identifies white creases, paper tears, dust speckles, and chemical stains",
                    latencyMs = 210,
                    tensorInput = "1x3x512x512",
                    tensorOutput = "Binary Defect Mask"
                ),
                PipelineStage(
                    id = "scr2",
                    name = "Partial Convolution Inpainting",
                    modelName = "LaMa (Large Mask Inpainting)",
                    description = "Fills missing damaged areas using perceptual contextual priors",
                    latencyMs = 580,
                    tensorInput = "Image + Mask",
                    tensorOutput = "Clean Repaired Image"
                )
            )

            EnhanceMode.AI_AVATAR -> listOf(
                PipelineStage(
                    id = "av1",
                    name = "Face ID Embedding",
                    modelName = "ArcFace / InsightFace",
                    description = "Extracts 512-dim facial identity vector invariant to pose and lighting",
                    latencyMs = 190,
                    tensorInput = "1x3x224x224",
                    tensorOutput = "512-dim ID vector"
                ),
                PipelineStage(
                    id = "av2",
                    name = "Latent Diffusion Generation",
                    modelName = "SDXL + ControlNet Tile & InstantID",
                    description = "Generates studio lighting, tailored clothing, and premium backdrop",
                    latencyMs = 1200,
                    tensorInput = "ID Embedding + Text Prompt",
                    tensorOutput = "1024x1024 Latent Tensor"
                ),
                PipelineStage(
                    id = "av3",
                    name = "Face Swap Refinement & Color Grade",
                    modelName = "SimSwap + LUT Harmonizer",
                    description = "Aligns micro expressions and ensures 100% likeness retention",
                    latencyMs = 310,
                    tensorInput = "Synthesized + Original",
                    tensorOutput = "Master Studio Portrait"
                )
            )
        }
    }

    /**
     * Executes the simulated AI pipeline reporting progress step-by-step
     */
    fun runPipelineStream(mode: EnhanceMode): Flow<List<PipelineStage>> = flow {
        val stages = getDefaultPipeline(mode).map { it.copy() }.toMutableList()
        emit(stages.toList())

        for (i in stages.indices) {
            stages[i] = stages[i].copy(status = StageStatus.PROCESSING)
            emit(stages.toList())

            delay(stages[i].latencyMs.toLong() / 2) // smooth UI feedback
            stages[i] = stages[i].copy(status = StageStatus.COMPLETED)
            emit(stages.toList())
        }
    }

    /**
     * Creates a high-fidelity enhanced bitmap demonstrating the before/after effect
     */
    fun createEnhancedPair(
        source: Bitmap,
        mode: EnhanceMode,
        faceDetail: Float = 0.85f,
        sharpness: Float = 0.75f,
        colorBoost: Float = 0.35f
    ): Pair<Bitmap, Bitmap> {
        val width = source.width
        val height = source.height

        // Before state: either degraded/blurry or original B&W
        val beforeBitmap = when (mode) {
            EnhanceMode.FACE_UNBLUR, EnhanceMode.SUPER_RES -> createDegradedVersion(source)
            EnhanceMode.COLORIZE -> toGrayscale(source)
            EnhanceMode.SCRATCH_REPAIR -> addScratchesAndDust(source)
            EnhanceMode.AI_AVATAR -> source.copy(Bitmap.Config.ARGB_8888, true)
        }

        // After state: crystal clear, sharpened, restored colors
        val afterBitmap = when (mode) {
            EnhanceMode.FACE_UNBLUR -> createRestoredFace(source, faceDetail, sharpness)
            EnhanceMode.SUPER_RES -> createSuperResolution(source, sharpness)
            EnhanceMode.COLORIZE -> createVibrantColorized(source, colorBoost)
            EnhanceMode.SCRATCH_REPAIR -> createRepairedVersion(source)
            EnhanceMode.AI_AVATAR -> createAvatarStylized(source)
        }

        return Pair(beforeBitmap, afterBitmap)
    }

    private fun createDegradedVersion(source: Bitmap): Bitmap {
        // Downscale and upscale with bilinear filter to simulate low-res and blur
        val scale = 0.25f
        val smallW = max(1, (source.width * scale).toInt())
        val smallH = max(1, (source.height * scale).toInt())

        val small = Bitmap.createScaledBitmap(source, smallW, smallH, true)
        val blurry = Bitmap.createScaledBitmap(small, source.width, source.height, true)

        // Add slight haze and desaturation
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0.7f)
        val scaleMatrix = ColorMatrix(floatArrayOf(
            0.9f, 0f, 0f, 0f, 15f,
            0f, 0.9f, 0f, 0f, 15f,
            0f, 0f, 0.9f, 0f, 15f,
            0f, 0f, 0f, 1f, 0f
        ))
        colorMatrix.postConcat(scaleMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(blurry, 0f, 0f, paint)

        return result
    }

    fun toGrayscale(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private fun addScratchesAndDust(source: Bitmap): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val scratchPaint = Paint().apply {
            color = android.graphics.Color.argb(160, 240, 240, 240)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Draw vintage scratches
        val w = source.width.toFloat()
        val h = source.height.toFloat()
        canvas.drawLine(w * 0.22f, h * 0.05f, w * 0.24f, h * 0.92f, scratchPaint)
        canvas.drawLine(w * 0.68f, h * 0.12f, w * 0.65f, h * 0.85f, scratchPaint)
        canvas.drawLine(w * 0.45f, h * 0.35f, w * 0.49f, h * 0.58f, scratchPaint)

        // Draw sepia tone over scratches
        val sepiaFilter = ColorMatrix().apply {
            setScale(1.05f, 0.95f, 0.82f, 1f)
        }
        val overlayPaint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(sepiaFilter)
        }
        canvas.drawBitmap(result, 0f, 0f, overlayPaint)

        return result
    }

    private fun createRestoredFace(source: Bitmap, faceDetail: Float, sharpness: Float): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        // Contrast enhancement & micro detail boosting matrix
        val contrast = 1.0f + (sharpness * 0.25f)
        val brightness = 5f * faceDetail
        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        // Boost vibrance
        val satMatrix = ColorMatrix().apply { setSaturation(1.15f + (faceDetail * 0.1f)) }
        cm.postConcat(satMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return result
    }

    private fun createSuperResolution(source: Bitmap, sharpness: Float): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        val boost = 1.0f + (sharpness * 0.3f)
        val cm = ColorMatrix(floatArrayOf(
            boost, 0f, 0f, 0f, -8f,
            0f, boost, 0f, 0f, -8f,
            0f, 0f, boost, 0f, -8f,
            0f, 0f, 0f, 1f, 0f
        ))
        val sat = ColorMatrix().apply { setSaturation(1.12f) }
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private fun createVibrantColorized(source: Bitmap, colorBoost: Float): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        // Warm colorization matrix that mimics DDColor skin tone and foliage
        val cm = ColorMatrix(floatArrayOf(
            1.15f + colorBoost * 0.1f, 0.05f, 0.0f, 0f, 10f,
            0.02f, 1.08f + colorBoost * 0.1f, 0.0f, 0f, 6f,
            0.0f, 0.02f, 1.02f, 0f, 2f,
            0f, 0f, 0f, 1f, 0f
        ))
        val sat = ColorMatrix().apply { setSaturation(1.35f + colorBoost * 0.2f) }
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private fun createRepairedVersion(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        val cm = ColorMatrix().apply {
            setSaturation(1.05f)
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    private fun createAvatarStylized(source: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        // Cinematic editorial magazine color grade
        val cm = ColorMatrix(floatArrayOf(
            1.22f, -0.05f, -0.02f, 0f, 12f,
            -0.02f, 1.18f, -0.01f, 0f, 8f,
            -0.01f, 0.03f, 1.25f, 0f, 18f,
            0f, 0f, 0f, 1f, 0f
        ))
        val sat = ColorMatrix().apply { setSaturation(1.28f) }
        cm.postConcat(sat)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    fun loadBitmapFromResource(context: Context, resId: Int, maxDim: Int = 1024): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, options)

        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDim || options.outHeight / sampleSize > maxDim) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeResource(context.resources, resId, decodeOptions)
    }
}
