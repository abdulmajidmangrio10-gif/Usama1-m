package com.example.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var defaultCanvas by remember { mutableStateOf("9:16 (Reels/TikTok)") }
    var defaultTextStyle by remember { mutableStateOf("Arabic Gold 🌟") }
    var quranFont by remember { mutableStateOf("Amiri Classical Quranic") }
    var urduFont by remember { mutableStateOf("Jameel Noori Nastaliq") }
    var exportQuality by remember { mutableStateOf("1080p (Full HD)") }
    var exportFps by remember { mutableStateOf("30 FPS") }
    var isDarkMode by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TOP HEADER: Official App Logo, Hafiz Abdul Majid Mangrio, Quran Video Editor
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF04241B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InShotYellow.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_quran_editor_official),
                            contentDescription = "Official Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Quran Video Caption & Editor",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hafiz Abdul Majid Mangrio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = InShotYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Professional Quran Reels, Subtitles & Video Studio",
                            fontSize = 11.sp,
                            color = Color(0xFFB2DFDB)
                        )
                    }
                }
            }

            // SECTION 1: EDITING
            item {
                SettingsSection(title = "EDITING") {
                    SettingsValueItem(
                        icon = Icons.Default.AspectRatio,
                        title = "Default Canvas",
                        value = defaultCanvas,
                        onClick = {
                            defaultCanvas = if (defaultCanvas.contains("9:16")) "16:9 (YouTube)" else "9:16 (Reels/TikTok)"
                        }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Title,
                        title = "Default Text Style",
                        value = defaultTextStyle,
                        onClick = {
                            defaultTextStyle = if (defaultTextStyle.contains("Gold")) "Clean White ⚪" else "Arabic Gold 🌟"
                        }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.MenuBook,
                        title = "Quran Font",
                        value = quranFont,
                        onClick = {
                            quranFont = if (quranFont.contains("Amiri")) "Uthman Taha Script" else "Amiri Classical Quranic"
                        }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Translate,
                        title = "Urdu Font",
                        value = urduFont,
                        onClick = {
                            urduFont = if (urduFont.contains("Jameel")) "Alvi Nastaleeq" else "Jameel Noori Nastaliq"
                        }
                    )
                }
            }

            // SECTION 2: VIDEO
            item {
                SettingsSection(title = "VIDEO") {
                    SettingsValueItem(
                        icon = Icons.Default.HighQuality,
                        title = "Export Quality",
                        value = exportQuality,
                        onClick = {
                            exportQuality = when (exportQuality) {
                                "1080p (Full HD)" -> "4K (Ultra HD)"
                                "4K (Ultra HD)" -> "720p (HD)"
                                else -> "1080p (Full HD)"
                            }
                        }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Speed,
                        title = "FPS",
                        value = exportFps,
                        onClick = {
                            exportFps = if (exportFps == "30 FPS") "60 FPS" else "30 FPS"
                        }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Default Video Volume",
                        value = "100%",
                        onClick = {}
                    )
                }
            }

            // SECTION 3: AUDIO
            item {
                SettingsSection(title = "AUDIO") {
                    SettingsValueItem(
                        icon = Icons.Default.MusicNote,
                        title = "Audio Volume",
                        value = "85%",
                        onClick = {}
                    )
                    SettingsValueItem(
                        icon = Icons.Default.VolumeMute,
                        title = "Original Video Audio",
                        value = "Enabled",
                        onClick = {}
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Mic,
                        title = "Voice Settings",
                        value = "Studio Clarity Noise Reduction",
                        onClick = {}
                    )
                }
            }

            // SECTION 4: APPEARANCE
            item {
                SettingsSection(title = "APPEARANCE") {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Professional studio dark theme",
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Palette,
                        title = "Theme Color",
                        value = "Islamic Gold & Emerald",
                        onClick = {}
                    )
                }
            }

            // SECTION 5: STORAGE
            item {
                SettingsSection(title = "STORAGE") {
                    SettingsValueItem(
                        icon = Icons.Default.Folder,
                        title = "Save Location",
                        value = "Movies/QuranVideoEditor",
                        onClick = {}
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Download,
                        title = "Export Folder",
                        value = "DCIM/QuranReels",
                        onClick = {}
                    )
                }
            }

            // SECTION 6: ABOUT
            item {
                SettingsSection(title = "ABOUT") {
                    SettingsValueItem(
                        icon = Icons.Default.Info,
                        title = "App Information",
                        value = "Version 1.0.0 (Pro Build)",
                        onClick = {}
                    )
                    SettingsValueItem(
                        icon = Icons.Default.Person,
                        title = "Creator Information",
                        value = "Hafiz Abdul Majid Mangrio",
                        onClick = {}
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "© 2026 Hafiz Abdul Majid Mangrio. All Rights Reserved.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = InShotYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsValueItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = InShotYellow, modifier = Modifier.size(20.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(text = value, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = InShotYellow, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = TextMuted, fontSize = 10.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = InShotYellow
            )
        )
    }
}
