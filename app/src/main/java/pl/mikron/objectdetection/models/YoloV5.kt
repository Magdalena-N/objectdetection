package pl.mikron.objectdetection.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import dagger.hilt.android.qualifiers.ApplicationContext
import pl.mikron.objectdetection.models.data.COCOLabels
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.nio.ByteBuffer
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoloV5 @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val modelName: String =
        "YoloV5"

    override val imageWidth: Int
        get() = 320

    override val imageHeight: Int
        get() = 320

    override fun inferForBatch(bitmaps: List<Bitmap>): SingleInferenceResult {
        return SingleInferenceResult()
    }

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = Array(1) { Array(320) { Array(320) {FloatArray(3)} } }

        for (x in 0 until imageWidth) {
            for (y in 0 until imageHeight) {
                val pixel = bitmap[y, x]
                inputData[0][x][y][0] = pixel.red / 255F
                inputData[0][x][y][0] = pixel.green / 255F
                inputData[0][x][y][0] = pixel.blue / 255F
            }
        }

        bitmap.recycle()

        val outputLocations = Array(1) { Array(6300) { FloatArray(85) } }

        val timeStart = System.nanoTime()

        interpreter.run(inputData, outputLocations)

        val timeEnd = System.nanoTime()

        for (l in 0..6299) {
            for (c in 0..84) {
                if (outputLocations[0][l][c] > 0.9) {
                    Log.e(modelName, "${COCOLabels.getLabelFor(c.toFloat())} ${outputLocations[0][l][c]}")
                }
            }
        }

        Log.e(
            modelName,
            outputLocations[0].joinToString { it.joinToString() }
        )

        val result = SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )

        Logger.getGlobal().warning(result.toString())

        return result
    }
}
