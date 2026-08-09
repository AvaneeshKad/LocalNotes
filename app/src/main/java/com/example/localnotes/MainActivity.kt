package com.example.localnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.localnotes.ui.editor.CanvasEditorScreen
import com.example.localnotes.ui.theme.LocalNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(noteViewModel: NoteViewModel = viewModel(
    factory = NoteViewModelFactory(
        (LocalContext.current.applicationContext as LocalNotesApplication).notesRepository
    )
)) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "notes_list") {
        composable("notes_list") {
            NotesListScreen(
                viewModel = noteViewModel,
                onAddNoteClick = { navController.navigate("canvas_editor") },
                onNoteClick = { noteId -> navController.navigate("canvas_editor?noteId=$noteId") }
            )
        }
        composable(
            route = "canvas_editor?noteId={noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
            CanvasEditorScreen(
                viewModel = noteViewModel,
                noteId = if (noteId == -1L) null else noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
