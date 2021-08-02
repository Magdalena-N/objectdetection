package pl.mikron.objectdetection.models

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
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
