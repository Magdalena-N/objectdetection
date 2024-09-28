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

    override fun inferSingleImage(bitmap: Bitmap, delegateName: String): SingleInferenceResult {

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

        val interpreter = selectInterpreter(delegateName)
        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }
}
