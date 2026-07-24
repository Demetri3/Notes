package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.data.NoteDatabase
import com.example.notes.ui.NoteViewModel
import com.example.notes.ui.screens.HomeScreen
import com.example.notes.ui.screens.NoteEditorScreen
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = NoteDatabase.getDatabase(this)
        val noteDao = database.noteDao()
        val viewModelFactory = NoteViewModel.Factory(noteDao)

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            
            NotesTheme(themeMode = themeMode) {
                // Wrapping everything in a Surface with background color prevents white flashes during animations
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: NoteViewModel = viewModel(factory = viewModelFactory)
                    val navigator = rememberListDetailPaneScaffoldNavigator<Long?>()
                    val scope = rememberCoroutineScope()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    
                    val isEditing = navigator.currentDestination?.contentKey != null
                    val isDetailVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] != PaneAdaptedValue.Hidden
                    val shouldDisableDrawer = isEditing && !isDetailVisible

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !shouldDisableDrawer,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(Modifier.height(48.dp))
                                Text("Settings", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                                HorizontalDivider()
                                
                                NavigationDrawerItem(
                                    label = { Text("Light Theme") },
                                    selected = themeMode == ThemeMode.LIGHT,
                                    onClick = { themeMode = ThemeMode.LIGHT; scope.launch { drawerState.close() } },
                                    icon = { Icon(Icons.Default.Brightness7, contentDescription = null) }
                                )
                                NavigationDrawerItem(
                                    label = { Text("Dark Theme") },
                                    selected = themeMode == ThemeMode.DARK,
                                    onClick = { themeMode = ThemeMode.DARK; scope.launch { drawerState.close() } },
                                    icon = { Icon(Icons.Default.Brightness4, contentDescription = null) }
                                )
                                NavigationDrawerItem(
                                    label = { Text("System Default") },
                                    selected = themeMode == ThemeMode.SYSTEM,
                                    onClick = { themeMode = ThemeMode.SYSTEM; scope.launch { drawerState.close() } },
                                    icon = { Icon(Icons.Default.SettingsSuggest, contentDescription = null) }
                                )
                            }
                        }
                    ) {
                        ListDetailPaneScaffold(
                            directive = navigator.scaffoldDirective,
                            value = navigator.scaffoldValue,
                            listPane = {
                                AnimatedPane(
                                    enterTransition = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.95f),
                                    exitTransition = fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 0.95f)
                                ) {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onNoteClick = { id ->
                                            scope.launch {
                                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id)
                                            }
                                        },
                                        onAddNoteClick = {
                                            scope.launch {
                                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, -1L) // Use -1 to indicate new note
                                            }
                                        },
                                        onMenuClick = {
                                            scope.launch { drawerState.open() }
                                        },
                                        menuEnabled = !shouldDisableDrawer
                                    )
                                }
                            },
                            detailPane = {
                                AnimatedPane(
                                    enterTransition = fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.95f),
                                    exitTransition = fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 0.95f)
                                ) {
                                    val noteId = navigator.currentDestination?.contentKey
                                    
                                    if (isDetailVisible) {
                                        if (noteId == null) {
                                            // Placeholder for landscape when no note is selected
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.NoteAdd,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(64.dp),
                                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                                                    )
                                                    Spacer(Modifier.height(16.dp))
                                                    Text(
                                                        "Select a note to view or edit,\nor create a new one.",
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                                    )
                                                }
                                            }
                                        } else {
                                            NoteEditorScreen(
                                                viewModel = viewModel,
                                                noteId = if (noteId == -1L) null else noteId,
                                                onBack = {
                                                    scope.launch {
                                                        navigator.navigateBack()
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
