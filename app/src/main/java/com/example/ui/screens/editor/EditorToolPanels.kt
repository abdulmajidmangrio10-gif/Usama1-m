package com.example.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CanvasToolPanel(
    currentRatio: CanvasRatio,
    currentBgType: CanvasBgType,
    currentBgColor: Long,
    onRatioSelected: (CanvasRatio) -> Unit,
    onBackgroundSelected: (CanvasBgType, Long) -> Unit,
    onClose: () -> Unit
) {
    val bgGradients = listOf(
        0xFF121217L to "Dark Slate",
        0xFFFF2A55L to "InShot Crimson",
        0xFF7C4DFFL to "Neon Violet",
        0xFF00E5FFL to "Cyber Cyan",
        0xFFFF6D00L to "Warm Amber",
        0xFF00E676L to "Emerald"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("canvas_tool_panel")
    ) {
        PanelHeader(title = "Canvas & Background", onClose = onClose)

        Spacer(modifier = Modifier.height(14.dp))

        // Ratio Selector
        Text(text = "ASPECT RATIO", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CanvasRatio.values()) { ratio ->
                val isSelected = currentRatio == ratio
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotRed else DarkBorder),
                    modifier = Modifier.clickable { onRatioSelected(ratio) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ratio.label,
                            color = if (isSelected) InShotRed else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(text = ratio.subtitle, color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Style
        Text(text = "BACKGROUND STYLE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Blur Button
            val isBlur = currentBgType == CanvasBgType.BLUR
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isBlur) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isBlur) InShotRed else DarkBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onBackgroundSelected(CanvasBgType.BLUR, currentBgColor) }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.BlurOn, null, tint = if (isBlur) InShotRed else Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Blur Video", color = if (isBlur) InShotRed else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            // Gradient Palette
            bgGradients.take(5).forEach { (colorHex, _) ->
                val isSelected = currentBgType == CanvasBgType.GRADIENT && currentBgColor == colorHex
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else DarkBorder,
                            shape = CircleShape
                        )
                        .clickable { onBackgroundSelected(CanvasBgType.GRADIENT, colorHex) }
                )
            }
        }
    }
}

@Composable
fun FilterToolPanel(
    selectedFilter: FilterType,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    vignette: Float,
    onFilterSelected: (FilterType) -> Unit,
    onAdjustmentsChanged: (Float, Float, Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Filter") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("filter_tool_panel")
    ) {
        PanelHeader(title = "Filter & Effects", onClose = onClose)

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Filter vs Adjust
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Filter", "Adjust").forEach { tab ->
                val isSelected = selectedTab == tab
                Button(
                    onClick = { selectedTab = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) InShotRed else DarkSurfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tab, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "Filter") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(FilterType.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onFilterSelected(filter) }
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (filter) {
                                        FilterType.ORIGINAL -> Color(0xFF4A4D5E)
                                        FilterType.CINEMATIC -> Color(0xFF00796B)
                                        FilterType.VINTAGE -> Color(0xFF8D6E63)
                                        FilterType.CYBERPUNK -> Color(0xFF7B1FA2)
                                        FilterType.BLACK_WHITE -> Color(0xFF212121)
                                        FilterType.WARM -> Color(0xFFFF8F00)
                                        FilterType.VIVID -> Color(0xFFE91E63)
                                        FilterType.GLITCH -> Color(0xFF303F9F)
                                        FilterType.COOL -> Color(0xFF0288D1)
                                        FilterType.SEPIA -> Color(0xFF6D4C41)
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) InShotYellow else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.displayName.take(2).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = filter.displayName,
                            color = if (isSelected) InShotYellow else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        } else {
            // Adjustment sliders
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdjustmentSlider(
                    label = "Brightness",
                    value = brightness,
                    range = -0.5f..0.5f,
                    onValueChange = { onAdjustmentsChanged(it, contrast, saturation, vignette) }
                )
                AdjustmentSlider(
                    label = "Contrast",
                    value = contrast,
                    range = 0.5f..1.8f,
                    onValueChange = { onAdjustmentsChanged(brightness, it, saturation, vignette) }
                )
                AdjustmentSlider(
                    label = "Saturation",
                    value = saturation,
                    range = 0f..2.0f,
                    onValueChange = { onAdjustmentsChanged(brightness, contrast, it, vignette) }
                )
                AdjustmentSlider(
                    label = "Vignette",
                    value = vignette,
                    range = 0f..0.8f,
                    onValueChange = { onAdjustmentsChanged(brightness, contrast, saturation, it) }
                )
            }
        }
    }
}

