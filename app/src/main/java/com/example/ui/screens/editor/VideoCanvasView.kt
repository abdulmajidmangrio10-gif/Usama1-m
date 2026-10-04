package com.example.ui.screens.editor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VideoCanvasView(
    project: Project,
    currentPlayheadMs: Long,
    selectedClip: VideoClip?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var showWatermark by remember { mutableStateOf(true) }
    var canvasScale by remember { mutableStateOf(1.0f) }

    // Dynamic wave animation when video is playing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val activeClip = remember(project.clips, currentPlayheadMs) {
        var accumulated = 0L
        for (clip in project.clips) {
            val clipDur = clip.trimmedDurationMs
            if (currentPlayheadMs >= accumulated && currentPlayheadMs <= accumulated + clipDur) {
                return@remember clip
            }
            accumulated += clipDur
        }
        selectedClip ?: project.clips.firstOrNull()
    }

    val colorFilter = remember(activeClip?.filter, activeClip?.brightness, activeClip?.contrast) {
        val matrix = when (activeClip?.filter) {
            FilterType.BLACK_WHITE -> ColorMatrix().apply { setToSaturation(0f) }
            FilterType.SEPIA -> ColorMatrix().apply { setToSaturation(0.2f) }
            FilterType.VIVID -> ColorMatrix().apply { setToSaturation(1.8f) }
            FilterType.CYBERPUNK -> ColorMatrix(floatArrayOf(
                1.2f, 0.1f, 0.4f, 0f, 20f,
                0.0f, 1.0f, 0.6f, 0f, 10f,
                0.4f, 0.2f, 1.5f, 0f, 30f,
                0f, 0f, 0f, 1f, 0f
            ))
            FilterType.CINEMATIC -> ColorMatrix(floatArrayOf(
                1.2f, 0f, 0f, 0f, 25f,
                0f, 1.05f, 0f, 0f, 10f,
                0f, 0f, 0.85f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            ))
            FilterType.WARM -> ColorMatrix(floatArrayOf(
                1.25f, 0f, 0f, 0f, 20f,
                0f, 1.15f, 0f, 0f, 15f,
                0f, 0f, 0.8f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            ))
            FilterType.COOL -> ColorMatrix(floatArrayOf(
                0.85f, 0f, 0f, 0f, -10f,
                0f, 1.05f, 0f, 0f, 5f,
                0f, 0f, 1.35f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            ))
            FilterType.GLITCH -> ColorMatrix(floatArrayOf(
                1.4f, 0.2f, 0f, 0f, 30f,
                0f, 0.9f, 0.1f, 0f, -5f,
                0.2f, 0.1f, 1.3f, 0f, 35f,
                0f, 0f, 0f, 1f, 0f
            ))
            else -> ColorMatrix()
        }
        ColorFilter.colorMatrix(matrix)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .testTag("video_canvas_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Container maintaining selected Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxHeight(0.94f)
                .aspectRatio(project.canvasRatio.aspectRatio, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        canvasScale = (canvasScale * zoom).coerceIn(0.5f, 2.5f)
                    }
                }
        ) {
            // 1. Background layer (Blur / Gradient / Solid Color)
            when (project.bgType) {
                CanvasBgType.BLUR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(activeClip?.themeGradientStart ?: 0xFF333333).copy(alpha = 0.7f),
                                        Color(activeClip?.themeGradientEnd ?: 0xFF111111).copy(alpha = 0.95f),
                                        Color.Black
                                    )
                                )
                            )
                    )
                }
                CanvasBgType.GRADIENT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(project.bgColorHex),
                                        Color(0xFF1E202A)
                                    )
                                )
                            )
                    )
                }
                CanvasBgType.SOLID_COLOR -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(project.bgColorHex))
                    )
                }
            }

            // 2. Video Video Content Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = (if (activeClip?.isFlippedH == true) -1f else 1f) * canvasScale * (if (isPlaying) pulseScale else 1f)
                        scaleY = (if (activeClip?.isFlippedV == true) -1f else 1f) * canvasScale * (if (isPlaying) pulseScale else 1f)
                        rotationZ = activeClip?.rotationAngle ?: 0f
                    }
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(activeClip?.themeGradientStart ?: 0xFFFF3366),
                                Color(activeClip?.themeGradientEnd ?: 0xFFFF9900)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Video Clip Scene Content Graphic
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = activeClip?.sceneIcon ?: "🎬",
                        fontSize = 54.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = activeClip?.title ?: "Clip",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    if (activeClip?.filter != FilterType.ORIGINAL) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Filter: ${activeClip?.filter?.displayName}",
                                color = InShotYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Vignette Shader Edge if set
                val vignetteAlpha = activeClip?.vignette ?: 0f
                if (vignetteAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = vignetteAlpha)
                                    )
                                )
                            )
                    )
                }
            }

            // 3. Text Overlays (Active at current playhead)
            project.textOverlays.forEach { textOverlay ->
                val isActive = currentPlayheadMs >= textOverlay.startOffsetMs &&
                        currentPlayheadMs <= (textOverlay.startOffsetMs + textOverlay.durationMs)
                if (isActive) {
                    val isArabicText = textOverlay.text.any { c -> c in '\u0600'..'\u06FF' || c in '\u0750'..'\u077F' }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = when {
                            textOverlay.posY < 0.35f -> Alignment.TopCenter
                            textOverlay.posY > 0.65f -> Alignment.BottomCenter
                            else -> Alignment.Center
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(textOverlay.bgColorHex),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = textOverlay.text,
                                color = Color(textOverlay.textColorHex),
                                fontSize = if (isArabicText) (textOverlay.fontSizeSp * 1.15f).sp else textOverlay.fontSizeSp.sp,
                                fontFamily = if (isArabicText) AmiriFontFamily else androidx.compose.ui.text.font.FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = if (isArabicText) 8.dp else 6.dp)
                            )
                        }
                    }
                }
            }

            // 4. Sticker Overlays (Active at current playhead)
            project.stickerOverlays.forEach { sticker ->
                val isActive = currentPlayheadMs >= sticker.startOffsetMs &&
                        currentPlayheadMs <= (sticker.startOffsetMs + sticker.durationMs)
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = when {
                            sticker.posX > 0.7f && sticker.posY < 0.3f -> Alignment.TopEnd
                            sticker.posX < 0.3f && sticker.posY < 0.3f -> Alignment.TopStart
                            sticker.posY > 0.7f -> Alignment.BottomCenter
                            else -> Alignment.Center
                        }
                    ) {
                        Text(
                            text = sticker.emojiOrIcon,
                            fontSize = (42 * sticker.scale).sp,
                            modifier = Modifier.rotate(sticker.rotation)
                        )
                    }
                }
            }

            // 5. InShot Watermark Badge (removable by user)
            if (showWatermark) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "InShot",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Watermark",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { showWatermark = false }
                        )
                    }
                }
            }

            // 6. Aspect Ratio Indicator Tag
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = project.canvasRatio.label,
                    color = InShotCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
