package com.example.butterflydetector.ml

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToInt

class ButterflyDetectorConsistencyTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var detector: ButterflyDetector

    @Before
    fun setup() = runBlocking {
        detector = ButterflyDetector.getInstance(context)
        Assert.assertTrue("Failed to initialize ButterflyDetector", detector.initialize())
    }

    @Test
    fun testButterflyDetectionConsistencyButterfly() = runBlocking {
        val inputStream = context.assets.open("butterfly_sample.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        var positives = 0
        val runs = 100

        repeat(runs) { runIndex ->
            val detected = detector.detectButterfly(bitmap)
            if (detected) positives++

            // Log each detection with run number
            println("Run ${runIndex + 1}: Butterfly detected? $detected")
        }

        val accuracy = (positives.toDouble() / runs) * 100
        println("Butterfly detection accuracy: ${accuracy.roundToInt()}% ($positives/$runs)")

        Assert.assertTrue("Accuracy too low: $accuracy%", positives >= 85)
    }


    @Test
    fun testButterflyDetectionConsistencyNoButterfly() = runBlocking {
        val inputStream = context.assets.open("butterfly_no_butterfly_sample.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        var positives = 0
        val runs = 100

        repeat(runs) { runIndex ->
            val detected = detector.detectButterfly(bitmap)
            if (detected) positives++

            // Log each detection with run number
            println("Run ${runIndex + 1}: Butterfly detected? $detected")
        }

        val accuracy = (positives.toDouble() / runs) * 100
        println("Butterfly detection accuracy: ${accuracy.roundToInt()}% ($positives/$runs)")

        Assert.assertTrue("Accuracy too low: $accuracy%", positives < 85)
    }
}
