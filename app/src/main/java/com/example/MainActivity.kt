package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.VideoEditorViewModel

sealed interface Screen {
    data object Home : Screen
    data class Editor(val projectId: Long) : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val editorViewModel: VideoEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    val projects by homeViewModel.projects.collectAsState()

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is Screen.Editor) {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it / 2 } + fadeOut()
                            } else {
                                slideInHorizontally { -it / 2 } + fadeIn() togetherWith
                                        slideOutHorizontally { it } + fadeOut()
                            }
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    projects = projects,
                                    onOpenProject = { projectId ->
                                        editorViewModel.loadProject(projectId)
                                        currentScreen = Screen.Editor(projectId)
                                    },
                                    onCreateProjectWithMedia = { uri, title, duration, onCreated ->
                                        homeViewModel.createProjectFromMedia(uri, title, duration) { newId ->
                                            onCreated(newId)
                                        }
                                    },
                                    onDuplicateProject = { project ->
                                        homeViewModel.duplicateProject(project)
                                    },
                                    onDeleteProject = { id ->
                                        homeViewModel.deleteProject(id)
                                    },
                                    onOpenSettings = {
                                        currentScreen = Screen.Settings
                                    }
                                )
                            }

                            is Screen.Editor -> {
                                EditorScreen(
                                    viewModel = editorViewModel,
                                    onNavigateBack = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }

                            is Screen.Settings -> {
                                BackHandler {
                                    currentScreen = Screen.Home
                                }
                                SettingsScreen(
                                    onNavigateBack = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
