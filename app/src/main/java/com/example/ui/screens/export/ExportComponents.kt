package com.example.ui.screens.export

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.viewmodel.ExportProgressState

@Composable
fun ExportSettingsDialog(
    estimatedDurationSec: Float,
    onDismiss: () -> Unit,
    onStartExport: (String, Int) -> Unit
) {
    var selectedResolution by remember { mutableStateOf("1080p (Full HD)") }
    var selectedFps by remember { mutableStateOf(30) }

    val resolutions = listOf("720p", "1080p (Full HD)", "2K", "4K (Ultra HD)")
    val fpsOptions = listOf(24, 30, 60)

    val estimatedSizeMb = remember(selectedResolution, selectedFps, estimatedDurationSec) {
        val multiplier = when (selectedResolution) {
            "4K (Ultra HD)" -> 4.0f
            "2K" -> 2.4f
            "1080p (Full HD)" -> 1.4f
            else -> 0.8f
        } * (selectedFps / 30f)
        (estimatedDurationSec * multiplier * 10).toInt() / 10f
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth().testTag("export_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Save & Export",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Resolution
                Text("RESOLUTION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    resolutions.forEach { res ->
                        val isSelected = selectedResolution == res
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotRed else DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedResolution = res }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res,
                                    color = if (isSelected) InShotRed else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = InShotRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Frame Rate
                Text("FRAME RATE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fpsOptions.forEach { fps ->
                        val isSelected = selectedFps == fps
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) InShotRed else DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFps = fps }
                        ) {
                            Text(
                                text = "${fps} FPS",
                                color = if (isSelected) InShotRed else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Estimated File Size Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Size:", color = TextSecondary, fontSize = 13.sp)
                    Text("~$estimatedSizeMb MB", color = InShotCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onStartExport(selectedResolution, selectedFps) },
                    colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_export_btn")
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE VIDEO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExportProgressDialog(
    exportState: ExportProgressState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "export_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Dialog(
        onDismissRequest = { if (exportState.isCompleted) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = exportState.isCompleted, dismissOnClickOutside = exportState.isCompleted)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth().testTag("export_progress_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!exportState.isCompleted) {
                    // Exporting State
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { exportState.progress },
                            modifier = Modifier.size(100.dp),
                            color = InShotRed,
                            trackColor = DarkSurfaceVariant,
                            strokeWidth = 8.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(exportState.progress * 100).toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Saving Video...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Frame ${exportState.currentFrame} / ${exportState.totalFrames}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please do not lock screen or switch apps",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    // Completed State
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(InShotGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = InShotGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Saved Successfully!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${exportState.resolution} • ${exportState.fps} FPS • ${exportState.estimatedSizeMb} MB",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Social Share Row
                    Text("SHARE TO", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ShareOptionItem(title = "Reels", icon = "📸", color = Color(0xFFE1306C)) {
                            shareText(context, "Check out my new video edited with InShot!")
                        }
                        ShareOptionItem(title = "TikTok", icon = "🎵", color = Color(0xFF000000)) {
                            shareText(context, "Check out my new video edited with InShot!")
                        }
                        ShareOptionItem(title = "YouTube", icon = "▶️", color = Color(0xFFFF0000)) {
                            shareText(context, "Check out my new video edited with InShot!")
                        }
                        ShareOptionItem(title = "WhatsApp", icon = "💬", color = Color(0xFF25D366)) {
                            shareText(context, "Check out my new video edited with InShot!")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("export_done_btn")
                    ) {
                        Text("DONE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ShareOptionItem(title: String, icon: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = DarkSurfaceVariant,
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

private fun shareText(context: android.content.Context, message: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Video via InShot")
    context.startActivity(shareIntent)
}
