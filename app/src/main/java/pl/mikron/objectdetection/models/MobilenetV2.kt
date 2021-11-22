package pl.mikron.objectdetection.models

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import javax.inject.Inject

class MobilenetV2 @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val modelName: String =
        "MobilenetV2_192x192"

    override val imageWidth: Int
        get() = 192

    override val imageHeight: Int
        get() = 192

    override fun inferForBatch(bitmaps: List<Bitmap>): SingleInferenceResult {

        interpreter.resizeInput(0, intArrayOf(BATCH_SIZE, imageWidth, imageHeight, 3))
        interpreter.allocateTensors()

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3 * BATCH_SIZE)
        inputData.rewind()

        for (b in 0 until BATCH_SIZE) {
            for (x in 0 until imageWidth) {
                for (y in 0 until imageHeight) {
                    val pixel = bitmaps[b][y, x]
                    inputData.put((pixel shr 16 and 0xFF).toByte())
                    inputData.put((pixel shr 8 and 0xFF).toByte())
                    inputData.put((pixel and 0xFF).toByte())
                }
            }
        }

        bitmaps.forEach { it.recycle() }

        interpreter.run(inputData, ByteBuffer.allocate(5904))

        return SingleInferenceResult()
    }

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3)
        inputData.rewind()

        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val pixel = bitmap[y, x]
                inputData.put((pixel shr 16 and 0xFF).toByte())
                inputData.put((pixel shr 8 and 0xFF).toByte())
                inputData.put((pixel and 0xFF).toByte())
            }
        }

        bitmap.recycle()

        val outputLocations = Array(1) { Array(100) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(100) }
        val outputScores = Array(1) { FloatArray(100) }
        val numDetections = FloatArray(1)

        val outputMap: MutableMap<Int, Any> = HashMap()

        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections

        val timeStart = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }
}
