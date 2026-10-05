package com.example.ui.screens.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.TextOverlay
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MultiTrackTimeline(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipIndex: Int,
    selectedTextOverlayId: String?,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectClip: (Int) -> Unit,
    onSelectTextOverlay: (String?) -> Unit,
    onTrimTextOverlay: (String, Long, Long) -> Unit,
    onDeleteTextOverlay: (String) -> Unit,
    onSplitClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onAddClip: () -> Unit,
    onTrimClip: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = project.totalDurationMs.coerceAtLeast(1000L)

    val playheadFormatted = remember(currentPlayheadMs) {
        val totalSeconds = currentPlayheadMs / 1000f
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds.toInt())
    }

    val totalFormatted = remember(totalDurationMs) {
        val totalSeconds = totalDurationMs / 1000f
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds.toInt())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkTimelineTrack)
    ) {
        // 1. InShot Top Actions Row (Play/Pause, Timecode, Split, Delete, Add)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Button with timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(InShotYellow)
                        .testTag("timeline_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$playheadFormatted / $totalFormatted",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Quick Tools
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onSplitClip,
                    modifier = Modifier.size(32.dp).testTag("timeline_split_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Split",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (selectedTextOverlayId != null) {
                            onDeleteTextOverlay(selectedTextOverlayId)
                        } else {
                            onDeleteClip()
                        }
                    },
                    modifier = Modifier.size(32.dp).testTag("timeline_delete_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onAddClip,
                    modifier = Modifier.size(32.dp).testTag("timeline_add_clip_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Clip",
                        tint = InShotYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Interactive Multi-Track Timeline Canvas with Vertical Layer Scrolling (210dp height)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Color(0xFF121417))
                .pointerInput(totalDurationMs) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val widthPx = size.width
                        if (widthPx > 0 && totalDurationMs > 0) {
                            val deltaMs = (dragAmount.x / widthPx * totalDurationMs).toLong()
                            onSeekTo((currentPlayheadMs + deltaMs).coerceIn(0L, totalDurationMs))
                        }
                    }
                }
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current
            val totalWidthDp = with(density) { totalWidthPx.toDp() }
            val trackContentWidthDp = (totalWidthDp - 64.dp).coerceAtLeast(100.dp)

            val playheadRatio = if (totalDurationMs > 0) {
                currentPlayheadMs.toFloat() / totalDurationMs
            } else 0f
            val playheadXDp = 54.dp + (trackContentWidthDp * playheadRatio)

            val verticalScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            ) {
                // Top Time Ruler Bar
                Row(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                    Spacer(modifier = Modifier.width(54.dp))
                    RulerBar(totalDurationMs = totalDurationMs, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(2.dp))

                // VERTICALLY SCROLLABLE TIMELINE LAYERS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(verticalScrollState),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // LAYER 1: VIDEO TRACK
                    TimelineTrackRow(label = "Video", icon = Icons.Default.Movie) {
                        InShotVideoFilmstripTrack(
                            project = project,
                            selectedClipIndex = selectedClipIndex,
                            totalDurationMs = totalDurationMs,
                            onSelectClip = onSelectClip
                        )
                    }

                    // LAYER 2: QURAN AYAH TRACKS (If any added)
                    val quranOverlays = project.textOverlays.filter { it.isQuranAyah }
                    if (quranOverlays.isNotEmpty()) {
                        TimelineTrackRow(label = "Quran", icon = Icons.Default.MenuBook) {
                            InShotTextElementsTrack(
                                textOverlays = quranOverlays,
                                selectedId = selectedTextOverlayId,
                                totalDurationMs = totalDurationMs,
                                totalWidthDp = trackContentWidthDp,
                                cardColor = Color(0xFF004D40), // Emerald
                                onSelect = onSelectTextOverlay,
                                onTrim = onTrimTextOverlay
                            )
                        }
                    }

                    // LAYER 3: BASMALA TRACK (If any added)
                    val basmalaOverlays = project.textOverlays.filter { it.isBasmala }
                    if (basmalaOverlays.isNotEmpty()) {
                        TimelineTrackRow(label = "Basmala", icon = Icons.Default.AutoAwesome) {
                            InShotTextElementsTrack(
                                textOverlays = basmalaOverlays,
                                selectedId = selectedTextOverlayId,
                                totalDurationMs = totalDurationMs,
                                totalWidthDp = trackContentWidthDp,
                                cardColor = Color(0xFF5D4037), // Heritage Bronze
                                onSelect = onSelectTextOverlay,
                                onTrim = onTrimTextOverlay
                            )
                        }
                    }

                    // LAYER 4: GENERAL TEXT / SUBTITLE TRACKS
                    val generalTextOverlays = project.textOverlays.filter { !it.isQuranAyah && !it.isBasmala }
                    TimelineTrackRow(label = "Text", icon = Icons.Default.Title) {
                        InShotTextElementsTrack(
                            textOverlays = generalTextOverlays,
                            selectedId = selectedTextOverlayId,
                            totalDurationMs = totalDurationMs,
                            totalWidthDp = trackContentWidthDp,
                            cardColor = Color(0xFF7CB342), // InShot signature Element Green
                            placeholderText = if (project.textOverlays.isEmpty()) "Tap Text or Quran to add caption layer" else "Text layer",
                            onSelect = onSelectTextOverlay,
                            onTrim = onTrimTextOverlay
                        )
                    }

                    // LAYER 5: AUDIO TRACKS
                    TimelineTrackRow(label = "Audio", icon = Icons.Default.MusicNote) {
                        InShotAudioTrack(
                            audioTracks = project.audioTracks,
                            totalDurationMs = totalDurationMs
                        )
                    }

                    // LAYER 6: IMAGE / PIP TRACKS
                    if (project.imageOverlays.isNotEmpty()) {
                        TimelineTrackRow(label = "Image", icon = Icons.Default.Image) {
                            InShotImageTrack(
                                imageOverlays = project.imageOverlays,
                                totalDurationMs = totalDurationMs,
                                totalWidthDp = trackContentWidthDp
                            )
                        }
                    }

                    // LAYER 7: STICKER TRACKS
                    if (project.stickerOverlays.isNotEmpty()) {
                        TimelineTrackRow(label = "Sticker", icon = Icons.Default.EmojiEmotions) {
                            InShotStickerTrack(
                                stickerOverlays = project.stickerOverlays,
                                totalDurationMs = totalDurationMs,
                                totalWidthDp = trackContentWidthDp
                            )
                        }
                    }
                }
            }

            // InShot Signature White Playhead Needle (Runs down entire height of timeline)
            Box(
                modifier = Modifier
                    .offset(x = (playheadXDp - 1.dp).coerceAtLeast(0.dp))
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )

            // Top Playhead Knob
            Box(
                modifier = Modifier
                    .offset(x = (playheadXDp - 5.dp).coerceAtLeast(0.dp), y = 0.dp)
                    .size(10.dp)
                    .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                    .background(Color.White)
            )
        }

        // 3. Bottom Time Display (InShot style: 00:00 start, current centered, total end)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "00:00", color = TextMuted, fontSize = 10.sp)
            Text(
                text = playheadFormatted,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(text = totalFormatted, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TimelineTrackRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Track Header Tag
        Row(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E2126))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = InShotYellow,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = Color(0xFFB0BEC5),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Track Content Area
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            content()
        }
    }
}

