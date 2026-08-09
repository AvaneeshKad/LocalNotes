package com.example.localnotes.ui.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.localnotes.NoteViewModel
import com.example.localnotes.data.export.PdfExporter
import com.example.localnotes.data.model.Note
import com.example.localnotes.data.model.NotePage
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.model.StrokeTool
import com.example.localnotes.data.repository.SaveNoteResult
import com.example.localnotes.data.validation.NoteValidator
import com.example.localnotes.gradientBackground
import com.example.localnotes.ui.canvas.DrawingCanvas
import com.example.localnotes.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

// --- Markdown Logic (Fix: Keep markers to avoid OffsetMapping crashes) ---

fun parseMarkdown(text: String, isDark: Boolean): AnnotatedString {
    val accent = if (isDark) CyanAccent else DeepRoyalBlue
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        pushStyle(SpanStyle(color = accent.copy(alpha = 0.6f)))
                        append("**")
                        pop()
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(text.substring(i + 2, end))
                        pop()
                        pushStyle(SpanStyle(color = accent.copy(alpha = 0.6f)))
                        append("**")
                        pop()
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("*", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1 && !text.startsWith("**", i)) {
                        pushStyle(SpanStyle(color = accent.copy(alpha = 0.6f)))
                        append("*")
                        pop()
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(text.substring(i + 1, end))
                        pop()
                        pushStyle(SpanStyle(color = accent.copy(alpha = 0.6f)))
                        append("*")
                        pop()
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("# ", i) && (i == 0 || text[i - 1] == '\n') -> {
                    val end = text.indexOf('\n', i)
                    val contentEnd = if (end == -1) text.length else end
                    pushStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 20.sp, color = accent))
                    append(text.substring(i, contentEnd))
                    pop()
                    i = contentEnd
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

class MarkdownVisualTransformation(private val isDark: Boolean) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(parseMarkdown(text.text, isDark), OffsetMapping.Identity)
    }
}

