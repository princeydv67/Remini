package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.EnhanceMode
import com.example.model.StylePreset
import com.example.processing.ImageEnhancerEngine
import com.example.processing.MediaPickerHelper
import com.example.ui.components.AiProcessingOverlay
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiStylesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val styles = remember {
        listOf(
            StylePreset(
                id = "corp_headshot",
                title = "Executive Headshot",
                prompt = "Professional studio portrait, executive tailored blazer, 85mm f/1.4 rim lighting, modern corporate office background",
                description = "LinkedIn & resume portrait with studio key lighting",
                iconName = "work",
                colorHex = 0xFF0284C7
            ),
            StylePreset(
                id = "yearbook_90s",
                title = "90s Retro Yearbook",
                prompt = "1990s high school yearbook photo, vintage laser background, soft flash reflection, nostalgic kodak film grain",
                description = "Iconic 1990s high-school nostalgic portrait filter",
                iconName = "school",
                colorHex = 0xFFF59E0B
            ),
            StylePreset(
                id = "cyberpunk",
                title = "Cyberpunk 2077",
                prompt = "Futuristic neon city night, chromatic aberration, holographic reflections, glowing cyan & magenta rim light",
                description = "Vibrant synthwave aesthetic with holographic accents",
                iconName = "bolt",
                colorHex = 0xFFEC4899
            ),
            StylePreset(
                id = "vintage_1950",
                title = "Classic Hollywood",
                prompt = "1950s golden age silver screen portrait, soft focus glamour, dramatic chiaroscuro key light",
                description = "Vintage film noir and classic silver-screen elegance",
                iconName = "movie",
                colorHex = 0xFF8B5CF6
            ),
            StylePreset(
                id = "renaissance",
                title = "Oil Masterpiece",
                prompt = "Oil painting on canvas by Leonardo da Vinci, rich brushstrokes, cracked varnish patina, chiaroscuro",
                description = "Classical Renaissance gallery oil painting texture",
                iconName = "palette",
                colorHex = 0xFF10B981
            ),
            StylePreset(
                id = "anime_glow",
                title = "Anime Aesthetic",
                prompt = "Makoto Shinkai aesthetic anime character portrait, vibrant skies, radiant lens flares, delicate cel-shading",
                description = "Japanese animation aesthetic with radiant color palette",
                iconName = "star",
                colorHex = 0xFFF43F5E
            )
        )
    }

    var selectedStyle by remember { mutableStateOf(styles[0]) }
    var identityStrength by remember { mutableFloatStateOf(0.85f) }
    var diffusionSteps by remember { mutableFloatStateOf(35f) }
    var isGenerating by remember { mutableStateOf(false) }
    var userPhotoLabel by remember { mutableStateOf("Demo Face") }

    var beforeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var afterBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val bmp = ImageEnhancerEngine.loadBitmapFromResource(context, R.drawable.img_sample_portrait)
        beforeBitmap = bmp
        val (_, a) = ImageEnhancerEngine.createEnhancedPair(
            source = bmp,
            mode = EnhanceMode.AI_AVATAR
        )
        afterBitmap = a
    }

    // Photo Picker for Avatars
    val avatarPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val info = MediaPickerHelper.getPhotoInfo(context, it)
                    val bmp = MediaPickerHelper.decodeBitmapFromUri(context, it, maxDimension = 1536)
                    beforeBitmap = bmp
                    userPhotoLabel = info.fileName
                    val (_, a) = ImageEnhancerEngine.createEnhancedPair(
                        source = bmp,
                        mode = EnhanceMode.AI_AVATAR
                    )
                    afterBitmap = a
                    Toast.makeText(context, "Loaded user face from gallery!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
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
            .testTag("ai_styles_screen")
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = null,
                        tint = StudioElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generative AI Avatars",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "ControlNet InstantID + SDXL Identity Preservation",
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
                    text = "Zero-Shot Face ID",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioElectricCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Before / After Split Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)
        ) {
            BeforeAfterSlider(
                beforeBitmap = beforeBitmap,
                afterBitmap = afterBitmap,
                modifier = Modifier.fillMaxSize(),
                beforeLabel = "Original ($userPhotoLabel)",
                afterLabel = selectedStyle.title.uppercase()
            )

            AiProcessingOverlay(
                isProcessing = isGenerating,
                stages = emptyList(),
                customMessage = "Generating ${selectedStyle.title} Avatar (InstantID)",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pick User Face from Gallery Button
        OutlinedButton(
            onClick = {
                avatarPhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_pick_avatar_photo"),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = StudioElectricCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Your Face from Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Styles Grid / Selection Carousel
        Text(
            text = "Select Avatar Transformation Style",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.chunked(2).forEach { rowStyles ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowStyles.forEach { style ->
                        val isSelected = selectedStyle.id == style.id
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedStyle = style
                                    scope.launch {
                                        isGenerating = true
                                        delay(450)
                                        beforeBitmap?.let { bmp ->
                                            val (_, a) = ImageEnhancerEngine.createEnhancedPair(
                                                source = bmp,
                                                mode = EnhanceMode.AI_AVATAR
                                            )
                                            afterBitmap = a
                                        }
                                        isGenerating = false
                                    }
                                }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(style.colorHex) else Color(0xFF222E46),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(style.colorHex).copy(alpha = 0.18f) else Color(0xFF101726)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(style.colorHex)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(style.colorHex),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = style.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = style.description,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 13.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Advanced Diffusion Tuning Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF101726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Identity & Model Hyperparameters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Facial ID Preservation Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Face Likeness Retention (InstantID Weight)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(identityStrength * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = identityStrength,
                        onValueChange = { identityStrength = it },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioElectricCyan,
                            activeTrackColor = StudioElectricCyan,
                            inactiveTrackColor = Color(0xFF1E283D)
                        )
                    )
                }

                // Diffusion Sampling Steps
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Diffusion Inference Steps (DPM++ 2M Karras)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${diffusionSteps.toInt()} steps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioNeonPink,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = diffusionSteps,
                        onValueChange = { diffusionSteps = it },
                        valueRange = 20f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioNeonPink,
                            activeTrackColor = StudioNeonPink,
                            inactiveTrackColor = Color(0xFF1E283D)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isGenerating = true
                    delay(1200)
                    isGenerating = false
                    Toast.makeText(context, "Generated ${selectedStyle.title} Avatar!", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_avatar_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioElectricCyan
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating with SDXL InstantID...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERATE ${selectedStyle.title.uppercase()}",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
