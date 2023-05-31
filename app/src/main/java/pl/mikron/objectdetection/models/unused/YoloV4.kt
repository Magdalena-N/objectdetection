package pl.mikron.objectdetection.models.unused

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.toColor
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.models.BaseModel
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoloV4 @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val name: String =
        "yolo_v4"

    override val imageWidth: Int
        get() = 416

    override val imageHeight: Int
        get() = 416

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3 * Float.SIZE_BYTES)
        inputData.rewind()

        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val pixel = bitmap[y, x].toColor()
                inputData.putFloat(pixel.red() / 255F)
                inputData.putFloat(pixel.green() / 255F)
                inputData.putFloat(pixel.blue() / 255F)
            }
        }

        inputData.rewind()

        bitmap.recycle()

        val outputLocations = Array(1) { Array(2535) { FloatArray(4) } }
        val outputClasses = Array(1) { Array(2535) { FloatArray(80) } }

        val outputMap: MutableMap<Int, Any> = HashMap()

        outputMap[0] = outputLocations
        outputMap[1] = outputClasses

        val timeStart = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(arrayOf(inputData), outputMap)

        val timeEnd = System.nanoTime()

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }

    override fun inferForBatch(bitmaps: List<Bitmap>): SingleInferenceResult {
        TODO("Not yet implemented")
    }
}
