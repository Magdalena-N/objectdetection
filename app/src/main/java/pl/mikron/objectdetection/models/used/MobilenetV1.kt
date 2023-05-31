package pl.mikron.objectdetection.models.used

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.models.BaseModel
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobilenetV1 @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val name: String =
        "MobilenetV1_300x300"

    override val imageWidth: Int
        get() = 300

    override val imageHeight: Int
        get() = 300

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3 * 4)
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

        val timeStart = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()
        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }

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


//        bitmaps.forEach { it.recycle() }

        val timeStart = System.nanoTime()

        interpreter.run(inputData, ByteBuffer.allocate(5904))

        val timeEnd = System.nanoTime()

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }
}
