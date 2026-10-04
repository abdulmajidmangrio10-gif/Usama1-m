package com.example.ui.screens.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
        String.format(Locale.getDefault(), "%d:%04.1f", minutes, seconds)
    }

    val totalFormatted = remember(totalDurationMs) {
        val totalSeconds = totalDurationMs / 1000f
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        String.format(Locale.getDefault(), "%d:%04.1f", minutes, seconds)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkTimelineTrack)
    ) {
        // 1. InShot Top Actions Row (Undo, Redo, Play/Pause, Split, Delete, Add)
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
                        .background(InShotRed)
                        .testTag("timeline_play_pause_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
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

            // Quick Tool Icons
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
                        tint = InShotCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Interactive Timeline Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFF141416))
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

            val playheadRatio = if (totalDurationMs > 0) {
                currentPlayheadMs.toFloat() / totalDurationMs
            } else 0f
            val playheadXDp = totalWidthDp * playheadRatio

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Time Ruler Bar
                RulerBar(totalDurationMs = totalDurationMs)

                Spacer(modifier = Modifier.height(4.dp))

                // TRACK 1: InShot Green Text / Element Track (Circled by user!)
                InShotTextElementsTrack(
                    textOverlays = project.textOverlays,
                    selectedId = selectedTextOverlayId,
                    totalDurationMs = totalDurationMs,
                    totalWidthDp = totalWidthDp - 16.dp,
                    onSelect = onSelectTextOverlay,
                    onTrim = onTrimTextOverlay
                )

                Spacer(modifier = Modifier.height(6.dp))

                // TRACK 2: InShot Video Clips Filmstrip Track
                InShotVideoFilmstripTrack(
                    project = project,
                    selectedClipIndex = selectedClipIndex,
                    totalDurationMs = totalDurationMs,
                    onSelectClip = onSelectClip
                )
            }

            // InShot Signature White Playhead Needle line
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

        // 3. Bottom Time Display (Matches user's InShot screenshot: 0:00.0 centered, 0:27.4 on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "00:00",
                color = TextMuted,
                fontSize = 10.sp
            )
            Text(
                text = playheadFormatted,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = totalFormatted,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun RulerBar(totalDurationMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp),
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

/**
 * InShot Text / Elements Track:
 * This renders the exact green pill with white [ < ] and [ > ] handles
 * that the user pointed to in the attached InShot screenshot.
 */
@Composable
fun InShotTextElementsTrack(
    textOverlays: List<TextOverlay>,
    selectedId: String?,
    totalDurationMs: Long,
    totalWidthDp: androidx.compose.ui.unit.Dp,
    onSelect: (String?) -> Unit,
    onTrim: (String, Long, Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF222428))
    ) {
        if (textOverlays.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Text & Quran Ayahs will appear here",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        } else {
            textOverlays.forEach { textOverlay ->
                val isSelected = selectedId == textOverlay.id
                val startRatio = (textOverlay.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
                val durationRatio = (textOverlay.durationMs.toFloat() / totalDurationMs).coerceIn(0.08f, 1f - startRatio)

                val startXDp = totalWidthDp * startRatio
                val blockWidthDp = (totalWidthDp * durationRatio).coerceAtLeast(54.dp)

                // InShot Green Capsule with white handles
                Row(
                    modifier = Modifier
                        .offset(x = startXDp)
                        .width(blockWidthDp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF7CB342)) // InShot signature Element Green
                        .border(
                            border = if (isSelected) BorderStroke(1.5.dp, Color.White) else BorderStroke(1.dp, Color(0xFF558B2F)),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelect(textOverlay.id) }
                        .testTag("timeline_text_block_${textOverlay.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Trim Handle: [ < ]
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = (dragAmount.x * 20).toLong()
                                        val newStart = (textOverlay.startOffsetMs + deltaMs).coerceIn(
                                            0L,
                                            textOverlay.startOffsetMs + textOverlay.durationMs - 500L
                                        )
                                        val newDuration = textOverlay.durationMs - (newStart - textOverlay.startOffsetMs)
                                        onTrim(textOverlay.id, newStart, newDuration)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Trim Left",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Middle Label: Text / Ayah Name
                    Text(
                        text = textOverlay.text.replace("\n", " "),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    // Right Trim Handle: [ > ]
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .pointerInput(textOverlay.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val deltaMs = (dragAmount.x * 20).toLong()
                                        val newDuration = (textOverlay.durationMs + deltaMs).coerceAtLeast(500L)
                                        onTrim(textOverlay.id, textOverlay.startOffsetMs, newDuration)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Trim Right",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * InShot Video Filmstrip Track:
 * Displays video clips with realistic filmstrip frames and timestamps (00:00, 00:02, 00:04).
 */
@Composable
fun InShotVideoFilmstripTrack(
    project: Project,
    selectedClipIndex: Int,
    totalDurationMs: Long,
    onSelectClip: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        project.clips.forEachIndexed { index, clip ->
            val clipDuration = clip.trimmedDurationMs
            val weight = (clipDuration.toFloat() / totalDurationMs).coerceAtLeast(0.08f)
            val isSelected = index == selectedClipIndex

            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(clip.themeGradientStart),
                                Color(clip.themeGradientEnd)
                            )
                        )
                    )
                    .border(
                        border = if (isSelected) BorderStroke(2.dp, TimelineSelectionHandle) else BorderStroke(0.5.dp, DarkBorder),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectClip(index) }
                    .testTag("timeline_clip_$index")
            ) {
                // Filmstrip Frame Dividers (Simulates video thumbnails like InShot)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.6f)
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }
                }

                // Scene Title & Emoji
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = clip.sceneIcon, fontSize = 14.sp)
                    Text(
                        text = clip.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // InShot Yellow Trim Handles if selected
                if (isSelected) {
                    // Left Trim Handle (<|)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(12.dp)
                            .fillMaxHeight()
                            .background(TimelineSelectionHandle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Right Trim Handle (|>)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(12.dp)
                            .fillMaxHeight()
                            .background(TimelineSelectionHandle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
