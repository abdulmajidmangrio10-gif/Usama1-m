package com.example.ui.screens.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.VideoClip
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MultiTrackTimeline(
    project: Project,
    currentPlayheadMs: Long,
    selectedClipIndex: Int,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectClip: (Int) -> Unit,
    onSplitClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onAddClip: () -> Unit,
    onTrimClip: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDurationMs = project.totalDurationMs
    val playheadFormatted = remember(currentPlayheadMs) {
        val totalSeconds = currentPlayheadMs / 1000f
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        String.format(Locale.getDefault(), "%02d:%04.1f", minutes, seconds)
    }

    val totalFormatted = remember(totalDurationMs) {
        val totalSeconds = totalDurationMs / 1000f
        val minutes = (totalSeconds / 60).toInt()
        val seconds = totalSeconds % 60
        String.format(Locale.getDefault(), "%02d:%04.1f", minutes, seconds)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkTimelineTrack)
    ) {
        // 1. Timeline Controls Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause + Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(36.dp)
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = playheadFormatted,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = " / $totalFormatted",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Actions: Split, Delete, Add
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onSplitClip,
                    modifier = Modifier.testTag("timeline_split_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = "Split",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClip,
                    enabled = project.clips.size > 1,
                    modifier = Modifier.testTag("timeline_delete_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (project.clips.size > 1) Color.White else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onAddClip,
                    modifier = Modifier.testTag("timeline_add_clip_quick_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Clip",
                        tint = InShotCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Interactive Multi-Track Canvas & Scrubber
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(DarkBackground)
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
            val playheadRatio = if (totalDurationMs > 0) {
                currentPlayheadMs.toFloat() / totalDurationMs
            } else 0f
            val playheadX = (playheadRatio * totalWidthPx).coerceIn(0f, totalWidthPx)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                // Time Ruler Bar
                RulerBar(totalDurationMs = totalDurationMs)

                Spacer(modifier = Modifier.height(4.dp))

                // Track 1: Text & Sticker Overlays Strip
                OverlaysTrackStrip(
                    project = project,
                    totalDurationMs = totalDurationMs
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Track 2: Audio & Music Strip
                AudioTrackStrip(
                    project = project,
                    totalDurationMs = totalDurationMs
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Track 3: Video Clips Strip with Trim Selection
                VideoClipsTrackStrip(
                    project = project,
                    selectedClipIndex = selectedClipIndex,
                    totalDurationMs = totalDurationMs,
                    onSelectClip = onSelectClip
                )
            }

            // Red Playhead Needle Overlay
            Box(
                modifier = Modifier
                    .offset(x = (playheadX - 6f).dp.coerceAtLeast(0.dp))
                    .width(12.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Top playhead marker knob
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(InShotRed)
                )
                // Vertical needle line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(InShotRed)
                )
            }
        }
    }
}

@Composable
fun RulerBar(totalDurationMs: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalSec = (totalDurationMs / 1000).toInt().coerceAtLeast(1)
        val step = (totalSec / 4).coerceAtLeast(1)
        for (sec in 0..totalSec step step) {
            Text(
                text = "${sec}s",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OverlaysTrackStrip(project: Project, totalDurationMs: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
    ) {
        // Text overlays
        project.textOverlays.forEach { textOverlay ->
            val startWeight = (textOverlay.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val durWeight = (textOverlay.durationMs.toFloat() / totalDurationMs).coerceIn(0.05f, 1f - startWeight)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (startWeight + durWeight).coerceAtMost(1f))
                    .padding(start = (startWeight * 280).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (textOverlay.isAutoCaption) InShotCyan.copy(alpha = 0.85f) else InShotYellow.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (textOverlay.isAutoCaption) "CC: ${textOverlay.text}" else "T: ${textOverlay.text}",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }

        // Sticker overlays
        project.stickerOverlays.forEach { sticker ->
            val startWeight = (sticker.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val durWeight = (sticker.durationMs.toFloat() / totalDurationMs).coerceIn(0.05f, 1f - startWeight)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (startWeight + durWeight).coerceAtMost(1f))
                    .padding(start = (startWeight * 280).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(InShotPurple.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = sticker.emojiOrIcon,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun AudioTrackStrip(project: Project, totalDurationMs: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.4f))
    ) {
        val audio = project.audioTracks.firstOrNull()
        if (audio != null) {
            val startRatio = (audio.startOffsetMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
            val durRatio = (audio.durationMs.toFloat() / totalDurationMs).coerceIn(0.1f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (startRatio + durRatio).coerceAtMost(1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(InShotCyan.copy(alpha = 0.25f))
                    .border(1.dp, InShotCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = InShotCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${audio.title} - ${audio.artist}",
                    color = InShotCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                // Simulated waveform bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(8) { idx ->
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((6 + (idx % 4) * 3).dp)
                                .background(InShotCyan.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "No audio track (Tap Music to add)",
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun VideoClipsTrackStrip(
    project: Project,
    selectedClipIndex: Int,
    totalDurationMs: Long,
    onSelectClip: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
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
                        width = if (isSelected) 2.5.dp else 0.dp,
                        color = if (isSelected) TimelineSelectionHandle else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectClip(index) }
                    .testTag("timeline_clip_$index")
            ) {
                // InShot yellow trim handles if selected
                if (isSelected) {
                    // Left Trim Handle (<|)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(TimelineSelectionHandle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("<", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Right Trim Handle (|>)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(TimelineSelectionHandle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(">", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Clip content info
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isSelected) 12.dp else 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = clip.sceneIcon, fontSize = 12.sp)
                        Text(
                            text = clip.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${clipDuration / 1000f}s",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                color = InShotYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