@Composable
fun RulerBar(totalDurationMs: Long, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalSec = (totalDurationMs / 1000).toInt().coerceAtLeast(1)
        val step = (totalSec / 4).coerceAtLeast(1)
        for (sec in 0..totalSec step step) {
            val min = sec / 60
            val s = sec % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", min, s),
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InShotTextElementsTrack(
    textOverlays: List<TextOverlay>,
    selectedId: String?,
    totalDurationMs: Long,
    totalWidthDp: androidx.compose.ui.unit.Dp,
    cardColor: Color = Color(0xFF7CB342),
    placeholderText: String = "No text overlays",
    onSelect: (String?) -> Unit,
    onTrim: (String, Long, Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1A1D21))
    ) {
        if (textOverlays.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = placeholderText, color = TextMuted, fontSize = 9.sp)
            }
        } else {
            textOverlays.forEach { textOverlay ->
                val isSelected = selectedId == textOverlay.id
                val startRatio = (textOverlay.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                val durationRatio = (textOverlay.durationMs.toFloat() / totalDurationMs).coerceIn(0.08f, 1f - startRatio)

                val startXDp = totalWidthDp * startRatio
                val blockWidthDp = (totalWidthDp * durationRatio).coerceAtLeast(50.dp)

                Row(
                    modifier = Modifier
                        .offset(x = startXDp)
                        .width(blockWidthDp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(cardColor)
                        .border(
                            border = if (isSelected) BorderStroke(1.5.dp, Color.White) else BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .clickable { onSelect(textOverlay.id) }
                        .testTag("timeline_text_block_${textOverlay.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = (dragAmount.x * 20).toLong()
                                        val newStart = (textOverlay.startOffsetMs + deltaMs).coerceIn(
                                            0L,
                                            textOverlay.startOffsetMs + textOverlay.durationMs - 400L
                                        )
                                        val newDuration = textOverlay.durationMs - (newStart - textOverlay.startOffsetMs)
                                        onTrim(textOverlay.id, newStart, newDuration)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Trim Left", tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }

                    Text(
                        text = textOverlay.text.replace("\n", " "),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                        textAlign = TextAlign.Center
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = (dragAmount.x * 20).toLong()
                                        val newDuration = (textOverlay.durationMs + deltaMs).coerceAtLeast(400L)
                                        onTrim(textOverlay.id, textOverlay.startOffsetMs, newDuration)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, "Trim Right", tint = Color.Black, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InShotVideoFilmstripTrack(
    project: Project,
    selectedClipIndex: Int,
    totalDurationMs: Long,
    onSelectClip: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        project.clips.forEachIndexed { index, clip ->
            val clipDuration = clip.trimmedDurationMs
            val weight = (clipDuration.toFloat() / totalDurationMs).coerceAtLeast(0.08f)
            val isSelected = index == selectedClipIndex

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF263238)) // Neutral slate
                    .border(
                        border = if (isSelected) BorderStroke(1.5.dp, InShotYellow) else BorderStroke(0.5.dp, Color(0xFF37474F)),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelectClip(index) }
                    .testTag("timeline_clip_$index")
            ) {
                // Filmstrip division lines
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) {
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(Color.White.copy(alpha = 0.15f)))
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = clip.sceneIcon, fontSize = 11.sp)
                    Text(
                        text = clip.title,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun InShotAudioTrack(
    audioTracks: List<com.example.model.AudioTrack>,
    totalDurationMs: Long
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF17262B))
    ) {
        if (audioTracks.isEmpty()) {
            Text(
                text = "Audio track",
                color = TextMuted,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            )
        } else {
            val audio = audioTracks.first()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.MusicNote, null, tint = InShotCyan, modifier = Modifier.size(12.dp))
                Text(
                    text = "${audio.title} - ${audio.artist}",
                    color = InShotCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun InShotImageTrack(
    imageOverlays: List<com.example.model.ImageOverlay>,
    totalDurationMs: Long,
    totalWidthDp: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E222B))
    ) {
        imageOverlays.forEach { img ->
            val startRatio = (img.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val durationRatio = (img.durationMs.toFloat() / totalDurationMs).coerceIn(0.08f, 1f - startRatio)
            val startXDp = totalWidthDp * startRatio
            val blockWidthDp = (totalWidthDp * durationRatio).coerceAtLeast(40.dp)

            Box(
                modifier = Modifier
                    .offset(x = startXDp)
                    .width(blockWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF3F51B5))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = img.title, color = Color.White, fontSize = 9.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun InShotStickerTrack(
    stickerOverlays: List<com.example.model.StickerOverlay>,
    totalDurationMs: Long,
    totalWidthDp: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF261D2B))
    ) {
        stickerOverlays.forEach { stk ->
            val startRatio = (stk.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val durationRatio = (stk.durationMs.toFloat() / totalDurationMs).coerceIn(0.08f, 1f - startRatio)
            val startXDp = totalWidthDp * startRatio
            val blockWidthDp = (totalWidthDp * durationRatio).coerceAtLeast(36.dp)

            Box(
                modifier = Modifier
                    .offset(x = startXDp)
                    .width(blockWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF8E24AA)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stk.emojiOrIcon, fontSize = 12.sp)
            }
        }
    }
}
