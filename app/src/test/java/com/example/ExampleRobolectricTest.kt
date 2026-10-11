package com.example

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.model.CloudProvider
import com.example.model.EnhanceMode
import com.example.processing.ExportFormat
import com.example.processing.GalleryExporter
import com.example.processing.ImageEnhancerEngine
import com.example.processing.SelectedPhotoInfo
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.getThemeColorScheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Remini AI", appName)
    }

    @Test
    fun `verify pipeline stages generated for all modes`() {
        EnhanceMode.values().forEach { mode ->
            val stages = ImageEnhancerEngine.getDefaultPipeline(mode)
            assertTrue("Mode $mode must have pipeline stages", stages.isNotEmpty())
            assertNotNull(stages.first().modelName)
        }
    }

    @Test
    fun `verify cloud provider economics parameters`() {
        val aws = CloudProvider.AWS_SELF_HOSTED_A10G
        assertTrue(aws.costPerImage < 0.005f)
        assertTrue(aws.latencySec <= 2.0f)
    }

    @Test
    fun `verify selected photo info model structure`() {
        val info = SelectedPhotoInfo(
            uri = Uri.parse("content://media/external/images/media/42"),
            fileName = "sample_portrait.jpg",
            fileSizeFormatted = "3.2 MB",
            width = 3024,
            height = 4032,
            megapixelText = "12.2 MP",
            mimeType = "image/jpeg"
        )
        assertEquals("sample_portrait.jpg", info.fileName)
        assertEquals(3024, info.width)
        assertEquals(4032, info.height)
        assertEquals("12.2 MP", info.megapixelText)
    }

    @Test
    fun `verify comparison split bitmap creation`() = runBlocking {
        val before = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val after = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val composite = GalleryExporter.createComparisonBitmap(before, after, 0.5f)
        assertNotNull(composite)
        assertEquals(200, composite.width)
        assertEquals(200, composite.height)
    }

    @Test
    fun `verify share and view gallery intents`() {
        val fakeUri = Uri.parse("content://media/external/images/media/99")
        val shareIntent = GalleryExporter.createShareIntent(fakeUri, ExportFormat.JPEG.mimeType)
        assertNotNull(shareIntent)
        assertEquals("image/jpeg", shareIntent.type)

        val viewIntent = GalleryExporter.createViewInGalleryIntent(fakeUri, ExportFormat.JPEG.mimeType)
        assertNotNull(viewIntent)
        assertEquals(fakeUri, viewIntent.data)
    }

    @Test
    fun `verify app theme modes and color schemes`() {
        AppThemeMode.values().forEach { mode ->
            val scheme = getThemeColorScheme(mode)
            assertNotNull(scheme.primary)
            assertNotNull(scheme.background)
            assertNotNull(scheme.surface)
        }
    }
}
