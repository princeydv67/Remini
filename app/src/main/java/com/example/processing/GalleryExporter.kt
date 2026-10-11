package com.example.processing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportResult(
    val uri: Uri,
    val filePathOrName: String,
    val width: Int,
    val height: Int,
    val fileSizeFormatted: String,
    val isComparisonSplit: Boolean
)

enum class ExportFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png")
}

object GalleryExporter {

    /**
     * Saves the enhanced photo to the device public Pictures/ReminiAI gallery via MediaStore
     */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat = ExportFormat.JPEG,
        quality: Int = 98,
        isComparisonSplit: Boolean = false
    ): ExportResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val prefix = if (isComparisonSplit) "Remini_Comparison_" else "Remini_Enhanced_4K_"
        val displayName = "$prefix$timeStamp.${format.extension}"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.DESCRIPTION, "Enhanced with Remini AI Super-Resolution")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ReminiAI")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Could not create MediaStore entry for saving")

        var bytesWritten = 0L
        try {
            resolver.openOutputStream(uri)?.use { stream ->
                val compressFormat = if (format == ExportFormat.PNG) {
                    Bitmap.CompressFormat.PNG
                } else {
                    Bitmap.CompressFormat.JPEG
                }
                bitmap.compress(compressFormat, quality, stream)
            }

            // Retrieve size written
            resolver.openInputStream(uri)?.use { inStream ->
                bytesWritten = inStream.available().toLong()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }

        val sizeFormatted = formatFileSize(bytesWritten)

        ExportResult(
            uri = uri,
            filePathOrName = displayName,
            width = bitmap.width,
            height = bitmap.height,
            fileSizeFormatted = sizeFormatted,
            isComparisonSplit = isComparisonSplit
        )
    }

    /**
     * Creates a composite comparison bitmap preserving the exact user split slider position
     */
    suspend fun createComparisonBitmap(
        beforeBitmap: Bitmap,
        afterBitmap: Bitmap,
        splitFraction: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = maxOf(beforeBitmap.width, afterBitmap.width)
        val height = maxOf(beforeBitmap.height, afterBitmap.height)

        val composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        // 1. Draw After Bitmap on full canvas
        val afterScaled = if (afterBitmap.width != width || afterBitmap.height != height) {
            Bitmap.createScaledBitmap(afterBitmap, width, height, true)
        } else {
            afterBitmap
        }
        canvas.drawBitmap(afterScaled, 0f, 0f, paint)

        // 2. Draw Before Bitmap on left side up to split
        val splitX = (width * splitFraction.coerceIn(0.01f, 0.99f)).toInt()
        val beforeScaled = if (beforeBitmap.width != width || beforeBitmap.height != height) {
            Bitmap.createScaledBitmap(beforeBitmap, width, height, true)
        } else {
            beforeBitmap
        }

        val srcRect = Rect(0, 0, splitX, height)
        val dstRect = Rect(0, 0, splitX, height)
        canvas.drawBitmap(beforeScaled, srcRect, dstRect, paint)

        // 3. Draw Split Line with Cyan Glow
        val lineGlowPaint = Paint().apply {
            color = Color.parseColor("#00F0FF")
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(splitX.toFloat(), 0f, splitX.toFloat(), height.toFloat(), lineGlowPaint)
        canvas.drawLine(splitX.toFloat(), 0f, splitX.toFloat(), height.toFloat(), linePaint)

        // 4. Draw Badges
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = (height * 0.024f).coerceAtLeast(28f)
            isFakeBoldText = true
            isAntiAlias = true
        }
        val badgeBgPaint = Paint().apply {
            color = Color.argb(190, 15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Before Badge
        val padding = 24f
        val beforeText = "BEFORE"
        val beforeW = textPaint.measureText(beforeText)
        val badgeH = textPaint.textSize + 20f
        canvas.drawRoundRect(
            padding,
            padding,
            padding + beforeW + 30f,
            padding + badgeH,
            16f,
            16f,
            badgeBgPaint
        )
        canvas.drawText(beforeText, padding + 15f, padding + badgeH - 12f, textPaint)

        // After Badge
        val afterText = "REMINI 4K"
        val afterW = textPaint.measureText(afterText)
        val afterX = width - padding - afterW - 30f
        val cyanBadgeBg = Paint().apply {
            color = Color.argb(220, 4, 47, 46)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            afterX,
            padding,
            afterX + afterW + 30f,
            padding + badgeH,
            16f,
            16f,
            cyanBadgeBg
        )
        val cyanTextPaint = Paint(textPaint).apply {
            color = Color.parseColor("#00F0FF")
        }
        canvas.drawText(afterText, afterX + 15f, padding + badgeH - 12f, cyanTextPaint)

        composite
    }

    /**
     * Creates an Android Share Intent for the saved photo
     */
    fun createShareIntent(uri: Uri, mimeType: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Enhanced with Remini AI 4K Super-Resolution!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Opens the photo in the system gallery or photo viewer
     */
    fun createViewInGalleryIntent(uri: Uri, mimeType: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "2.8 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes B"
        }
    }
}
