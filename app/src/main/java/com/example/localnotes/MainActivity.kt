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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.localnotes.ui.editor.CanvasEditorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
fun AppNavigation(noteViewModel: NoteViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "notes_list") {
        composable("notes_list") {
            NotesListScreen(
                viewModel = noteViewModel,
                onAddNoteClick = { navController.navigate("canvas_editor") }
            )
        }
        composable("canvas_editor") {
            CanvasEditorScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
