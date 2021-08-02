package pl.mikron.objectdetection.models

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.models.data.COCOLabels
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SsdMobilenet @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val modelName: String =
        "ssd_mobilenet"

    override val imageWidth: Int
        get() = 300

    override val imageHeight: Int
        get() = 300

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

        inputData.rewind()

        bitmap.recycle()

        val outputLocations = Array(1) { Array(10) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(10) }
        val outputScores = Array(1) { FloatArray(10) }
        val numDetections = FloatArray(1)

        val outputMap: MutableMap<Int, Any> = HashMap()

        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections

        interpreter.getInputTensor(0)

        val timeStart = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()

        Logger.getGlobal().log(Level.SEVERE, "Number of detections: ${numDetections[0]}")

        for (x in 0 until numDetections[0].toInt()) {
            if (outputClasses[0][x] != 0f) {
                Logger.getGlobal().log(Level.SEVERE, "Class: ${COCOLabels.getLabelFor(outputClasses[0][x])} in box: ${outputLocations[0][x][0]}, ${outputLocations[0][x][1]}, ${outputLocations[0][x][2]}, ${outputLocations[0][x][3]}, confidence: ${outputScores[0][x]}")
            }
        }

        val result = SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )

        Logger.getGlobal().warning(result.toString())

        return result
    }
}
