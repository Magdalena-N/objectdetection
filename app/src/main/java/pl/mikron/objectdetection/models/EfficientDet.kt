package pl.mikron.objectdetection.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import javax.inject.Singleton

@Singleton
class EfficientDet(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val modelName: String =
        "EfficientDet_512x512"

    override val imageWidth: Int
        get() = 512

    override val imageHeight: Int
        get() = 512

    override fun inferForBatch(bitmaps: List<Bitmap>): SingleInferenceResult {
        interpreter.resizeInput(0, intArrayOf(BATCH_SIZE, imageWidth, imageHeight, 3))
        interpreter.allocateTensors()
        return SingleInferenceResult()
    }

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = ByteBuffer.allocateDirect(imageWidth * imageHeight * 3)
        inputData.rewind()

        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val pixel = bitmap[y, x]
                pixel.blue
                inputData.put((pixel.red).toByte())
                inputData.put((pixel.green).toByte())
                inputData.put((pixel.blue).toByte())
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

        Log.e("Locations", outputLocations[0].joinToString { it.joinToString() } )
        Log.e("Classes", outputClasses[0].joinToString())
        Log.e("Something", outputSomething[0].joinToString())
        Log.e("Const", outputConst[0].toString())

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }
}