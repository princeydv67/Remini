package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.PipelineStage
import com.example.model.StageStatus
import com.example.processing.ImageEnhancerEngine
import com.example.processing.MediaPickerHelper
import com.example.processing.SelectedPhotoInfo
import androidx.compose.material.icons.filled.Palette
import com.example.ui.components.AiProcessingOverlay
import com.example.ui.components.BeforeAfterSlider
import com.example.ui.components.ExportSuccessDialog
import com.example.ui.components.PaywallDialog
import com.example.ui.components.PhotoPickerBottomSheet
import com.example.ui.components.PipelineVisualizer
import com.example.ui.components.ThemeSelectorBottomSheet
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioEmeraldGreen
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EnhanceStudioScreen(
    onNavigateToBlueprint: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentTheme = LocalAppTheme.current

    // Screen State
    var selectedMode by remember { mutableStateOf(EnhanceMode.FACE_UNBLUR) }
    var faceDetailWeight by remember { mutableFloatStateOf(0.85f) }
    var sharpnessFactor by remember { mutableFloatStateOf(0.70f) }
    var colorVibrance by remember { mutableFloatStateOf(0.40f) }
    var upscaleFactor by remember { mutableStateOf("4x") }

    var isProcessing by remember { mutableStateOf(false) }
    var pipelineStages by remember { mutableStateOf(ImageEnhancerEngine.getDefaultPipeline(EnhanceMode.FACE_UNBLUR)) }
    var isSubscribed by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var currentSplitFraction by remember { mutableFloatStateOf(0.5f) }
    var freeEnhanceCount by remember { mutableIntStateOf(3) }

    // Selected Photo Metadata
    var currentPhotoInfo by remember { mutableStateOf<SelectedPhotoInfo?>(null) }
    var photoSourceLabel by remember { mutableStateOf("Demo: Natural Portrait") }

    // Bitmaps
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var beforeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var afterBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load initial sample
    LaunchedEffect(Unit) {
        val bmp = ImageEnhancerEngine.loadBitmapFromResource(context, R.drawable.img_sample_portrait)
        originalBitmap = bmp
        val (b, a) = ImageEnhancerEngine.createEnhancedPair(
            source = bmp,
            mode = selectedMode,
            faceDetail = faceDetailWeight,
            sharpness = sharpnessFactor,
            colorBoost = colorVibrance
        )
        beforeBitmap = b
        afterBitmap = a
        photoSourceLabel = "Demo: Portrait Sample (1080×1440)"
    }

    // Update pair when parameters or mode change
    fun refreshEnhancement(mode: EnhanceMode = selectedMode) {
        originalBitmap?.let { src ->
            val (b, a) = ImageEnhancerEngine.createEnhancedPair(
                source = src,
                mode = mode,
                faceDetail = faceDetailWeight,
                sharpness = sharpnessFactor,
                colorBoost = colorVibrance
            )
            beforeBitmap = b
            afterBitmap = a
        }
    }

    // Trigger AI Execution Pipeline
    fun executeAiPipeline() {
        if (!isSubscribed && freeEnhanceCount <= 0) {
            showPaywall = true
            return
        }

        scope.launch {
            isProcessing = true
            pipelineStages = ImageEnhancerEngine.getDefaultPipeline(selectedMode)

            ImageEnhancerEngine.runPipelineStream(selectedMode).collect { updatedStages ->
                pipelineStages = updatedStages
            }

            refreshEnhancement(selectedMode)
            isProcessing = false
            if (!isSubscribed) {
                freeEnhanceCount--
            }
            Toast.makeText(context, "4K Restoration Completed!", Toast.LENGTH_SHORT).show()
        }
    }

    // Load Uri from Photo Picker or File Browser
    fun loadPhotoFromUri(uri: Uri) {
        scope.launch {
            try {
                isProcessing = true
                val info = MediaPickerHelper.getPhotoInfo(context, uri)
                val bitmap = MediaPickerHelper.decodeBitmapFromUri(context, uri, maxDimension = 2048)
                currentPhotoInfo = info
                originalBitmap = bitmap
                photoSourceLabel = "${info.fileName} • ${info.megapixelText}"
                refreshEnhancement(selectedMode)
                isProcessing = false
                Toast.makeText(context, "Loaded: ${info.fileName} (${info.megapixelText})", Toast.LENGTH_SHORT).show()
                executeAiPipeline()
            } catch (e: Exception) {
                isProcessing = false
                Toast.makeText(context, "Failed to load selected photo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Direct system photo picker launcher for immediate 1-tap access
    val directPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { loadPhotoFromUri(it) }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
            .testTag("enhance_studio_screen")
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(currentTheme.primaryColor.copy(alpha = 0.15f))
                        .border(1.dp, currentTheme.primaryColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = currentTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Remini AI Studio",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (currentTheme.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                    Text(
                        text = if (isSubscribed) "PRO UNLIMITED" else "$freeEnhanceCount / 3 Free Daily Pass",
                        fontSize = 11.sp,
                        color = if (isSubscribed) StudioEmeraldGreen else StudioAmberGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Theme Switcher Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentTheme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.primaryColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { showThemeSheet = true }
                        .testTag("btn_change_theme")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme",
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // VIP / PRO Badge Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSubscribed) Color(0xFF064E3B) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSubscribed) StudioEmeraldGreen else StudioAmberGold
                    ),
                    modifier = Modifier
                        .clickable { showPaywall = true }
                        .testTag("vip_pass_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = if (isSubscribed) StudioEmeraldGreen else StudioAmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSubscribed) "VIP ACTIVE" else "GET PRO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSubscribed) StudioEmeraldGreen else StudioAmberGold
                        )
                    }
                }
            }
        }

        // Active Photo Source Header / Gallery Trigger
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showPhotoPickerSheet = true }
                .testTag("active_photo_source_bar"),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF121A2A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334D))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 9.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = StudioElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = photoSourceLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        currentPhotoInfo?.let { info ->
                            Text(
                                text = "${info.width}x${info.height} • ${info.fileSizeFormatted}",
                                fontSize = 10.sp,
                                color = StudioAmberGold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StudioElectricCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioElectricCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChangeCircle,
                            contentDescription = null,
                            tint = StudioElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Change",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Feature Mode Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EnhanceMode.values()) { mode ->
                val isSelected = selectedMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedMode = mode
                        pipelineStages = ImageEnhancerEngine.getDefaultPipeline(mode)
                        refreshEnhancement(mode)
                    },
                    label = {
                        Text(
                            text = mode.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioElectricCyan.copy(alpha = 0.2f),
                        selectedLabelColor = StudioElectricCyan,
                        containerColor = Color(0xFF141C2D),
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFF26334D),
                        selectedBorderColor = StudioElectricCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Showcase: Interactive Before / After Split Slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(0.85f)
        ) {
            BeforeAfterSlider(
                beforeBitmap = beforeBitmap,
                afterBitmap = afterBitmap,
                modifier = Modifier.fillMaxSize(),
                initialSplit = currentSplitFraction,
                onSplitChanged = { currentSplitFraction = it },
                beforeLabel = when (selectedMode) {
                    EnhanceMode.COLORIZE -> "ORIGINAL B&W"
                    EnhanceMode.SCRATCH_REPAIR -> "DAMAGED"
                    else -> "BLURRY / 240P"
                },
                afterLabel = when (selectedMode) {
                    EnhanceMode.COLORIZE -> "NEURAL COLOR"
                    EnhanceMode.AI_AVATAR -> "AI STUDIO HEADSHOT"
                    else -> "RESTORED 4K"
                }
            )

            // Circular Progress Indicator Overlay displayed over image area while AI is processing
            AiProcessingOverlay(
                isProcessing = isProcessing,
                stages = pipelineStages,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Prominent Photo Action Bar: Select from Gallery or Demo Presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    // Launch Android Photo Picker directly or open Bottom Sheet
                    directPhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(46.dp)
                    .testTag("pick_photo_gallery_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioElectricCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Select from Gallery",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }

            OutlinedButton(
                onClick = {
                    showPhotoPickerSheet = true
                },
                modifier = Modifier
                    .weight(0.8f)
                    .height(46.dp)
                    .testTag("more_photo_options_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = StudioAmberGold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Options", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Pipeline Controls & Fine-Tuning Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF101726),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = StudioElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Model Tuning Parameters",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Real-time",
                        fontSize = 11.sp,
                        color = StudioElectricCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Parameter 1: Face CodeFormer Weight
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Face Codebook Fidelity (w)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(faceDetailWeight * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioElectricCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = faceDetailWeight,
                        onValueChange = {
                            faceDetailWeight = it
                            refreshEnhancement()
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioElectricCyan,
                            activeTrackColor = StudioElectricCyan,
                            inactiveTrackColor = Color(0xFF1E283D)
                        )
                    )
                }

                // Parameter 2: Texture Sharpening
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Super-Res Texture Synthesis",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${(sharpnessFactor * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioNeonPink,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = sharpnessFactor,
                        onValueChange = {
                            sharpnessFactor = it
                            refreshEnhancement()
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StudioNeonPink,
                            activeTrackColor = StudioNeonPink,
                            inactiveTrackColor = Color(0xFF1E283D)
                        )
                    )
                }

                // Upscale Factor Selector [2x, 4x, 8x]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Resolution Scale:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("2x", "4x (4K)", "8x (Ultra)").forEach { factor ->
                            val isSel = upscaleFactor == factor
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) StudioElectricCyan else Color(0xFF1E293B),
                                modifier = Modifier.clickable {
                                    upscaleFactor = factor
                                }
                            ) {
                                Text(
                                    text = factor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main CTA Button: Execute Enhancement Pipeline
        Button(
            onClick = { executeAiPipeline() },
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(54.dp)
                .testTag("enhance_action_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioElectricCyan
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Running GPU Pipeline...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RUN ${selectedMode.title.uppercase()} (4K)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pipeline Step Tracker / Latency Inspector
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            PipelineVisualizer(
                stages = pipelineStages,
                isProcessing = isProcessing
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent Primary "Save to Gallery" Action Button
        Button(
            onClick = { showExportDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(54.dp)
                .testTag("btn_save_to_gallery"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioEmeraldGreen
            )
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE TO GALLERY (4K MASTER)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Export / Save Card with split snapshot option
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showExportDialog = true },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F1523),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222E46))
        ) {
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Output: 3840 x 2160 (4K UHD)",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Current slider split: ${(currentSplitFraction * 100).toInt()}% • PNG / JPEG",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "Export Options",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioElectricCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Export to Gallery Dialog
    ExportSuccessDialog(
        isOpen = showExportDialog,
        onDismiss = { showExportDialog = false },
        beforeBitmap = beforeBitmap,
        afterBitmap = afterBitmap,
        currentSplitFraction = currentSplitFraction
    )

    // Photo Picker Bottom Sheet
    PhotoPickerBottomSheet(
        isOpen = showPhotoPickerSheet,
        onDismiss = { showPhotoPickerSheet = false },
        onPhotoSelected = { uri ->
            loadPhotoFromUri(uri)
        },
        onSampleSelected = { resId ->
            val bmp = ImageEnhancerEngine.loadBitmapFromResource(context, resId)
            originalBitmap = bmp
            currentPhotoInfo = null
            if (resId == R.drawable.img_sample_vintage) {
                selectedMode = EnhanceMode.COLORIZE
                pipelineStages = ImageEnhancerEngine.getDefaultPipeline(EnhanceMode.COLORIZE)
                photoSourceLabel = "Demo: 1950s Vintage B&W"
            } else {
                photoSourceLabel = "Demo: Natural Portrait"
            }
            refreshEnhancement(selectedMode)
            Toast.makeText(context, "Loaded Demo Photograph", Toast.LENGTH_SHORT).show()
        },
        currentPhotoInfo = currentPhotoInfo
    )

    // Paywall Dialog
    PaywallDialog(
        isOpen = showPaywall,
        onDismiss = { showPaywall = false },
        onPlanSubscribed = { planId ->
            isSubscribed = true
            Toast.makeText(context, "Welcome to VIP Unlimited!", Toast.LENGTH_LONG).show()
        }
    )

    // Theme Selector Bottom Sheet
    ThemeSelectorBottomSheet(
        isOpen = showThemeSheet,
        onDismiss = { showThemeSheet = false }
    )
}
