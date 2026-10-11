package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainAppContainer() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentTheme = LocalAppTheme.current

    val navItems = listOf(
        NavItem("Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_studio"),
        NavItem("Video 4K", Icons.Filled.Videocam, Icons.Outlined.Videocam, "nav_video"),
        NavItem("AI Styles", Icons.Filled.Brush, Icons.Outlined.Brush, "nav_styles"),
        NavItem("Blueprint", Icons.Filled.Architecture, Icons.Outlined.Architecture, "nav_blueprint")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = currentTheme.surfaceColor,
                contentColor = if (currentTheme.isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (currentTheme.isDark) Color.Black else Color.White,
                            selectedTextColor = currentTheme.primaryColor,
                            indicatorColor = currentTheme.primaryColor,
                            unselectedIconColor = if (currentTheme.isDark) TextMuted else Color(0xFF94A3B8),
                            unselectedTextColor = if (currentTheme.isDark) TextMuted else Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.backgroundColor)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> EnhanceStudioScreen(onNavigateToBlueprint = { selectedTab = 3 })
                1 -> VideoEnhancerScreen()
                2 -> AiStylesScreen()
                3 -> BlueprintScreen()
            }
        }
    }
}
