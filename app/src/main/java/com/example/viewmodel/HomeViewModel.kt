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
        title: String = "Original Video",
        durationMs: Long = 10000L,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val clip = VideoClip(
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

            val newProject = Project(
                id = 0,
                title = title,
                lastModified = System.currentTimeMillis(),
                canvasRatio = CanvasRatio.RATIO_ORIGINAL,
                bgType = CanvasBgType.COLOR,
                bgColorHex = 0xFF000000,
                clips = listOf(clip),
                audioTracks = emptyList(),
                textOverlays = emptyList(),
                stickerOverlays = emptyList(),
                imageOverlays = emptyList()
            )

            val newId = repository.saveProject(newProject)
            onCreated(newId)
        }
    }

    fun createNewProject(
        title: String = "Original Video",
        canvasRatio: CanvasRatio = CanvasRatio.RATIO_ORIGINAL,
        templateType: String = "Blank",
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val clips = listOf(
                VideoClip(
                    id = "clip_${System.currentTimeMillis()}",
                    title = title,
                    durationMs = 8000L,
                    trimStartMs = 0L,
                    trimEndMs = 8000L,
                    filter = FilterType.ORIGINAL,
                    speed = 1.0f,
                    volume = 1.0f,
                    themeGradientStart = 0xFF141920,
                    themeGradientEnd = 0xFF0D1217,
                    sceneIcon = "🎬"
                )
            )

            val newProject = Project(
                id = 0,
                title = title,
                lastModified = System.currentTimeMillis(),
                canvasRatio = canvasRatio,
                bgType = CanvasBgType.COLOR,
                bgColorHex = 0xFF000000,
                clips = clips,
                audioTracks = emptyList(),
                textOverlays = emptyList(),
                stickerOverlays = emptyList(),
                imageOverlays = emptyList()
            )

            val newId = repository.saveProject(newProject)
            onCreated(newId)
        }
    }
}
