package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ProjectRepository
import com.example.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    val projects: StateFlow<List<Project>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())

        projects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.checkAndSeedInitialProjects()
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0,
                title = "${project.title} (Copy)",
                lastModified = System.currentTimeMillis()
            )
            repository.saveProject(duplicate)
        }
    }

    fun createProjectFromMedia(
        uriString: String?,
        title: String = "Quran Video",
        durationMs: Long = 10000L,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val clip = if (uriString != null) {
                VideoClip(
                    id = "clip_${System.currentTimeMillis()}",
                    title = title,
                    durationMs = durationMs,
                    trimEndMs = durationMs,
                    uriString = uriString,
                    themeGradientStart = 0xFF0D5C3A,
                    themeGradientEnd = 0xFF042617,
                    sceneIcon = "📹"
                )
            } else {
                VideoClip(
                    id = "clip_${System.currentTimeMillis()}",
                    title = title,
                    durationMs = 8000L,
                    trimEndMs = 8000L,
                    filter = FilterType.CINEMATIC,
                    themeGradientStart = 0xFF0D5C3A,
                    themeGradientEnd = 0xFF042617,
                    sceneIcon = "🕌"
                )
            }

            val newProject = Project(
                id = 0,
                title = title,
                lastModified = System.currentTimeMillis(),
                canvasRatio = CanvasRatio.RATIO_9_16,
                bgType = CanvasBgType.BLUR,
                bgColorHex = 0xFF04241B,
                clips = listOf(clip),
                audioTracks = emptyList(),
                textOverlays = emptyList(),
                stickerOverlays = emptyList()
            )

            val newId = repository.saveProject(newProject)
            onCreated(newId)
        }
    }

    fun createNewProject(
        title: String = "Untitled Video",
        canvasRatio: CanvasRatio = CanvasRatio.RATIO_9_16,
        templateType: String = "Blank",
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val clips = when (templateType) {
                "Reel / Short" -> listOf(
                    VideoClip("clip_r1", "Intro Beat", 3000L, filter = FilterType.CINEMATIC, themeGradientStart = 0xFFFF416C, themeGradientEnd = 0xFFFF4B2B, sceneIcon = "🔥"),
                    VideoClip("clip_r2", "Main Action", 4000L, filter = FilterType.VIVID, themeGradientStart = 0xFF7F00FF, themeGradientEnd = 0xFFE100FF, sceneIcon = "🚀"),
                    VideoClip("clip_r3", "Outro Glow", 3000L, filter = FilterType.WARM, themeGradientStart = 0xFFF7971E, themeGradientEnd = 0xFFFFD200, sceneIcon = "✨")
                )
                "Travel Vlog" -> listOf(
                    VideoClip("clip_t1", "Flight & Clouds", 4000L, filter = FilterType.COOL, themeGradientStart = 0xFF00C6FF, themeGradientEnd = 0xFF0072FF, sceneIcon = "✈️"),
                    VideoClip("clip_t2", "Mountain View", 5000L, filter = FilterType.CINEMATIC, themeGradientStart = 0xFF11998E, themeGradientEnd = 0xFF38EF7D, sceneIcon = "🏔️"),
                    VideoClip("clip_t3", "Campfire Night", 4500L, filter = FilterType.WARM, themeGradientStart = 0xFFFF512F, themeGradientEnd = 0xFFDD2476, sceneIcon = "⛺")
                )
                "Fitness / Energy" -> listOf(
                    VideoClip("clip_f1", "Warmup", 3500L, filter = FilterType.BLACK_WHITE, themeGradientStart = 0xFF232526, themeGradientEnd = 0xFF414345, sceneIcon = "🏋️"),
                    VideoClip("clip_f2", "Intense Set", 4000L, filter = FilterType.GLITCH, themeGradientStart = 0xFFFF0844, themeGradientEnd = 0xFFFFB199, sceneIcon = "⚡")
                )
                else -> listOf(
                    VideoClip("clip_init_1", "Clip 1", 4000L, filter = FilterType.ORIGINAL, themeGradientStart = 0xFFFF3366, themeGradientEnd = 0xFFFF9900, sceneIcon = "🎬"),
                    VideoClip("clip_init_2", "Clip 2", 3500L, filter = FilterType.CINEMATIC, themeGradientStart = 0xFF3A7BD5, themeGradientEnd = 0xFF3A6073, sceneIcon = "🎥")
                )
            }

            val newProject = Project(
                id = 0,
                title = title,
                lastModified = System.currentTimeMillis(),
                canvasRatio = canvasRatio,
                bgType = CanvasBgType.BLUR,
                bgColorHex = 0xFF121217,
                clips = clips,
                audioTracks = listOf(
                    AudioTrack("init_audio", "Summer Groove", "InShot Studio", 10000L, 0L, 0.8f)
                ),
                textOverlays = listOf(
                    TextOverlay("init_text", "NEW STORY ✨", 500L, 3000L, 0.5f, 0.25f, 24f, 0xFFFFFFFF, 0x88FF2A55)
                ),
                stickerOverlays = listOf(
                    StickerOverlay("init_sticker", "⭐", 1000L, 2500L, 0.85f, 0.15f, 1.2f)
                )
            )

            val newId = repository.saveProject(newProject)
            onCreated(newId)
        }
    }
}
