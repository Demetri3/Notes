package com.example.notes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.data.Note
import com.example.notes.ui.NoteViewModel
import com.example.notes.ui.theme.NoteColors
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    noteId: Long?,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(NoteColors.random().toArgb()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (noteId != null && noteId != -1L) {
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                title = note.title
                content = note.content
                color = note.color
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            viewModel.saveNote(
                                Note(
                                    id = if (noteId == -1L) 0 else (noteId ?: 0),
                                    title = title,
                                    content = content,
                                    timestamp = System.currentTimeMillis(),
                                    color = color
                                )
                            )
                        }
                        onBack()
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(
                    onClick = { /* Edit/Save specific action */ },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Title",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            
            Text(
                text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "Type something...",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    )
                },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = TextStyle(
                    fontSize = 18.sp
                )
            )
        }
    }
}
