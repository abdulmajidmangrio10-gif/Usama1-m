package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CanvasRatio
import com.example.ui.theme.*

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, CanvasRatio, String) -> Unit,
    onPickDeviceMedia: () -> Unit
) {
    var title by remember { mutableStateOf("New Story") }
    var selectedRatio by remember { mutableStateOf(CanvasRatio.RATIO_9_16) }
    var selectedTemplate by remember { mutableStateOf("Reel / Short") }

    val templates = listOf(
        "Reel / Short" to "🔥",
        "Travel Vlog" to "✈️",
        "Fitness / Energy" to "⚡",
        "Blank Canvas" to "🎬"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
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
                        text = "New Project",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Project Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = InShotRed,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = InShotRed,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Aspect Ratio Selector
                Text(
                    text = "CANVAS RATIO",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CanvasRatio.values().take(4).forEach { ratio ->
                        val isSelected = selectedRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) InShotRed.copy(alpha = 0.15f) else DarkSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) InShotRed else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = ratio.label,
                                    color = if (isSelected) InShotRed else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = when (ratio) {
                                        CanvasRatio.RATIO_9_16 -> "Reels"
                                        CanvasRatio.RATIO_1_1 -> "Insta"
                                        CanvasRatio.RATIO_16_9 -> "YT"
                                        CanvasRatio.RATIO_4_5 -> "Feed"
                                        else -> ""
                                    },
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Presets & Templates
                Text(
                    text = "PRESET TEMPLATE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { (tmpl, emoji) ->
                        val isSelected = selectedTemplate == tmpl
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) InShotRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) InShotRed else DarkBorder
                            ),
                            modifier = Modifier.clickable { selectedTemplate = tmpl }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                                Text(
                                    text = tmpl,
                                    color = if (isSelected) InShotRed else Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onCreate(title.ifBlank { "New Project" }, selectedRatio, selectedTemplate)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MovieFilter, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Project", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onPickDeviceMedia,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, tint = InShotCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select From Gallery", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
