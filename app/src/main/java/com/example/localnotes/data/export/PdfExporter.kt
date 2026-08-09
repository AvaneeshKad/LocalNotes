package com.example.localnotes.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.localnotes.data.model.Note
import com.example.localnotes.data.model.StrokeTool
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Path
import android.graphics.Color as AndroidColor

object PdfExporter {

    fun exportNoteToPdf(context: Context, note: Note): File? {
        val pdfDocument = PdfDocument()

        val pageWidth = 595f // A4 width in points
        val pageHeight = 842f // A4 height in points

        note.pages.forEachIndexed { index, page ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), index + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas = pdfPage.canvas

            // 1. Background Color
            val bgColorLong = page.backgroundColorHex
            val bgR = (bgColorLong shr 48).toInt() and 0xFF
            val bgG = (bgColorLong shr 40).toInt() and 0xFF
            val bgB = (bgColorLong shr 32).toInt() and 0xFF
            val bgColor = AndroidColor.rgb(bgR, bgG, bgB)
            canvas.drawColor(bgColor)

            val isDark = (bgR * 0.299 + bgG * 0.587 + bgB * 0.114) < 128

            // 2. Header (First Page Only)
            var y = 60f
            if (index == 0) {
                val titlePaint = Paint().apply {
                    color = if (isDark) AndroidColor.WHITE else AndroidColor.BLACK
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(note.title.ifBlank { "Untitled Note" }, 40f, y, titlePaint)

                val datePaint = Paint().apply {
                    color = if (isDark) AndroidColor.LTGRAY else AndroidColor.DKGRAY
                    textSize = 12f
                    isAntiAlias = true
                }
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
                canvas.drawText(sdf.format(Date(note.timestamp)), 40f, y + 25f, datePaint)
                y += 80f
            }

            // 3. Markdown Text Rendering
            if (page.content.isNotBlank()) {
                val normalPaint = Paint().apply {
                    color = if (isDark) AndroidColor.WHITE else AndroidColor.BLACK
                    textSize = 14f
                    isAntiAlias = true
                }
                
                val lines = page.content.split("\n")
                lines.forEach { line ->
                    if (y > pageHeight - 100f) return@forEach // Basic overflow check
                    
                    if (line.startsWith("# ")) {
                        val headerPaint = Paint(normalPaint).apply {
                            textSize = 20f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            color = AndroidColor.parseColor("#00BCD4") // Cyan Accent for headers
                        }
                        canvas.drawText(line.substring(2), 40f, y, headerPaint)
                        y += 30f
                    } else {
                        renderMarkdownLine(canvas, line, 40f, y, normalPaint)
                        y += 20f
                    }
                }
                y += 20f
            }

            // 4. Stylus Stroke Rendering (Normalized & Scaled)
            val drawingMargin = 40f
            val availableWidth = pageWidth - (drawingMargin * 2)
            val availableHeight = (pageHeight - y - drawingMargin).coerceAtLeast(100f)
            
            val allPoints = page.strokes.flatMap { it.points }
            if (allPoints.isNotEmpty()) {
                val minX = allPoints.minOf { it.x }
                val maxX = allPoints.maxOf { it.x }
                val minY = allPoints.minOf { it.y }
                val maxY = allPoints.maxOf { it.y }
                
                val contentWidth = (maxX - minX).coerceAtLeast(1f)
                val contentHeight = (maxY - minY).coerceAtLeast(1f)
                
                val scale = minOf(availableWidth / contentWidth, availableHeight / contentHeight, 1.2f)

                page.strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val strokePaint = Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            strokeWidth = (stroke.strokeWidth * scale).coerceAtLeast(2f)
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            
                            if (stroke.tool == StrokeTool.ERASER) {
                                color = bgColor
                                strokeWidth *= 1.5f
                            } else {
                                val cL = stroke.colorHex
                                val r = (cL shr 48).toInt() and 0xFF
                                val g = (cL shr 40).toInt() and 0xFF
                                val b = (cL shr 32).toInt() and 0xFF
                                color = AndroidColor.rgb(r, g, b)
                                
                                // High Contrast Correction
                                val brightness = (r * 0.299 + g * 0.587 + b * 0.114)
                                if (isDark && brightness < 50) color = AndroidColor.WHITE
                                else if (!isDark && brightness > 230) color = AndroidColor.BLACK
                            }
                        }

                        val path = Path()
                        val first = stroke.points.first()
                        path.moveTo((first.x - minX) * scale + drawingMargin, (first.y - minY) * scale + y)
                        
                        for (i in 1 until stroke.points.size) {
                            val p0 = stroke.points[i - 1]
                            val p1 = stroke.points[i]
                            path.quadTo(
                                (p0.x - minX) * scale + drawingMargin, (p0.y - minY) * scale + y,
                                ((p0.x + p1.x) / 2f - minX) * scale + drawingMargin, ((p0.y + p1.y) / 2f - minY) * scale + y
                            )
                        }
                        canvas.drawPath(path, strokePaint)
                    }
                }
            }

            pdfDocument.finishPage(pdfPage)
        }

        val fileName = "${note.title.replace(" ", "_").ifBlank { "Note" }}_${System.currentTimeMillis()}.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Simple line-based Markdown renderer for PDF.
     * Handles **bold** and *italic* within a single line.
     */
    private fun renderMarkdownLine(canvas: android.graphics.Canvas, line: String, x: Float, y: Float, basePaint: Paint) {
        var currentX = x
        var i = 0
        while (i < line.length) {
            when {
                line.startsWith("**", i) -> {
                    val end = line.indexOf("**", i + 2)
                    if (end != -1) {
                        val paint = Paint(basePaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                        val segment = line.substring(i + 2, end)
                        canvas.drawText(segment, currentX, y, paint)
                        currentX += paint.measureText(segment)
                        i = end + 2
                    } else {
                        canvas.drawText(line[i].toString(), currentX, y, basePaint)
                        currentX += basePaint.measureText(line[i].toString())
                        i++
                    }
                }
                line.startsWith("*", i) -> {
                    val end = line.indexOf("*", i + 1)
                    if (end != -1) {
                        val paint = Paint(basePaint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC) }
                        val segment = line.substring(i + 1, end)
                        canvas.drawText(segment, currentX, y, paint)
                        currentX += paint.measureText(segment)
                        i = end + 1
                    } else {
                        canvas.drawText(line[i].toString(), currentX, y, basePaint)
                        currentX += basePaint.measureText(line[i].toString())
                        i++
                    }
                }
                else -> {
                    canvas.drawText(line[i].toString(), currentX, y, basePaint)
                    currentX += basePaint.measureText(line[i].toString())
                    i++
                }
            }
        }
    }
}
