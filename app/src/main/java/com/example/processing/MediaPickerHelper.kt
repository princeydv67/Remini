package com.example.processing

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class SelectedPhotoInfo(
    val uri: Uri,
    val fileName: String,
    val fileSizeFormatted: String,
    val width: Int,
    val height: Int,
    val megapixelText: String,
    val mimeType: String
)

object MediaPickerHelper {

    /**
     * Extracts rich metadata from a user-selected Uri via ContentResolver
     */
    suspend fun getPhotoInfo(context: Context, uri: Uri): SelectedPhotoInfo = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileName = "photo_${System.currentTimeMillis()}.jpg"
        var sizeBytes = 0L
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        // Query file name and size from ContentResolver
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { fileName = it }
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        sizeBytes = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}

        // Decode bounds without loading full pixels
        var width = 1080
        var height = 1920
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(stream, null, options)
                if (options.outWidth > 0 && options.outHeight > 0) {
                    width = options.outWidth
                    height = options.outHeight
                }
            }
        } catch (_: Exception) {}

        val formattedSize = formatFileSize(sizeBytes)
        val megapixels = (width.toLong() * height.toLong()) / 1_000_000.0
        val megapixelText = if (megapixels > 0.1) String.format("%.1f MP", megapixels) else "${width}x${height}"

        SelectedPhotoInfo(
            uri = uri,
            fileName = fileName,
            fileSizeFormatted = formattedSize,
            width = width,
            height = height,
            megapixelText = megapixelText,
            mimeType = mimeType
        )
    }

    /**
     * Decodes the Bitmap from Uri safely handling downsampling (max dimension 2048) and EXIF rotation
     */
    suspend fun decodeBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 1. Measure dimensions
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        val origW = options.outWidth
        val origH = options.outHeight

        // 2. Compute sample size to avoid OOM
        var sampleSize = 1
        val maxSide = max(origW, origH)
        while (maxSide / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        // 3. Decode scaled bitmap
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inMutable = true
        }

        val rawBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalStateException("Could not decode bitmap from URI")

        // 4. Correct EXIF orientation
        val rotationAngle = getExifOrientationDegrees(resolver, uri)
        if (rotationAngle != 0) {
            val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
            val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            if (rotated != rawBitmap) {
                rawBitmap.recycle()
            }
            rotated
        } else {
            rawBitmap
        }
    }

    private fun getExifOrientationDegrees(resolver: ContentResolver, uri: Uri): Int {
        return try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Unknown size"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes bytes"
        }
    }
}
