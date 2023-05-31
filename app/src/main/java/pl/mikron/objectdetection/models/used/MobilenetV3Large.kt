package pl.mikron.objectdetection.models.used

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.models.BaseModel
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.HashMap

@Singleton
class MobilenetV3Large @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val name: String =
        "MobilenetV3_320x320_large"

    override val imageWidth: Int
        get() = 320

    override val imageHeight: Int
        get() = 320

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3)
        inputData.rewind()

        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val pixel = bitmap[y, x]
                pixel.blue
                inputData.put((pixel.red/2 + 127).toByte())
                inputData.put((pixel.green/2 + 127).toByte())
                inputData.put((pixel.blue/2 + 127).toByte())
            }
        }

        bitmap.recycle()

        val outputLocations = Array(1) { Array(100) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(100) }
        val outputSomething = Array(1) { FloatArray(100) }
        val outputConst = FloatArray(1)

        val outputMap: MutableMap<Int, Any> = HashMap()

        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputSomething
        outputMap[3] = outputConst

        val timeStart = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()

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


        bitmaps.forEach { it.recycle() }

        val timeStart = System.nanoTime()

        interpreter.run(inputData, ByteBuffer.allocate(5904))

        val timeEnd = System.nanoTime()

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )

    }
}
