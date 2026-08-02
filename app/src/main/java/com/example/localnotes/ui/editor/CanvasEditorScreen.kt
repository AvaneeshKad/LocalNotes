package com.example.localnotes.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.localnotes.NoteViewModel
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.ui.canvas.DrawingCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasEditorScreen(
    viewModel: NoteViewModel,
    noteId: Long? = null,
    onBack: () -> Unit
) {
    val existingNote = remember(noteId) { noteId?.let { viewModel.getNote(it) } }

    val strokes = remember { mutableStateListOf<Stroke>().apply { existingNote?.strokes?.let { addAll(it) } } }
    var titleText by remember { mutableStateOf(existingNote?.title ?: "") }
    var typedText by remember { mutableStateOf(existingNote?.content ?: "") }

    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedWidth by remember { mutableFloatStateOf(6f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val finalTitle = titleText.ifBlank { "Untitled Note" }
                            viewModel.saveOrUpdateNote(
                                id = noteId,
                                title = finalTitle,
                                content = typedText,
                                strokes = strokes.toList()
                            )
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note")
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
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text("Note Title...") },
                singleLine = true
            )

            OutlinedTextField(
                value = typedText,
                onValueChange = { typedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text("Type optional text notes here...") }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val availableColors = listOf(
                    Color.Black,
                    Color(0xFFD32F2F),
                    Color(0xFF1976D2),
                    Color(0xFF388E3C),
                    Color(0xFFFBC02D)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Size",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Slider(
                        value = selectedWidth,
                        onValueChange = { selectedWidth = it },
                        valueRange = 2f..24f
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                DrawingCanvas(
                    strokes = strokes,
                    selectedColor = selectedColor,
                    selectedWidth = selectedWidth,
                    currentAudioTimeMs = 0L
                )
            }
        }
    }
}
