package com.example.localnotes.data.serialization

import com.example.localnotes.data.model.SerializedPoint
import com.example.localnotes.data.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeCodecTest {

    @Test
    fun encodeDecode_roundTripsStrokes() {
        val strokes = listOf(
            Stroke(
                points = listOf(
                    SerializedPoint(x = 10f, y = 20f, pressure = 0.8f),
                    SerializedPoint(x = 30f, y = 40f, pressure = 1.0f)
                ),
                colorHex = 0xFF1976D2,
                strokeWidth = 8f,
                timestampMs = 1234L
            )
        )

        val encoded = StrokeCodec.encode(strokes)
        val decoded = StrokeCodec.decode(encoded)

        assertEquals(strokes, decoded)
    }

    @Test
    fun decode_blankJson_returnsEmptyList() {
        assertEquals(emptyList<Stroke>(), StrokeCodec.decode(""))
        assertEquals(emptyList<Stroke>(), StrokeCodec.decode("   "))
    }
}