// --- Main Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasEditorScreen(
    viewModel: NoteViewModel,
    noteId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val existingNote = remember(noteId) { noteId?.let { viewModel.getNote(it) } }

    var titleText by remember { mutableStateOf(existingNote?.title ?: "") }
    val pages = remember {
        mutableStateListOf<NotePage>().apply {
            if (existingNote != null) addAll(existingNote.pages) else add(NotePage())
        }
    }

    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedWidth by remember { mutableFloatStateOf(6f) }
    var selectedTool by remember { mutableStateOf(StrokeTool.PEN) }
    var isScrollLocked by remember { mutableStateOf(false) }

    // Zoom Tech
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

    val saveScope = rememberCoroutineScope()

    fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        placeholder = { Text("Note Title", color = Color.White.copy(alpha = 0.5f)) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val noteToExport = Note(id = noteId ?: -1L, title = titleText.ifBlank { "Untitled" }, pages = pages.toList(), timestamp = existingNote?.timestamp ?: System.currentTimeMillis())
                        PdfExporter.exportNoteToPdf(context, noteToExport)?.let { sharePdf(it) }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = NeonGold)
                    }
                    IconButton(onClick = {
                        saveScope.launch {
                            val result = viewModel.saveOrUpdateNote(id = noteId, title = titleText.ifBlank { NoteValidator.DEFAULT_TITLE }, pages = pages.toList())
                            if (result is SaveNoteResult.Success) onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note", tint = CyanAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .gradientBackground()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CreativeToolbar(
                    selectedTool = selectedTool,
                    onToolChange = { selectedTool = it },
                    selectedColor = selectedColor,
                    onColorChange = { selectedColor = it },
                    selectedWidth = selectedWidth,
                    onWidthChange = { selectedWidth = it },
                    onAddPage = { pages.add(NotePage()) },
                    isLocked = isScrollLocked,
                    onLockToggle = { isScrollLocked = !isScrollLocked },
                    zoomScale = zoomScale,
                    onZoomIn = { zoomScale = (zoomScale * 1.2f).coerceAtMost(5f) },
                    onZoomOut = { zoomScale = (zoomScale / 1.2f).coerceAtLeast(0.5f) },
                    onZoomReset = { zoomScale = 1f; zoomOffset = Offset.Zero }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // --- Stylus & Zoom Fix: Combined Gesture Detection ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .pointerInput(isScrollLocked) {
                            // Only handle zoom gestures at this level if NOT locked, 
                            // or use a logic that doesn't block children.
                            // To fix stylus input, we must ensure child (DrawingCanvas) gets events.
                            if (!isScrollLocked) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5f)
                                    zoomOffset += pan
                                }
                            }
                        }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = zoomScale,
                                scaleY = zoomScale,
                                translationX = zoomOffset.x,
                                translationY = zoomOffset.y
                            ),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        userScrollEnabled = !isScrollLocked
                    ) {
                        itemsIndexed(pages) { index, page ->
                            PageContainer(
                                pageNumber = index + 1,
                                page = page,
                                onPageUpdate = { pages[index] = it },
                                selectedColor = selectedColor,
                                selectedWidth = selectedWidth,
                                selectedTool = selectedTool,
                                isLocked = isScrollLocked // Pass lock state
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CreativeToolbar(
    selectedTool: StrokeTool,
    onToolChange: (StrokeTool) -> Unit,
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    selectedWidth: Float,
    onWidthChange: (Float) -> Unit,
    onAddPage: () -> Unit,
    isLocked: Boolean,
    onLockToggle: () -> Unit,
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomReset: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ToolButton(Icons.Default.Brush, selectedTool == StrokeTool.PEN, { onToolChange(StrokeTool.PEN) }, CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                ToolButton(Icons.Default.CleaningServices, selectedTool == StrokeTool.ERASER, { onToolChange(StrokeTool.ERASER) }, ElectricMagenta)
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onLockToggle) {
                    Icon(if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock", tint = if (isLocked) NeonGold else Color.White.copy(alpha = 0.6f))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onZoomOut) { Icon(Icons.Default.ZoomOut, contentDescription = null, tint = Color.White) }
                Text("${(zoomScale * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = onZoomIn) { Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White) }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(selectedColor).border(2.dp, Color.White, CircleShape).clickable { expanded = !expanded })
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onAddPage) { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "Add Page", tint = NeonGold) }
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(Color.Black, Color.White, Color.Red, Color.Blue, Color.Green, Color.Yellow, CyanAccent, ElectricMagenta).forEach { color ->
                        Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(color).border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape).clickable { onColorChange(color) })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thickness", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Slider(value = selectedWidth, onValueChange = onWidthChange, valueRange = 2f..40f, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, accentColor: Color) {
    val size by animateDpAsState(if (isSelected) 48.dp else 40.dp, label = "")
    Box(modifier = Modifier.size(size).clip(CircleShape).background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent).border(if (isSelected) 2.dp else 0.dp, accentColor, CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = if (isSelected) accentColor else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(if (isSelected) 28.dp else 24.dp))
    }
}

@Composable
fun PageContainer(
    pageNumber: Int, 
    page: NotePage, 
    onPageUpdate: (NotePage) -> Unit, 
    selectedColor: Color, 
    selectedWidth: Float, 
    selectedTool: StrokeTool,
    isLocked: Boolean
) {
    val isDark = page.backgroundColorHex == 0xFF000000L
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(page.backgroundColorHex)).border(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier.fillMaxWidth().background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)).padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PAGE $pageNumber", style = MaterialTheme.typography.labelSmall.copy(color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f), fontWeight = FontWeight.Bold))
            IconButton(onClick = { onPageUpdate(page.copy(backgroundColorHex = if (isDark) 0xFFFFFFFFL else 0xFF000000L)) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = if (isDark) Color.White else Color.Black, modifier = Modifier.size(16.dp))
            }
        }
        OutlinedTextField(
            value = page.content,
            onValueChange = { onPageUpdate(page.copy(content = it)) },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            placeholder = { Text("Write markdown here... (**bold**, *italic*, # header)", color = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f), fontSize = 12.sp) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = if (isDark) Color.White else Color.Black),
            visualTransformation = MarkdownVisualTransformation(isDark),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = if (isDark) Color.White else Color.Black, unfocusedTextColor = if (isDark) Color.White else Color.Black, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
        )
        Box(modifier = Modifier.fillMaxWidth().height(500.dp).background(Color.Transparent)) {
            val currentStrokes = remember(pageNumber, page.strokes.size) { mutableStateListOf<Stroke>().apply { addAll(page.strokes) } }
            LaunchedEffect(currentStrokes.size) { onPageUpdate(page.copy(strokes = currentStrokes.toList())) }
            
            // DrawingCanvas ONLY gets input if we are locked, otherwise LazyColumn might scroll
            DrawingCanvas(
                strokes = currentStrokes, 
                selectedColor = if (selectedColor == Color.Black && isDark) Color.White else selectedColor, 
                selectedWidth = selectedWidth, 
                selectedTool = selectedTool, 
                currentAudioTimeMs = 0L, 
                enabled = isLocked, // Drawing is only enabled when locked
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
