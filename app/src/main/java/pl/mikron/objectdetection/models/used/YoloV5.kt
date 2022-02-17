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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoloV5 @Inject constructor(
    @ApplicationContext private val context: Context
) : BaseModel(context.resources) {

    override val name: String =
        "YoloV5"

    override val imageWidth: Int
        get() = 640

    override val imageHeight: Int
        get() = 640

    override fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult {

        val inputData = Array(1) { Array(imageWidth) { Array(imageHeight) { FloatArray(3) } } }

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

        return SingleInferenceResult(
            durationInterpreter = interpreter.lastNativeInferenceDurationNanoseconds,
            durationMeasured = timeEnd - timeStart
        )
    }
}
