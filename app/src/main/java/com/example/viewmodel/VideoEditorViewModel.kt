package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ExportProgressState(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val currentFrame: Int = 0,
    val totalFrames: Int = 300,
    val resolution: String = "1080p (Full HD)",
    val fps: Int = 30,
    val estimatedSizeMb: Float = 14.8f,
    val isCompleted: Boolean = false,
    val exportedFileName: String = ""
)

data class AutoCaptionGeneratingState(
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = ""
)

class VideoEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository =
        ProjectRepository(AppDatabase.getDatabase(application).projectDao())

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _currentPlayheadMs = MutableStateFlow(0L)
    val currentPlayheadMs: StateFlow<Long> = _currentPlayheadMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedClipIndex = MutableStateFlow(0)
    val selectedClipIndex: StateFlow<Int> = _selectedClipIndex.asStateFlow()

    private val _selectedTextOverlayId = MutableStateFlow<String?>(null)
    val selectedTextOverlayId: StateFlow<String?> = _selectedTextOverlayId.asStateFlow()

    private val _activeTool = MutableStateFlow<EditorTool?>(null)
    val activeTool: StateFlow<EditorTool?> = _activeTool.asStateFlow()

    private val _exportState = MutableStateFlow(ExportProgressState())
    val exportState: StateFlow<ExportProgressState> = _exportState.asStateFlow()

    private val _autoCaptionState = MutableStateFlow(AutoCaptionGeneratingState())
    val autoCaptionState: StateFlow<AutoCaptionGeneratingState> = _autoCaptionState.asStateFlow()

    private val _timelineZoom = MutableStateFlow(1.0f) // 0.5x to 3.0x
    val timelineZoom: StateFlow<Float> = _timelineZoom.asStateFlow()

    private val _isFillCanvas = MutableStateFlow(false)
    val isFillCanvas: StateFlow<Boolean> = _isFillCanvas.asStateFlow()

    private val _editorCustomizationEnabled = MutableStateFlow(false)
    val editorCustomizationEnabled: StateFlow<Boolean> = _editorCustomizationEnabled.asStateFlow()

    fun zoomInTimeline() {
        _timelineZoom.value = (_timelineZoom.value + 0.25f).coerceAtMost(3.0f)
    }

    fun zoomOutTimeline() {
        _timelineZoom.value = (_timelineZoom.value - 0.25f).coerceAtLeast(0.5f)
    }

    fun setTimelineZoom(zoom: Float) {
        _timelineZoom.value = zoom.coerceIn(0.5f, 3.0f)
    }

    fun toggleFillCanvas() {
        _isFillCanvas.value = !_isFillCanvas.value
    }

    fun setEditorCustomizationEnabled(enabled: Boolean) {
        _editorCustomizationEnabled.value = enabled
    }

    // Undo / Redo history stacks
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var playbackJob: Job? = null

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val loaded = repository.getProjectById(projectId) ?: createFallbackProject(projectId)
            _project.value = loaded
            _currentPlayheadMs.value = 0L
            _selectedClipIndex.value = 0
            _selectedTextOverlayId.value = null
            _activeTool.value = null
            undoStack.clear()
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    private fun createFallbackProject(id: Long): Project {
        return Project(
            id = id,
            title = "Original Video",
            lastModified = System.currentTimeMillis(),
            canvasRatio = CanvasRatio.RATIO_ORIGINAL,
            bgType = CanvasBgType.COLOR,
            bgColorHex = 0xFF000000,
            clips = listOf(
                VideoClip("clip_fb_1", "Original Video", 5000L, filter = FilterType.ORIGINAL, sceneIcon = "🎬")
            ),
            audioTracks = emptyList(),
            textOverlays = emptyList(),
            stickerOverlays = emptyList(),
            imageOverlays = emptyList()
        )
    }

    private fun pushHistory() {
        _project.value?.let { current ->
            undoStack.add(current)
            if (undoStack.size > 20) undoStack.removeAt(0)
            redoStack.clear()
            updateUndoRedoStates()
        }
    }

    private fun updateUndoRedoStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val current = _project.value ?: return
            redoStack.add(current)
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _project.value = previous
            updateUndoRedoStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val current = _project.value ?: return
            undoStack.add(current)
            val next = redoStack.removeAt(redoStack.lastIndex)
            _project.value = next
            updateUndoRedoStates()
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        val totalDuration = _project.value?.totalDurationMs ?: 1000L
        if (_currentPlayheadMs.value >= totalDuration) {
            _currentPlayheadMs.value = 0L
        }
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val intervalMs = 25L
            while (isActive && _isPlaying.value) {
                delay(intervalMs)
                val current = _currentPlayheadMs.value
                val maxDur = _project.value?.totalDurationMs ?: 1000L
                val next = current + intervalMs
                if (next >= maxDur) {
                    _currentPlayheadMs.value = 0L // loop
                } else {
                    _currentPlayheadMs.value = next
                }
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        val maxDur = _project.value?.totalDurationMs ?: 1000L
        val clamped = timeMs.coerceIn(0L, maxDur)
        _currentPlayheadMs.value = clamped

        // update active clip selection based on playhead
        val currentProject = _project.value ?: return
        var accumulated = 0L
        currentProject.clips.forEachIndexed { index, clip ->
            val clipDur = clip.trimmedDurationMs
            if (clamped >= accumulated && clamped <= accumulated + clipDur) {
                _selectedClipIndex.value = index
                return@forEachIndexed
            }
            accumulated += clipDur
        }
    }

    fun selectClip(index: Int) {
        val current = _project.value ?: return
        if (index in current.clips.indices) {
            _selectedClipIndex.value = index
            // seek to start of this clip
            var offset = 0L
            for (i in 0 until index) {
                offset += current.clips[i].trimmedDurationMs
            }
            _currentPlayheadMs.value = offset
        }
    }

    fun setActiveTool(tool: EditorTool?) {
        _activeTool.value = tool
    }

    // --- Editing operations ---

    fun setCanvasRatio(ratio: CanvasRatio) {
        val current = _project.value ?: return
        pushHistory()
        _project.value = current.copy(canvasRatio = ratio)
    }

    fun setCanvasBackground(type: CanvasBgType, colorHex: Long) {
        val current = _project.value ?: return
        pushHistory()
        _project.value = current.copy(bgType = type, bgColorHex = colorHex)
    }

    fun updateSelectedClip(transform: (VideoClip) -> VideoClip) {
        val current = _project.value ?: return
        val idx = _selectedClipIndex.value
        if (idx in current.clips.indices) {
            pushHistory()
            val updatedClips = current.clips.toMutableList()
            updatedClips[idx] = transform(updatedClips[idx])
            _project.value = current.copy(clips = updatedClips)
        }
    }

    fun trimClip(trimStartMs: Long, trimEndMs: Long) {
        updateSelectedClip { clip ->
            val start = trimStartMs.coerceIn(0L, clip.durationMs - 200L)
            val end = trimEndMs.coerceIn(start + 200L, clip.durationMs)
            clip.copy(trimStartMs = start, trimEndMs = end)
        }
    }

    fun splitClipAtPlayhead() {
        val current = _project.value ?: return
        val idx = _selectedClipIndex.value
        if (idx !in current.clips.indices) return

        val clip = current.clips[idx]
        // Calculate offset into this clip
        var clipStartOffset = 0L
        for (i in 0 until idx) {
            clipStartOffset += current.clips[i].trimmedDurationMs
        }
        val playheadInClip = (_currentPlayheadMs.value - clipStartOffset).coerceAtLeast(0L)
        val relativeMsInSource = (clip.trimStartMs + (playheadInClip * clip.speed).toLong())
            .coerceIn(clip.trimStartMs + 200L, clip.trimEndMs - 200L)

        pushHistory()
        val firstHalf = clip.copy(
            id = "${clip.id}_part1",
            title = "${clip.title} (Part 1)",
            trimStartMs = clip.trimStartMs,
            trimEndMs = relativeMsInSource
        )
        val secondHalf = clip.copy(
            id = "${clip.id}_part2",
            title = "${clip.title} (Part 2)",
            trimStartMs = relativeMsInSource,
            trimEndMs = clip.trimEndMs
        )

        val updatedClips = current.clips.toMutableList()
        updatedClips.removeAt(idx)
        updatedClips.add(idx, firstHalf)
        updatedClips.add(idx + 1, secondHalf)

        _project.value = current.copy(clips = updatedClips)
        _selectedClipIndex.value = idx + 1
    }

    fun deleteSelectedClip() {
        val current = _project.value ?: return
        if (current.clips.size <= 1) return // Keep at least one clip
        val idx = _selectedClipIndex.value
        if (idx in current.clips.indices) {
            pushHistory()
            val updated = current.clips.toMutableList()
            updated.removeAt(idx)
            _project.value = current.copy(clips = updated)
            _selectedClipIndex.value = (idx - 1).coerceAtLeast(0)
            _currentPlayheadMs.value = 0L
        }
    }

    fun duplicateSelectedClip() {
        val current = _project.value ?: return
        val idx = _selectedClipIndex.value
        if (idx in current.clips.indices) {
            pushHistory()
            val clip = current.clips[idx]
            val copy = clip.copy(
                id = "${clip.id}_copy_${System.currentTimeMillis() % 10000}",
                title = "${clip.title} (Copy)"
            )
            val updated = current.clips.toMutableList()
            updated.add(idx + 1, copy)
            _project.value = current.copy(clips = updated)
            _selectedClipIndex.value = idx + 1
        }
    }

    fun addClipWithUri(title: String, uriString: String, durationMs: Long) {
        val current = _project.value ?: return
        pushHistory()
        val newClip = VideoClip(
            id = "clip_${System.currentTimeMillis()}",
            title = title,
            durationMs = durationMs.coerceAtLeast(1000L),
            trimStartMs = 0L,
            trimEndMs = durationMs.coerceAtLeast(1000L),
            uriString = uriString,
            speed = 1.0f,
            volume = 1.0f,
            rotationAngle = 0f,
            isFlippedH = false,
            isFlippedV = false,
            filter = FilterType.ORIGINAL,
            brightness = 0f,
            contrast = 0f,
            saturation = 0f,
            vignette = 0f,
            themeGradientStart = 0xFF141920,
            themeGradientEnd = 0xFF0D1217,
            sceneIcon = "🎬"
        )
        val updated = current.clips + newClip
        _project.value = current.copy(clips = updated)
        _selectedClipIndex.value = updated.lastIndex
    }

    fun addClip(title: String, durationMs: Long = 4000L, gradientStart: Long = 0xFFFF4081, gradientEnd: Long = 0xFF7C4DFF, icon: String = "🎬") {
        val current = _project.value ?: return
        pushHistory()
        val newClip = VideoClip(
            id = "clip_${System.currentTimeMillis()}",
            title = title,
            durationMs = durationMs,
            themeGradientStart = gradientStart,
            themeGradientEnd = gradientEnd,
            sceneIcon = icon
        )
        val updated = current.clips + newClip
        _project.value = current.copy(clips = updated)
        _selectedClipIndex.value = updated.lastIndex
    }

    fun setSpeed(speed: Float) {
        updateSelectedClip { it.copy(speed = speed) }
    }

    fun setVolume(vol: Float) {
        updateSelectedClip { it.copy(volume = vol) }
    }

    fun rotate90() {
        updateSelectedClip {
            it.copy(rotationAngle = (it.rotationAngle + 90f) % 360f)
        }
    }

    fun flipHorizontal() {
        updateSelectedClip { it.copy(isFlippedH = !it.isFlippedH) }
    }

    fun setFilter(filter: FilterType) {
        updateSelectedClip { it.copy(filter = filter) }
    }

    fun updateAdjustments(brightness: Float, contrast: Float, saturation: Float, vignette: Float) {
        updateSelectedClip {
            it.copy(
                brightness = brightness,
                contrast = contrast,
                saturation = saturation,
                vignette = vignette
            )
        }
    }

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
    }

    fun stopVoiceRecording() {
        if (!_isRecordingVoice.value) return
        _isRecordingVoice.value = false
        val current = _project.value ?: return
        pushHistory()
        val newAudio = AudioTrack(
            id = "voice_${System.currentTimeMillis()}",
            title = "Voice Recitation",
            artist = "Microphone",
            durationMs = 4500L,
            startOffsetMs = _currentPlayheadMs.value,
            volume = 1.0f,
            isVoiceover = true,
            colorHex = 0xFFFFD700
        )
        _project.value = current.copy(audioTracks = current.audioTracks + newAudio)
    }

    fun addBasmala(style: CaptionStyle = CaptionStyle.ARABIC_GOLD) {
        val current = _project.value ?: return
        pushHistory()
        val newId = "basmala_${System.currentTimeMillis()}"
        val overlay = TextOverlay(
            id = newId,
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝",
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 4000L,
            posX = 0.5f,
            posY = 0.22f,
            fontSizeSp = 28f,
            textColorHex = style.textColorHex,
            bgColorHex = style.bgColorHex,
            fontStyle = "Bold",
            isBasmala = true,
            hasBackgroundBox = true
        )
        _project.value = current.copy(textOverlays = current.textOverlays + overlay)
        _selectedTextOverlayId.value = newId
    }

    fun addImageOverlay(uriString: String?, title: String = "Photo Layer") {
        val current = _project.value ?: return
        pushHistory()
        val newImg = ImageOverlay(
            id = "img_${System.currentTimeMillis()}",
            uriString = uriString,
            title = title,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 4000L,
            posX = 0.5f,
            posY = 0.5f
        )
        _project.value = current.copy(imageOverlays = current.imageOverlays + newImg)
    }

    // --- Audio Tracks ---

    fun addAudioTrack(title: String, artist: String, durationMs: Long = 8000L) {
        val current = _project.value ?: return
        pushHistory()
        val newAudio = AudioTrack(
            id = "audio_${System.currentTimeMillis()}",
            title = title,
            artist = artist,
            durationMs = durationMs,
            startOffsetMs = _currentPlayheadMs.value,
            volume = 0.85f
        )
        _project.value = current.copy(audioTracks = current.audioTracks + newAudio)
    }

    fun removeAudioTrack(id: String) {
        val current = _project.value ?: return
        pushHistory()
        _project.value = current.copy(audioTracks = current.audioTracks.filterNot { it.id == id })
    }

    // --- Text Overlays ---

    fun addTextOverlay(text: String, colorHex: Long = 0xFFFFFFFF, bgColorHex: Long = 0x88000000) {
        val current = _project.value ?: return
        pushHistory()
        val newId = "txt_${System.currentTimeMillis()}"
        val newText = TextOverlay(
            id = newId,
            text = text,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 3000L,
            textColorHex = colorHex,
            bgColorHex = bgColorHex
        )
        _project.value = current.copy(textOverlays = current.textOverlays + newText)
        _selectedTextOverlayId.value = newId
    }

    fun selectTextOverlay(id: String?) {
        _selectedTextOverlayId.value = id
    }

    fun trimTextOverlay(id: String, newStartMs: Long, newDurationMs: Long) {
        val current = _project.value ?: return
        val updated = current.textOverlays.map {
            if (it.id == id) {
                it.copy(
                    startOffsetMs = newStartMs.coerceAtLeast(0L),
                    durationMs = newDurationMs.coerceAtLeast(400L)
                )
            } else it
        }
        pushHistory()
        _project.value = current.copy(textOverlays = updated)
    }

    fun removeTextOverlay(id: String) {
        val current = _project.value ?: return
        pushHistory()
        if (_selectedTextOverlayId.value == id) {
            _selectedTextOverlayId.value = null
        }
        _project.value = current.copy(textOverlays = current.textOverlays.filterNot { it.id == id })
    }

    // --- Sticker Overlays ---

    fun addSticker(emoji: String) {
        val current = _project.value ?: return
        pushHistory()
        val newSticker = StickerOverlay(
            id = "stk_${System.currentTimeMillis()}",
            emojiOrIcon = emoji,
            startOffsetMs = _currentPlayheadMs.value,
            durationMs = 2500L,
            posX = 0.5f,
            posY = 0.4f
        )
        _project.value = current.copy(stickerOverlays = current.stickerOverlays + newSticker)
    }

    fun removeSticker(id: String) {
        val current = _project.value ?: return
        pushHistory()
        _project.value = current.copy(stickerOverlays = current.stickerOverlays.filterNot { it.id == id })
    }

    // --- Auto Text / Captions ---

    fun generateAutoCaptions(
        language: String,
        style: CaptionStyle,
        onComplete: () -> Unit = {}
    ) {
        val current = _project.value ?: return
        viewModelScope.launch {
            _autoCaptionState.value = AutoCaptionGeneratingState(
                isGenerating = true,
                progress = 0.1f,
                statusText = "Analyzing audio waveform & speech tracks..."
            )
            delay(400)
            _autoCaptionState.value = _autoCaptionState.value.copy(
                progress = 0.45f,
                statusText = "Recognizing spoken sentences ($language)..."
            )
            delay(500)
            _autoCaptionState.value = _autoCaptionState.value.copy(
                progress = 0.8f,
                statusText = "Aligning subtitle timestamps with video timeline..."
            )
            delay(400)

            val phrases = when (language) {
                "Arabic (مع التشكيل)" -> listOf(
                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ 🌸",
                    "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ✨",
                    "إِنَّ مَعَ الْعُسْرِ يُسْرًا 🕊️",
                    "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي 💫",
                    "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ 🤲"
                )
                "Urdu (اعراب کے ساتھ)" -> listOf(
                    "خُوشْ آمَدِیدْ دَوْسْتُو! 👋",
                    "یہِ مَنْظَرْ کِتْنَا خُوبْصُورَتْ ہَے! ✨",
                    "رَنْگْ اَوْر فِلْٹَر بَہُتْ عُمْدَہ ہَیں 🎬",
                    "پُورِی وِیڈِیُو کُو مُکَمَّلْ دِیکھِیں 🔥",
                    "وِیڈِیُو کُو لَائکْ اَوْر شَیئرْ ضَرُورْ کَرِیں! ❤️"
                )
                "Hinglish / Urdu" -> listOf(
                    "Welcome guys! 👋",
                    "Yeh scene kitna zabardast lag raha hai ✨",
                    "Color grading and mood ekdum perfect 🎬",
                    "Pure energy and cinematic vibes 🔥",
                    "Video pasand aaye toh Like & Share zaroor karein! ❤️"
                )
                "Hindi" -> listOf(
                    "नमस्ते दोस्तों, स्वागत है आपका! 👋",
                    "यह खूबसूरत सिनेमैटिक नज़ारा देखिए ✨",
                    "इस शॉट का विज़ुअल बहुत ही शानदार है 🎬",
                    "पूरी एनर्जी और वाइब्स बहुत खास हैं 🔥",
                    "चैनल को सब्सक्राइब और लाइक ज़रूर करें! ❤️"
                )
                else -> listOf(
                    "Welcome back to my video! 👋",
                    "Check out this breathtaking aesthetic scene ✨",
                    "Cinematic colors and smooth motion 🎬",
                    "The rhythm and vibes are immaculate 🔥",
                    "Make sure to follow and share! ❤️"
                )
            }

            // Distribute across timeline
            val totalDurationMs = current.totalDurationMs
            val segmentDuration = (totalDurationMs / phrases.size.coerceAtLeast(1)).coerceAtLeast(1500L)

            val newCaptions = phrases.mapIndexed { index, phrase ->
                val start = index * segmentDuration
                val dur = (segmentDuration - 200L).coerceAtLeast(1200L)
                TextOverlay(
                    id = "auto_cap_${System.currentTimeMillis()}_$index",
                    text = phrase,
                    startOffsetMs = start,
                    durationMs = dur,
                    posX = 0.5f,
                    posY = 0.82f, // Bottom subtitle zone
                    fontSizeSp = style.fontSizeSp,
                    textColorHex = style.textColorHex,
                    bgColorHex = style.bgColorHex,
                    fontStyle = "Bold",
                    hasBackgroundBox = style.hasBackgroundBox,
                    isAutoCaption = true
                )
            }

            pushHistory()
            // Keep user-added manual text overlays, replace prior auto-captions
            val manualTexts = current.textOverlays.filterNot { it.isAutoCaption }
            _project.value = current.copy(textOverlays = manualTexts + newCaptions)

            _autoCaptionState.value = AutoCaptionGeneratingState(
                isGenerating = false,
                progress = 1.0f,
                statusText = "Auto Captions Generated!"
            )
            onComplete()
        }
    }

    fun updateCaptionText(id: String, newText: String) {
        val current = _project.value ?: return
        val updated = current.textOverlays.map {
            if (it.id == id) it.copy(text = newText) else it
        }
        pushHistory()
        _project.value = current.copy(textOverlays = updated)
    }

    fun applyStyleToAllCaptions(style: CaptionStyle) {
        val current = _project.value ?: return
        val updated = current.textOverlays.map {
            if (it.isAutoCaption) {
                it.copy(
                    fontSizeSp = style.fontSizeSp,
                    textColorHex = style.textColorHex,
                    bgColorHex = style.bgColorHex,
                    hasBackgroundBox = style.hasBackgroundBox
                )
            } else it
        }
        pushHistory()
        _project.value = current.copy(textOverlays = updated)
    }

    fun clearAllAutoCaptions() {
        val current = _project.value ?: return
        pushHistory()
        _project.value = current.copy(
            textOverlays = current.textOverlays.filterNot { it.isAutoCaption }
        )
    }

    // --- Quran Ayahs to Timeline ---

    fun addQuranAyahsToTimeline(
        selectedAyahs: List<AyahItem>,
        includeUrduTranslation: Boolean,
        style: CaptionStyle = CaptionStyle.ARABIC_GOLD
    ) {
        val current = _project.value ?: return
        if (selectedAyahs.isEmpty()) return

        pushHistory()
        val currentPlayhead = _currentPlayheadMs.value
        val ayahDuration = 3800L
        var currentOffset = currentPlayhead

        val newOverlays = selectedAyahs.mapIndexed { index, ayah ->
            val displayText = if (includeUrduTranslation && ayah.urduTranslation.isNotBlank()) {
                "${ayah.arabicText} ۝\n${ayah.urduTranslation}"
            } else {
                "${ayah.arabicText} ۝"
            }

            val overlay = TextOverlay(
                id = "quran_ayah_${System.currentTimeMillis()}_$index",
                text = displayText,
                startOffsetMs = currentOffset,
                durationMs = ayahDuration,
                posX = 0.5f,
                posY = 0.80f,
                fontSizeSp = if (includeUrduTranslation) 22f else 25f,
                textColorHex = style.textColorHex,
                bgColorHex = style.bgColorHex,
                fontStyle = "Bold",
                hasBackgroundBox = true,
                isAutoCaption = true
            )
            currentOffset += ayahDuration
            overlay
        }

        _project.value = current.copy(
            textOverlays = current.textOverlays + newOverlays
        )
        _selectedTextOverlayId.value = newOverlays.firstOrNull()?.id
    }

    // --- Save & Export ---

    fun saveProject() {
        val current = _project.value ?: return
        viewModelScope.launch {
            val updated = current.copy(lastModified = System.currentTimeMillis())
            repository.saveProject(updated)
            _project.value = updated
        }
    }

    fun startExport(resolution: String, fps: Int) {
        pause()
        val current = _project.value ?: return
        val totalDuration = current.totalDurationMs
        val frames = ((totalDuration / 1000f) * fps).toInt().coerceAtLeast(90)

        val sizeMb = when (resolution) {
            "4K (Ultra HD)" -> (totalDuration / 1000f) * 4.2f
            "2K (Quad HD)" -> (totalDuration / 1000f) * 2.5f
            "1080p (Full HD)" -> (totalDuration / 1000f) * 1.4f
            else -> (totalDuration / 1000f) * 0.8f
        }

        _exportState.value = ExportProgressState(
            isExporting = true,
            progress = 0f,
            currentFrame = 0,
            totalFrames = frames,
            resolution = resolution,
            fps = fps,
            estimatedSizeMb = (sizeMb * 10).toInt() / 10f,
            isCompleted = false,
            exportedFileName = "InShot_${System.currentTimeMillis()}.mp4"
        )

        viewModelScope.launch {
            for (f in 1..frames) {
                delay(20L)
                val prog = f.toFloat() / frames
                _exportState.value = _exportState.value.copy(
                    progress = prog,
                    currentFrame = f
                )
            }
            saveProject()
            _exportState.value = _exportState.value.copy(
                isExporting = false,
                isCompleted = true,
                progress = 1.0f
            )
        }
    }

    fun dismissExport() {
        _exportState.value = ExportProgressState()
    }
}
