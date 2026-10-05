package com.example.ui.screens.home

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Project
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    projects: List<Project>,
    onOpenProject: (Long) -> Unit,
    onCreateProjectWithMedia: (String?, String, Long, (Long) -> Unit) -> Unit,
    onDuplicateProject: (Project) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    // Video Picker: Opens phone gallery directly
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val (durationMs, title) = extractVideoInfo(context, uri)
            onCreateProjectWithMedia(uri.toString(), title, durationMs) { newId ->
                onOpenProject(newId)
            }
        }
    }

    // Photo/Image Picker: Opens phone gallery for image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onCreateProjectWithMedia(uri.toString(), "Photo Video", 6000L) { newId ->
                onOpenProject(newId)
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_quran_editor_official),
                            contentDescription = "Official Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column {
                            Text(
                                text = "Quran Video Editor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Hafiz Abdul Majid Mangrew",
                                fontSize = 11.sp,
                                color = InShotYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // PRIMARY HERO ACTION: + ADD VIDEO (Direct Gallery Opening)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF063325)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, InShotYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Open gallery directly with zero unnecessary dialogs
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                    .testTag("home_add_video_hero_btn")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(InShotYellow, Color(0xFFFFA000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Video",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "+ Add Video",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tap to pick a video directly from your phone gallery",
                        fontSize = 12.sp,
                        color = Color(0xFFB2DFDB),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECONDARY ACTIONS: [ Video ] and [ Photo / Image ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Video Button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                        .testTag("home_secondary_video_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0E4A37)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = InShotYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Video",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Select from gallery",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Secondary Photo / Image Button
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("home_secondary_photo_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0E4A37)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = InShotCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Photo/Image",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Photo video reels",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECENT PROJECTS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects (${projects.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                // Quick sample button in case running in emulator without gallery media
                TextButton(
                    onClick = {
                        onCreateProjectWithMedia(null, "Surah Ar-Rahman", 11000L) { newId ->
                            onOpenProject(newId)
                        }
                    }
                ) {
                    Text(
                        text = "+ Sample Quran Clip",
                        fontSize = 11.sp,
                        color = InShotYellow,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No drafts yet. Tap '+ Add Video' to start editing.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        RecentProjectCard(
                            project = project,
                            onOpen = { onOpenProject(project.id) },
                            onDuplicate = { onDuplicateProject(project) },
                            onDelete = { onDeleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentProjectCard(
    project: Project,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val dateFormatted = remember(project.lastModified) {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(project.lastModified))
    }

    val totalDurationSeconds = project.totalDurationMs / 1000f
    val durationText = remember(totalDurationSeconds) {
        val min = (totalDurationSeconds / 60).toInt()
        val sec = (totalDurationSeconds % 60).toInt()
        String.format(Locale.getDefault(), "%02d:%02d", min, sec)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("recent_project_${project.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0A3326), Color(0xFF021B13))
                        )
                    )
                    .border(1.dp, InShotYellow.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = project.clips.firstOrNull()?.sceneIcon ?: "🕌",
                    fontSize = 24.sp
                )
            }

            // Title & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = durationText,
                        color = InShotYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "•", color = TextMuted, fontSize = 11.sp)
                    Text(
                        text = "${project.clips.size} clips",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = dateFormatted,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            // More Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color.White) },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFFF5252)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF5252)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

private fun extractVideoInfo(context: Context, uri: Uri): Pair<Long, String> {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val titleStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val durationMs = durationStr?.toLongOrNull()?.coerceAtLeast(1000L) ?: 10000L
        val cleanTitle = if (!titleStr.isNullOrBlank()) titleStr else "Imported Video"
        retriever.release()
        Pair(durationMs, cleanTitle)
    } catch (_: Exception) {
        Pair(12000L, "Imported Video")
    }
}
