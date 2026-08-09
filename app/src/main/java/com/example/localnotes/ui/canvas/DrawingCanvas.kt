package com.example.localnotes.ui.canvas

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.model.StrokeTool

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawingCanvas(
    strokes: MutableList<Stroke>,
    selectedColor: Color = Color.Black,
    selectedWidth: Float = 8f,
    selectedTool: StrokeTool = StrokeTool.PEN,
    currentAudioTimeMs: Long = 0L,
    enabled: Boolean = true, // Control if drawing is allowed
    modifier: Modifier = Modifier
) {
    val currentPoints = remember { mutableStateListOf<SerializedPoint>() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .pointerInteropFilter { event ->
                if (!enabled) return@pointerInteropFilter false
                
                val point = SerializedPoint(
                    x = event.x,
                    y = event.y,
                    pressure = if (event.pressure > 0) event.pressure else 1.0f
                )

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPoints.clear()
                        currentPoints.add(point)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPoints.add(point)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (currentPoints.isNotEmpty()) {
                            strokes.add(
                                Stroke(
                                    points = currentPoints.toList(),
                                    colorHex = if (selectedTool == StrokeTool.ERASER) Color.Transparent.value.toLong() else selectedColor.value.toLong(),
                                    strokeWidth = selectedWidth,
                                    tool = selectedTool,
                                    timestampMs = currentAudioTimeMs
                                )
                            )
                            currentPoints.clear()
                        }
                        true
                    }
                    else -> false
                }
            }
    ) {
        strokes.forEach { stroke ->
            if (stroke.points.size > 1) {
                val path = Path().apply {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    for (i in 1 until stroke.points.size) {
                        val p0 = stroke.points[i - 1]
                        val p1 = stroke.points[i]
                        quadraticTo(
                            p0.x, p0.y,
                            (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f
                        )
                    }
                }
                
                val isEraser = stroke.tool == StrokeTool.ERASER
                drawPath(
                    path = path,
                    color = if (isEraser) Color.Transparent else Color(stroke.colorHex.toULong()),
                    style = DrawStroke(width = stroke.strokeWidth),
                    blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
                )
            }
        }

        if (currentPoints.size > 1) {
            val livePath = Path().apply {
                moveTo(currentPoints.first().x, currentPoints.first().y)
                for (i in 1 until currentPoints.size) {
                    val p0 = currentPoints[i - 1]
                    val p1 = currentPoints[i]
                    quadraticTo(
                        p0.x, p0.y,
                        (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f
                    )
                }
            }
            
            val isEraser = selectedTool == StrokeTool.ERASER
            drawPath(
                path = livePath,
                color = if (isEraser) Color.Transparent else selectedColor,
                style = DrawStroke(width = selectedWidth),
                blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
            )
        }
    }
}