@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(String.format(Locale.getDefault(), "%.1f", value), color = InShotCyan, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = InShotRed,
                activeTrackColor = InShotRed,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
fun MusicToolPanel(
    onAddTrack: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val stockTracks = listOf(
        "Midnight Drive" to "Synthwave Chill",
        "Summer Groove" to "Tropical Beat",
        "Lo-Fi Study" to "Coffee Beats",
        "Future Bass Drop" to "Electro Party",
        "Acoustic Sunset" to "Warm Guitar",
        "Urban Trap Beat" to "Hype Rhythm"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("music_tool_panel")
    ) {
        PanelHeader(title = "Music & Sound FX", onClose = onClose)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stockTracks) { (title, genre) ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier
                        .clickable { onAddTrack(title, genre) }
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(InShotCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = InShotCyan, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(genre, color = TextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onAddTrack(title, genre) },
                            colors = ButtonDefaults.buttonColors(containerColor = InShotCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Use", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextToolPanel(
    onAddText: (String, Long, Long) -> Unit,
    onClose: () -> Unit
) {
    var textInput by remember { mutableStateOf("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ 🌸") }
    var selectedColor by remember { mutableStateOf(0xFFFFFFFFL) }
    var selectedBg by remember { mutableStateOf(0xAA000000L) }

    val colors = listOf(0xFFFFFFFFL, 0xFFFFD700L, 0xFFFF2A55L, 0xFF00E5FFL, 0xFF00E676L, 0xFF7C4DFFL, 0xFF000000L)

    val arabicTashkeelList = listOf(
        "ـَ" to "\u064E", // Zabar / Fatha
        "ـِ" to "\u0650", // Zer / Kasra
        "ـُ" to "\u064F", // Pesh / Damma
        "ـْ" to "\u0652", // Jazm / Sukun
        "ـّ" to "\u0651", // Tashdeed / Shaddah
        "ـً" to "\u064B", // Do Zabar / Tanween Fath
        "ـٍ" to "\u064D", // Do Zer / Tanween Kasr
        "ـٌ" to "\u064C", // Do Pesh / Tanween Damm
        "ـٰ" to "\u0670", // Khari Zabar
        "ـٖ" to "\u0656", // Khari Zer
        "ـٓ" to "\u0653", // Maddah
        "ـٗ" to "\u0657", // Ulta Pesh
        "﷽" to "﷽",
        "ﷺ" to "ﷺ",
        "ﷻ" to "ﷻ"
    )

    val arabicPresets = listOf(
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
        "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
        "فَاذْكُرُونِي أَذْكُرْكُمْ",
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
        "لَا إِلَٰهَ إِلَّا اللَّهُ",
        "مَا شَاءَ اللَّهُ تَبَارَكَ اللَّهُ",
        "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("text_tool_panel")
    ) {
        PanelHeader(title = "Add Text & Arabic Calligraphy", onClose = onClose)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Text / عبارت (اعراب کے ساتھ)") },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                color = Color.White
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = InShotRed,
                unfocusedBorderColor = DarkBorder
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Arabic Tashkeel / Harakat Bar (زبر، زیر، پیش، جزم، تشدید، کھڑی زبر)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ARABIC TASHKEEL (زبر، زیر، پیش، جزم، تشدید)",
                color = InShotCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(text = "1-Tap Insert", color = TextMuted, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(arabicTashkeelList) { (label, charCode) ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, InShotCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable {
                        textInput += charCode
                    }
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontFamily = AmiriFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Arabic Verses with Full Tashkeel
        Text(text = "QUICK ARABIC PRESETS (مع التشكيل)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(arabicPresets) { preset ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.clickable {
                        textInput = preset
                    }
                ) {
                    Text(
                        text = preset,
                        color = InShotYellow,
                        fontFamily = AmiriFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "TEXT COLOR", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(hex))
                        .border(
                            width = if (selectedColor == hex) 2.5.dp else 1.dp,
                            color = if (selectedColor == hex) InShotYellow else DarkBorder,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = hex }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "FONT CATEGORIES (50+ FONTS)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        val fontCategories = listOf("Arabic", "Urdu", "English", "Calligraphy", "Modern", "Bold", "Elegant")
        var selectedCategory by remember { mutableStateOf("Arabic") }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(fontCategories) { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) InShotYellow.copy(alpha = 0.25f) else DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) InShotYellow else DarkBorder),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) InShotYellow else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (textInput.isNotBlank()) {
                    onAddText(textInput, selectedColor, selectedBg)
                    onClose()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = InShotYellow),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_text_submit_btn")
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add to Timeline (ٹائم لائن پر شامل کریں)", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StickerToolPanel(
    onAddSticker: (String) -> Unit,
    onClose: () -> Unit
) {
    val stickers = listOf(
        "🔥", "✨", "❤️", "😍", "🎉", "⚡", "⭐", "🚀", "👑", "💯",
        "🎬", "🎧", "☕", "🍕", "🏖️", "🌴", "💎", "🎯", "🤙", "👏"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("sticker_tool_panel")
    ) {
        PanelHeader(title = "Stickers & Emojis", onClose = onClose)

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stickers) { emoji ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceVariant,
                    modifier = Modifier
                        .clickable {
                            onAddSticker(emoji)
                            onClose()
                        }
                        .padding(2.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedToolPanel(
    currentSpeed: Float,
    onSpeedChanged: (Float) -> Unit,
    onClose: () -> Unit
) {
    val presets = listOf(0.2f, 0.5f, 1.0f, 1.5f, 2.0f, 4.0f, 8.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("speed_tool_panel")
    ) {
        PanelHeader(title = "Speed Control (${currentSpeed}x)", onClose = onClose)

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = currentSpeed,
            onValueChange = onSpeedChanged,
            valueRange = 0.2f..8.0f,
            colors = SliderDefaults.colors(
                thumbColor = InShotRed,
                activeTrackColor = InShotRed,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presets.forEach { speed ->
                val isSelected = currentSpeed == speed
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) InShotRed else DarkSurfaceVariant,
                    modifier = Modifier.clickable { onSpeedChanged(speed) }
                ) {
                    Text(
                        text = "${speed}x",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VolumeToolPanel(
    currentVolume: Float,
    onVolumeChanged: (Float) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("volume_tool_panel")
    ) {
        PanelHeader(title = "Clip Volume (${(currentVolume * 100).toInt()}%)", onClose = onClose)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = { onVolumeChanged(if (currentVolume == 0f) 1f else 0f) }
            ) {
                Icon(
                    imageVector = if (currentVolume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute",
                    tint = if (currentVolume == 0f) InShotRed else InShotCyan
                )
            }
            Slider(
                value = currentVolume,
                onValueChange = onVolumeChanged,
                valueRange = 0f..2f,
                colors = SliderDefaults.colors(
                    thumbColor = InShotCyan,
                    activeTrackColor = InShotCyan,
                    inactiveTrackColor = DarkSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AutoTextToolPanel(
    captions: List<TextOverlay>,
    isGenerating: Boolean,
    progress: Float,
    statusText: String,
    onGenerateAutoCaptions: (String, CaptionStyle) -> Unit,
    onUpdateCaption: (String, String) -> Unit,
    onDeleteCaption: (String) -> Unit,
    onApplyStyleToAll: (CaptionStyle) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    var selectedLanguage by remember { mutableStateOf("Arabic (مع التشكيل)") }
    var selectedStyle by remember { mutableStateOf(CaptionStyle.ARABIC_GOLD) }
    var activeTab by remember { mutableStateOf(if (captions.any { it.isAutoCaption }) "Captions" else "Generate") }

    val languages = listOf("Arabic (مع التشكيل)", "Urdu (اعراب کے ساتھ)", "Hinglish / Urdu", "English", "Hindi")
    val autoCaptions = captions.filter { it.isAutoCaption }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("auto_text_tool_panel")
    ) {
        PanelHeader(title = "Auto Text / Captions", onClose = onClose)

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Generate vs Edit Captions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Generate", "Captions (${autoCaptions.size})").forEach { tab ->
                val tabKey = if (tab.startsWith("Generate")) "Generate" else "Captions"
                val isSelected = activeTab == tabKey
                Button(
                    onClick = { activeTab = tabKey },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) InShotRed else DarkSurfaceVariant,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(tab, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == "Generate") {
            // Generating in progress animation
            if (isGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = InShotRed,
                        trackColor = DarkSurfaceVariant,
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 5.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% Synchronizing with video timeline...",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            } else {
                // Language selection
                Text(
                    text = "SELECT SPOKEN LANGUAGE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotRed else DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedLanguage = lang }
                        ) {
                            Text(
                                text = lang,
                                color = if (isSelected) InShotRed else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caption Style Presets
                Text(
                    text = "CAPTION ANIMATION STYLE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CaptionStyle.values()) { style ->
                        val isSelected = selectedStyle == style
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) InShotRed.copy(alpha = 0.25f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) InShotYellow else DarkBorder
                            ),
                            modifier = Modifier.clickable {
                                selectedStyle = style
                                onApplyStyleToAll(style)
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(style.bgColorHex),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = style.sampleText,
                                        color = Color(style.textColorHex),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = style.displayName,
                                    color = if (isSelected) InShotYellow else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Generate Button
                Button(
                    onClick = {
                        onGenerateAutoCaptions(selectedLanguage, selectedStyle)
                        activeTab = "Captions"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("generate_auto_text_btn")
                ) {
                    Icon(Icons.Default.ClosedCaption, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Auto Text", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Captions Edit List View
            if (autoCaptions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No auto captions yet.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { activeTab = "Generate" },
                        colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create Auto Text")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${autoCaptions.size} Timed Lines",
                        color = InShotCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onClearAll) {
                        Text("Clear All", color = InShotRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    autoCaptions.forEach { cap ->
                        val timeSeconds = cap.startOffsetMs / 1000f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkBackground
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%04.1fs", timeSeconds),
                                    color = InShotYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }

                            var textState by remember(cap.text) { mutableStateOf(cap.text) }
                            BasicTextField(
                                value = textState,
                                onValueChange = {
                                    textState = it
                                    onUpdateCaption(cap.id, it)
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onDeleteCaption(cap.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PanelHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Done", tint = InShotRed)
        }
    }
}
