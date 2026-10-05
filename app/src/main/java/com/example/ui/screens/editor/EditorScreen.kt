package com.example.ui.screens.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.screens.export.ExportProgressDialog
import com.example.ui.screens.export.ExportSettingsDialog
import com.example.ui.theme.*
import com.example.viewmodel.VideoEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: VideoEditorViewModel,
    onNavigateBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val currentPlayheadMs by viewModel.currentPlayheadMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedClipIndex by viewModel.selectedClipIndex.collectAsState()
    val activeTool by viewModel.activeTool.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val selectedTextOverlayId by viewModel.selectedTextOverlayId.collectAsState()

    var showSaveDraftDialog by remember { mutableStateOf(false) }
    var showExportSettingsDialog by remember { mutableStateOf(false) }

    // Intercept back button to prompt save draft
    BackHandler {
        showSaveDraftDialog = true
    }

    val currentProject = project
    if (currentProject == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = InShotRed)
        }
        return
    }

    val selectedClip = currentProject.clips.getOrNull(selectedClipIndex)

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
                        onClick = { showSaveDraftDialog = true },
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Undo
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.size(36.dp).testTag("editor_undo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) Color.White else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Redo
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.size(36.dp).testTag("editor_redo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) Color.White else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Aspect ratio quick badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.setActiveTool(EditorTool.CANVAS) }
                            .padding(end = 10.dp)
                    ) {
                        Text(
                            text = currentProject.canvasRatio.label,
                            color = InShotCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // SAVE / EXPORT Button (InShot's iconic top-right export button)
                    Button(
                        onClick = { showExportSettingsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = InShotRed),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("editor_export_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SAVE",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
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
        ) {
            // 1. Video Canvas Preview Area
            VideoCanvasView(
                project = currentProject,
                currentPlayheadMs = currentPlayheadMs,
                selectedClip = selectedClip,
                isPlaying = isPlaying,
                selectedTextOverlayId = selectedTextOverlayId,
                onSelectTextOverlay = { viewModel.selectTextOverlay(it) },
                onDeleteTextOverlay = { viewModel.removeTextOverlay(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // 2. Multi-track Timeline Scrubber with InShot Text Elements Track
            MultiTrackTimeline(
                project = currentProject,
                currentPlayheadMs = currentPlayheadMs,
                selectedClipIndex = selectedClipIndex,
                selectedTextOverlayId = selectedTextOverlayId,
                isPlaying = isPlaying,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSeekTo = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onSelectTextOverlay = { viewModel.selectTextOverlay(it) },
                onTrimTextOverlay = { id, start, dur -> viewModel.trimTextOverlay(id, start, dur) },
                onDeleteTextOverlay = { viewModel.removeTextOverlay(it) },
                onSplitClip = { viewModel.splitClipAtPlayhead() },
                onDeleteClip = { viewModel.deleteSelectedClip() },
                onAddClip = {
                    viewModel.addClip("New Clip", 4000L, 0xFF00E5FF, 0xFF7C4DFF, "✨")
                },
                onTrimClip = { start, end -> viewModel.trimClip(start, end) }
            )

            // 3. Active Tool Panel OR Main Tool Strip
            AnimatedContent(
                targetState = activeTool,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                },
                label = "tool_panel"
            ) { tool ->
                if (tool != null) {
                    when (tool) {
                        EditorTool.CANVAS -> {
                            CanvasToolPanel(
                                currentRatio = currentProject.canvasRatio,
                                currentBgType = currentProject.bgType,
                                currentBgColor = currentProject.bgColorHex,
                                onRatioSelected = { viewModel.setCanvasRatio(it) },
                                onBackgroundSelected = { type, color -> viewModel.setCanvasBackground(type, color) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.FILTER -> {
                            FilterToolPanel(
                                selectedFilter = selectedClip?.filter ?: FilterType.ORIGINAL,
                                brightness = selectedClip?.brightness ?: 0f,
                                contrast = selectedClip?.contrast ?: 1f,
                                saturation = selectedClip?.saturation ?: 1f,
                                vignette = selectedClip?.vignette ?: 0f,
                                onFilterSelected = { viewModel.setFilter(it) },
                                onAdjustmentsChanged = { b, c, s, v -> viewModel.updateAdjustments(b, c, s, v) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.MUSIC -> {
                            MusicToolPanel(
                                onAddTrack = { title, genre -> viewModel.addAudioTrack(title, genre) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.QURAN -> {
                            QuranToolPanel(
                                onAddAyahsToTimeline = { ayahs, withUrdu, style ->
                                    viewModel.addQuranAyahsToTimeline(ayahs, withUrdu, style)
                                },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.BASMALA -> {
                            BasmalaToolPanel(
                                onAddBasmala = { style -> viewModel.addBasmala(style) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.ADJUST -> {
                            AdjustToolPanel(
                                brightness = selectedClip?.brightness ?: 0f,
                                contrast = selectedClip?.contrast ?: 1f,
                                saturation = selectedClip?.saturation ?: 1f,
                                vignette = selectedClip?.vignette ?: 0f,
                                onAdjustmentsChanged = { b, c, s, v -> viewModel.updateAdjustments(b, c, s, v) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.AUDIO -> {
                            MusicToolPanel(
                                onAddTrack = { title, genre -> viewModel.addAudioTrack(title, genre) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.VOICE -> {
                            val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()
                            VoiceRecordToolPanel(
                                isRecording = isRecordingVoice,
                                onStartRecording = { viewModel.startVoiceRecording() },
                                onStopRecording = { viewModel.stopVoiceRecording() },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.IMAGE -> {
                            ImagePipToolPanel(
                                onAddImage = { uri -> viewModel.addImageOverlay(uri) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.AUTO_TEXT -> {
                            val autoCaptionState by viewModel.autoCaptionState.collectAsState()
                            AutoTextToolPanel(
                                captions = currentProject.textOverlays,
                                isGenerating = autoCaptionState.isGenerating,
                                progress = autoCaptionState.progress,
                                statusText = autoCaptionState.statusText,
                                onGenerateAutoCaptions = { lang, style ->
                                    viewModel.generateAutoCaptions(lang, style)
                                },
                                onUpdateCaption = { id, txt -> viewModel.updateCaptionText(id, txt) },
                                onDeleteCaption = { id -> viewModel.removeTextOverlay(id) },
                                onApplyStyleToAll = { style -> viewModel.applyStyleToAllCaptions(style) },
                                onClearAll = { viewModel.clearAllAutoCaptions() },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.TEXT -> {
                            TextToolPanel(
                                onAddText = { txt, col, bg -> viewModel.addTextOverlay(txt, col, bg) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.STICKER -> {
                            StickerToolPanel(
                                onAddSticker = { emoji -> viewModel.addSticker(emoji) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.SPEED -> {
                            SpeedToolPanel(
                                currentSpeed = selectedClip?.speed ?: 1.0f,
                                onSpeedChanged = { viewModel.setSpeed(it) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        EditorTool.VOLUME -> {
                            VolumeToolPanel(
                                currentVolume = selectedClip?.volume ?: 1.0f,
                                onVolumeChanged = { viewModel.setVolume(it) },
                                onClose = { viewModel.setActiveTool(null) }
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurface)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tool.label, color = Color.White, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { viewModel.setActiveTool(null) }) {
                                        Icon(Icons.Default.Check, null, tint = InShotRed)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Iconic Bottom Tools Strip
                    EditorBottomToolStrip(
                        onToolClick = { selected ->
                            when (selected) {
                                EditorTool.SPLIT -> viewModel.splitClipAtPlayhead()
                                EditorTool.DELETE -> viewModel.deleteSelectedClip()
                                EditorTool.DUPLICATE -> viewModel.duplicateSelectedClip()
                                EditorTool.ROTATE -> viewModel.rotate90()
                                EditorTool.FLIP -> viewModel.flipHorizontal()
                                else -> viewModel.setActiveTool(selected)
                            }
                        }
                    )
                }
            }
        }
    }

    // Save Draft Confirmation Dialog on Back
    if (showSaveDraftDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDraftDialog = false },
            title = { Text("Save draft?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Save your video draft before leaving? You can continue editing anytime.", color = TextSecondary) },
            containerColor = DarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProject()
                        showSaveDraftDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InShotRed)
                ) {
                    Text("Save Draft", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveDraftDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = TextMuted)
                }
            }
        )
    }

    // Export Settings Dialog
    if (showExportSettingsDialog) {
        ExportSettingsDialog(
            estimatedDurationSec = currentProject.totalDurationMs / 1000f,
            onDismiss = { showExportSettingsDialog = false },
            onStartExport = { res, fps ->
                showExportSettingsDialog = false
                viewModel.startExport(res, fps)
            }
        )
    }

    // Exporting Progress & Finished Dialog
    if (exportState.isExporting || exportState.isCompleted) {
        ExportProgressDialog(
            exportState = exportState,
            onDismiss = {
                viewModel.dismissExport()
            }
        )
    }
}

@Composable
fun EditorBottomToolStrip(
    onToolClick: (EditorTool) -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(EditorTool.values()) { tool ->
                EditorToolItem(tool = tool) {
                    onToolClick(tool)
                }
            }
        }
    }
}

@Composable
fun EditorToolItem(
    tool: EditorTool,
    onClick: () -> Unit
) {
    val icon = when (tool) {
        EditorTool.VIDEO -> Icons.Default.Movie
        EditorTool.CANVAS -> Icons.Default.AspectRatio
        EditorTool.QURAN -> Icons.Default.MenuBook
        EditorTool.BASMALA -> Icons.Default.AutoAwesome
        EditorTool.TEXT -> Icons.Default.Title
        EditorTool.AUTO_TEXT -> Icons.Default.ClosedCaption
        EditorTool.AUDIO, EditorTool.MUSIC -> Icons.Default.MusicNote
        EditorTool.VOICE -> Icons.Default.Mic
        EditorTool.IMAGE -> Icons.Default.Image
        EditorTool.FILTER -> Icons.Default.AutoFixHigh
        EditorTool.ADJUST -> Icons.Default.Tune
        EditorTool.SPEED -> Icons.Default.Speed
        EditorTool.VOLUME -> Icons.Default.VolumeUp
        EditorTool.ROTATE -> Icons.Default.RotateRight
        EditorTool.FLIP -> Icons.Default.Flip
        EditorTool.DUPLICATE -> Icons.Default.ContentCopy
        EditorTool.STICKER -> Icons.Default.EmojiEmotions
        EditorTool.TRIM -> Icons.Default.ContentCut
        EditorTool.SPLIT -> Icons.Default.CallSplit
        EditorTool.DELETE -> Icons.Default.Delete
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("tool_${tool.name.lowercase()}")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tool.label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
