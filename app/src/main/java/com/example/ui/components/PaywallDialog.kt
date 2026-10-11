package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StudioAmberGold
import com.example.ui.theme.StudioElectricCyan
import com.example.ui.theme.StudioNeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PaywallDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onPlanSubscribed: (String) -> Unit
) {
    if (!isOpen) return

    var selectedPlanId by remember { mutableStateOf("annual_pro") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(28.dp))
                .testTag("paywall_dialog"),
            color = Color(0xFF0B0F19)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "PRO PASS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = StudioElectricCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hero Headline
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(StudioElectricCyan, StudioNeonPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Unlock Remini Ultra AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Professional 4K Face Restorer & Video Upscaler",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Feature Highlights
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureRow(Icons.Default.HighQuality, "Unlimited 4K Super-Resolution & Zero Watermarks")
                    FeatureRow(Icons.Default.Videocam, "60 FPS Video Frame-by-Frame AI Deblurring")
                    FeatureRow(Icons.Default.ElectricBolt, "Dedicated A10G Cloud GPU Queue (Sub-1s Processing)")
                    FeatureRow(Icons.Default.LockOpen, "Full AI Avatar & Retro Colorizer Model Suite")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Plan 1: Annual Pro (Best Value)
                PlanSelectCard(
                    id = "annual_pro",
                    title = "Annual VIP Access",
                    price = "$39.99 / year",
                    subtitle = "$0.77 / week • Save 84%",
                    badge = "BEST VALUE (70% OFF)",
                    badgeColor = StudioAmberGold,
                    isSelected = selectedPlanId == "annual_pro",
                    onSelect = { selectedPlanId = "annual_pro" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Plan 2: Weekly with 3-Day Free Trial
                PlanSelectCard(
                    id = "weekly_trial",
                    title = "Weekly + 3-Day Free Trial",
                    price = "$4.99 / week",
                    subtitle = "Cancel anytime before trial ends",
                    badge = "3 DAYS FREE",
                    badgeColor = StudioElectricCyan,
                    isSelected = selectedPlanId == "weekly_trial",
                    onSelect = { selectedPlanId = "weekly_trial" }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Plan 3: Pay-As-You-Go Credits
                PlanSelectCard(
                    id = "credits_100",
                    title = "100 Enhancement Credits",
                    price = "$6.99 one-time",
                    subtitle = "No recurring charges • Never expires",
                    badge = "CREDIT PACK",
                    badgeColor = Color(0xFFA855F7),
                    isSelected = selectedPlanId == "credits_100",
                    onSelect = { selectedPlanId = "credits_100" }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Primary CTA button
                Button(
                    onClick = {
                        onPlanSubscribed(selectedPlanId)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscribe_cta_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StudioElectricCyan
                    )
                ) {
                    Text(
                        text = if (selectedPlanId == "weekly_trial") "Start 3-Day Free Trial" else "Subscribe & Unlock All Features",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Secured by Google Play Billing. Cancel anytime in Play Store settings.",
                    fontSize = 10.sp,
                    color = TextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = StudioElectricCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlanSelectCard(
    id: String,
    title: String,
    price: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) StudioElectricCyan else Color(0xFF26334D),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0E2238) else Color(0xFF131B2A)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = price,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) StudioElectricCyan else TextPrimary
                )

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isSelected) StudioElectricCyan else Color(0xFF334155),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(20.dp)
                )
            }
        }
    }
}
