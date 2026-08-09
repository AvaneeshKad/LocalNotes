package com.example.localnotes.data.serialization

import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import org.json.JSONArray
import org.json.JSONObject

/**
 * Encodes and decodes stroke lists for Room persistence.
 *
 * Input: [List]<[Stroke]> or JSON string.
 * Output: JSON string or [List]<[Stroke]>.
 * Throws [StrokeCodecException] when JSON is malformed or missing required fields.
 */
object StrokeCodec {

    class StrokeCodecException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

    fun encode(strokes: List<Stroke>): String {
        val root = JSONArray()
        strokes.forEach { stroke ->
            val strokeObject = JSONObject()
                .put("colorHex", stroke.colorHex)
                .put("strokeWidth", stroke.strokeWidth.toDouble())
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
            root.put(strokeObject)
        }
        return root.toString()
    }

    fun decode(json: String): List<Stroke> {
        if (json.isBlank()) {
            return emptyList()
        }

        return try {
            val root = JSONArray(json)
            buildList(root.length()) {
                for (index in 0 until root.length()) {
                    add(parseStroke(root.getJSONObject(index), index))
                }
            }
        } catch (exception: Exception) {
            throw StrokeCodecException("Unable to decode stroke JSON.", exception)
        }
    }

    private fun parseStroke(strokeObject: JSONObject, index: Int): Stroke {
        val pointsArray = strokeObject.optJSONArray("points")
            ?: throw StrokeCodecException("Stroke $index is missing a points array.")

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

        return Stroke(
            points = points,
            colorHex = strokeObject.getLong("colorHex"),
            strokeWidth = strokeObject.getDouble("strokeWidth").toFloat(),
            timestampMs = strokeObject.optLong("timestampMs", 0L)
        )
    }
}
