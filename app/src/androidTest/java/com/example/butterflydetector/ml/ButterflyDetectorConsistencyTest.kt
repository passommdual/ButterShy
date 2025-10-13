package com.example.butterflydetector.ml

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.math.roundToInt

class ButterflyDetectorConsistencyTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var detector: ButterflyDetector

    @Before
    fun setup() = runBlocking {
        detector = ButterflyDetector.getInstance(context)
        Assert.assertTrue("Failed to initialize ButterflyDetector", detector.initialize())
    }

    private fun writeResultsToFile(filename: String, content: String) {
        // Use instrumentation targetContext for writing to device storage
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, filename)
        file.writeText(content)
    }

    @Test
    fun testButterflyDetectionConsistencyButterfly() = runBlocking {
        val inputStream = context.assets.open("butterfly_sample.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val results = StringBuilder()
        var positives = 0
        val runs = 100

        repeat(runs) { runIndex ->
            val detected = detector.detectButterfly(bitmap)
            if (detected) positives++
            results.append("Run ${runIndex + 1}: Butterfly detected? $detected\n")
        }

        val accuracy = (positives.toDouble() / runs) * 100
        results.append("Butterfly detection accuracy: ${accuracy.roundToInt()}% ($positives/$runs)\n")

        writeResultsToFile("butterfly_positive_results.txt", results.toString())
        Assert.assertTrue("Accuracy too low: $accuracy%", positives >= 85)
    }

    @Test
    fun testButterflyDetectionConsistencyNoButterfly() = runBlocking {
        val inputStream = context.assets.open("butterfly_no_butterfly_sample.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val results = StringBuilder()
        var positives = 0
        val runs = 100

        repeat(runs) { runIndex ->
            val detected = detector.detectButterfly(bitmap)
            if (detected) positives++
            results.append("Run ${runIndex + 1}: Butterfly detected? $detected\n")
        }

        val accuracy = (positives.toDouble() / runs) * 100
        results.append("Butterfly detection accuracy: ${accuracy.roundToInt()}% ($positives/$runs)\n")

        writeResultsToFile("butterfly_negative_results.txt", results.toString())
        Assert.assertTrue("Too many false positives: $accuracy%", positives < 85)
    }
}
