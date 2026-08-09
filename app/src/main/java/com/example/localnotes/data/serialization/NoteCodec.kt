package com.example.localnotes.data.serialization

import com.example.localnotes.data.model.NotePage
import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import com.example.localnotes.data.model.StrokeTool
import org.json.JSONArray
import org.json.JSONObject

/**
 * Encodes and decodes note pages for Room persistence.
 */
object NoteCodec {

    class NoteCodecException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

    fun encodePages(pages: List<NotePage>): String {
        val root = JSONArray()
        pages.forEach { page ->
            val pageObject = JSONObject()
            pageObject.put("content", page.content)
            pageObject.put("backgroundColorHex", page.backgroundColorHex)
            
            val strokesArray = JSONArray()
            page.strokes.forEach { stroke ->
                val strokeObject = JSONObject()
                    .put("colorHex", stroke.colorHex)
                    .put("strokeWidth", stroke.strokeWidth.toDouble())
                    .put("tool", stroke.tool.name)
                    .put("timestampMs", stroke.timestampMs)

                val pointsArray = JSONArray()
                stroke.points.forEach { point ->
                    pointsArray.put(
                        JSONObject()
                            .put("x", point.x.toDouble())
                            .put("y", point.y.toDouble())
                            .put("pressure", point.pressure.toDouble())
                    )
                }
                strokeObject.put("points", pointsArray)
                strokesArray.put(strokeObject)
            }
            pageObject.put("strokes", strokesArray)
            root.put(pageObject)
        }
        return root.toString()
    }

    fun decodePages(json: String): List<NotePage> {
        if (json.isBlank()) {
            return listOf(NotePage())
        }

        return try {
            val root = JSONArray(json)
            buildList(root.length()) {
                for (index in 0 until root.length()) {
                    add(parsePage(root.getJSONObject(index)))
                }
            }
        } catch (exception: Exception) {
            throw NoteCodecException("Unable to decode note JSON.", exception)
        }
    }

    private fun parsePage(pageObject: JSONObject): NotePage {
        val content = pageObject.optString("content", "")
        val backgroundColorHex = pageObject.optLong("backgroundColorHex", 0xFFFFFFFF)
        val strokesArray = pageObject.optJSONArray("strokes") ?: JSONArray()
        
        val strokes = buildList(strokesArray.length()) {
            for (index in 0 until strokesArray.length()) {
                add(parseStroke(strokesArray.getJSONObject(index), index))
            }
        }
        
        return NotePage(strokes = strokes, content = content, backgroundColorHex = backgroundColorHex)
    }

    private fun parseStroke(strokeObject: JSONObject, index: Int): Stroke {
        val pointsArray = strokeObject.optJSONArray("points")
            ?: throw NoteCodecException("Stroke $index is missing a points array.")

        val points = buildList(pointsArray.length()) {
            for (pointIndex in 0 until pointsArray.length()) {
                val pointObject = pointsArray.getJSONObject(pointIndex)
                add(
                    SerializedPoint(
                        x = pointObject.getDouble("x").toFloat(),
                        y = pointObject.getDouble("y").toFloat(),
                        pressure = pointObject.optDouble("pressure", 1.0).toFloat()
                    )
                )
            }
        }

        val toolName = strokeObject.optString("tool", StrokeTool.PEN.name)
        val tool = try { StrokeTool.valueOf(toolName) } catch (e: Exception) { StrokeTool.PEN }

        return Stroke(
            points = points,
            colorHex = strokeObject.getLong("colorHex"),
            strokeWidth = strokeObject.getDouble("strokeWidth").toFloat(),
            tool = tool,
            timestampMs = strokeObject.optLong("timestampMs", 0L)
        )
    }
}
