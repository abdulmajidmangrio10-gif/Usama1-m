package com.example.ui.screens.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptionStyle
import com.example.ui.theme.*

@Composable
fun BasmalaToolPanel(
    onAddBasmala: (CaptionStyle) -> Unit,
    onClose: () -> Unit
) {
    var selectedStyle by remember { mutableStateOf(CaptionStyle.ARABIC_GOLD) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("basmala_tool_panel")
    ) {
        PanelHeader(title = "Add Basmala (بِسْمِ اللَّهِ)", onClose = onClose)

        Spacer(modifier = Modifier.height(14.dp))

        // Calligraphic Live Preview
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF04241B)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, InShotYellow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝",
                    fontFamily = AmiriFontFamily,
                    fontSize = 24.sp,
                    color = Color(selectedStyle.textColorHex),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Style selector
        Text(text = "CALLIGRAPHY STYLES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val styles = listOf(
                CaptionStyle.ARABIC_GOLD,
                CaptionStyle.ARABIC_EMERALD,
                CaptionStyle.CLEAN_WHITE,
                CaptionStyle.GOLD_OUTLINE
            )
            items(styles) { style ->
                val isSelected = selectedStyle == style
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) InShotYellow.copy(alpha = 0.2f) else DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotYellow else DarkBorder),
                    modifier = Modifier.clickable { selectedStyle = style }
                ) {
                    Text(
                        text = style.displayName,
                        color = if (isSelected) InShotYellow else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onAddBasmala(selectedStyle)
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = InShotYellow),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("add_basmala_btn")
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Basmala to Video (ٹائم لائن پر شامل کریں)", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdjustToolPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    vignette: Float,
    onAdjustmentsChanged: (Float, Float, Float, Float) -> Unit,
    onClose: () -> Unit
) {
    var b by remember(brightness) { mutableStateOf(brightness) }
    var c by remember(contrast) { mutableStateOf(contrast) }
    var s by remember(saturation) { mutableStateOf(saturation) }
    var v by remember(vignette) { mutableStateOf(vignette) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("adjust_tool_panel")
    ) {
        PanelHeader(title = "Video Adjustments", onClose = onClose)

        Spacer(modifier = Modifier.height(10.dp))

        // Monochrome Black/White/Gray Sliders
        AdjustSliderItem(label = "Brightness", value = b, range = -0.5f..0.5f, valueLabel = "${(b * 100).toInt()}%") {
            b = it
            onAdjustmentsChanged(b, c, s, v)
        }

        AdjustSliderItem(label = "Contrast", value = c, range = 0.5f..1.8f, valueLabel = "${((c - 1f) * 100).toInt()}%") {
            c = it
            onAdjustmentsChanged(b, c, s, v)
        }

        AdjustSliderItem(label = "Saturation", value = s, range = 0.0f..2.0f, valueLabel = "${((s - 1f) * 100).toInt()}%") {
            s = it
            onAdjustmentsChanged(b, c, s, v)
        }

        AdjustSliderItem(label = "Vignette", value = v, range = 0.0f..1.0f, valueLabel = "${(v * 100).toInt()}%") {
            v = it
            onAdjustmentsChanged(b, c, s, v)
        }
    }
}

@Composable
private fun AdjustSliderItem(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = valueLabel, color = Color(0xFFCCCCCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0xFF424242)
            ),
            modifier = Modifier.fillMaxWidth().height(28.dp)
        )
    }
}

@Composable
fun VoiceRecordToolPanel(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClose: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("voice_tool_panel"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PanelHeader(title = "Voice Record (تلاوت ریکارڈ کریں)", onClose = onClose)

        Spacer(modifier = Modifier.height(18.dp))

        // Big Mic Record Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(if (isRecording) InShotRed else Color(0xFF004D40))
                .clickable {
                    if (isRecording) onStopRecording() else onStartRecording()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Stop" else "Record",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isRecording) "● Recording in progress..." else "Tap to Record Recitation",
            color = if (isRecording) InShotRed else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Record your Quran recitation or commentary directly over video",
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ImagePipToolPanel(
    onAddImage: (String?) -> Unit,
    onClose: () -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddImage(uri.toString())
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp)
            .testTag("image_pip_tool_panel")
    ) {
        PanelHeader(title = "Add Image / Overlay (تصویر شامل کریں)", onClose = onClose)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pick from Gallery
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = InShotYellow, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select from Gallery", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Preset App Logo Overlay
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onAddImage(null)
                        onClose()
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Verified, null, tint = InShotCyan, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Quran Editor Logo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
