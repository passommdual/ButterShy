package com.example.butterflydetector.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ai.onnxruntime.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.FloatBuffer
import kotlin.math.exp

class ButterflyDetector private constructor(private val context: Context) {

    private var ortSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "ButterflyDetector"
        private const val MODEL_NAME = "butterfly_model.onnx"
        private const val INPUT_SIZE = 224
        private const val DETECTION_THRESHOLD = 0.85f
        @Volatile
        private var INSTANCE: ButterflyDetector? = null

        fun getInstance(context: Context): ButterflyDetector {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ButterflyDetector(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) return@withContext true
            Log.d(TAG, "Initializing butterfly detector...")

            ortEnvironment = OrtEnvironment.getEnvironment()
            val modelFile = copyModelToCache() ?: run {
                Log.e(TAG, "Model files not found in cache")
                return@withContext false
            }

            // Log files in cache for debugging
            val dataFile = File(context.cacheDir, "${MODEL_NAME}.data")
            Log.d(TAG, "Cache directory files: ${context.cacheDir.listFiles()?.joinToString { it.name }}")
            Log.d(TAG, "ONNX file exists: ${modelFile.exists()}, size: ${modelFile.length()}")
            Log.d(TAG, ".data file exists: ${dataFile.exists()}, size: ${dataFile.length()}")

            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            ortSession = ortEnvironment?.createSession(modelFile.absolutePath, sessionOptions)

            isInitialized = true
            Log.d(TAG, "Butterfly detector initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize butterfly detector", e)
            false
        }
    }

    private fun copyModelToCache(): File? {
        return try {
            val onnxFile = File(context.cacheDir, MODEL_NAME)
            val dataFile = File(context.cacheDir, "${MODEL_NAME}.data")

            if (!onnxFile.exists() || onnxFile.length() == 0L) {
                context.assets.open(MODEL_NAME).use { input ->
                    FileOutputStream(onnxFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (!dataFile.exists() || dataFile.length() == 0L) {
                context.assets.open("${MODEL_NAME}.data").use { input ->
                    FileOutputStream(dataFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            onnxFile
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model files", e)
            null
        }
    }

    /**
     * Returns true if a butterfly is detected in the image.
     * Works with any number of labels in the ONNX model.
     */
    suspend fun detectButterfly(bitmap: Bitmap): Boolean = withContext(Dispatchers.Default) {
        if (!isInitialized || ortSession == null) return@withContext false

        try {
            val inputBuffer = preprocessImage(bitmap)
            val inputName = ortSession!!.inputNames.iterator().next()
            val shape = longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
            val tensor = OnnxTensor.createTensor(ortEnvironment, inputBuffer, shape)

            val result = ortSession!!.run(mapOf(inputName to tensor))
            val outputArray = result[0].value as Array<FloatArray>
            val probabilities = softmax(outputArray[0])

            // Log full probabilities
            val probsString = probabilities.mapIndexed { index, prob ->
                "Class$index: ${String.format("%.3f", prob)}"
            }.joinToString(", ")
            Log.d(TAG, "Class probabilities: [$probsString]")

            // Max confidence
            val maxConfidence = probabilities.maxOrNull() ?: 0f
            val isDetected = maxConfidence > DETECTION_THRESHOLD
            Log.d(TAG, "Max confidence: $maxConfidence, Butterfly detected: $isDetected")

            tensor.close()
            result.close()

            isDetected
        } catch (e: Exception) {
            Log.e(TAG, "Detection error", e)
            false
        }
    }

    private fun preprocessImage(bitmap: Bitmap): FloatBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val buffer = FloatBuffer.allocate(3 * INPUT_SIZE * INPUT_SIZE)
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) / 255f
            val g = ((p shr 8) and 0xFF) / 255f
            val b = (p and 0xFF) / 255f
            buffer.put(i, (r - mean[0]) / std[0])
            buffer.put(i + INPUT_SIZE * INPUT_SIZE, (g - mean[1]) / std[1])
            buffer.put(i + 2 * INPUT_SIZE * INPUT_SIZE, (b - mean[2]) / std[2])
        }

        return buffer
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp(it - max) }
        val sum = exps.sum()
        return exps.map { (it / sum).toFloat() }.toFloatArray()
    }

    fun cleanup() {
        try {
            ortSession?.close()
            ortEnvironment?.close()
            isInitialized = false
            File(context.cacheDir, MODEL_NAME).delete()
            Log.d(TAG, "Butterfly detector cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error", e)
        }
    }
}
