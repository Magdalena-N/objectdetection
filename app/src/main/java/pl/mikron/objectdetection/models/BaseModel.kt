package pl.mikron.objectdetection.models

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import pl.mikron.objectdetection.models.data.TestData
import pl.mikron.objectdetection.utils.awaitSuccess
import pl.mikron.objectdetection.network.result.SingleInferenceResult
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
abstract class BaseModel(private val resources: Resources) : ModelLifecycle {

    abstract val imageWidth: Int

    abstract val imageHeight: Int

    private val conditions = CustomModelDownloadConditions
        .Builder()
        .build()

    protected lateinit var interpreter: Interpreter

    override suspend fun initialize() {
        FirebaseModelDownloader.getInstance()
            .getModel(
                name,
                DownloadType.LATEST_MODEL,
                conditions
            )
            .awaitSuccess { model ->

                val modelFile = model.file
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    Logger.getGlobal().log(Level.SEVERE, "Created interpreter for $name.")
                } else {
                    throw Throwable("Model file does not exist.")
                }
            }
    }

    override suspend fun infer(): List<SingleInferenceResult> =
        suspendCoroutine { continuation ->
            val results = loadSingleInferenceImages()
                .mapIndexed { index, bitmap ->
                    Log.e("TAG", "Inference $index/250")
                    inferSingleImage(bitmap)
                }
                .drop(1)
            continuation.resume(results)
        }

    override suspend fun inferOnBatch(): List<SingleInferenceResult> =
        suspendCoroutine { continuation ->
            val results = loadBatchInferenceImages()
                .map(::inferForBatch)
            continuation.resume(results)
        }

    abstract fun inferSingleImage(bitmap: Bitmap): SingleInferenceResult

    abstract fun inferForBatch(bitmaps: List<Bitmap>): SingleInferenceResult

    private fun loadSingleInferenceImages(): List<Bitmap> =
        TestData.getSingleInferenceDataSet().map(::loadBitmapFromResource)

    private fun loadBatchInferenceImages(): List<List<Bitmap>> =
        TestData
            .getSingleInferenceDataSet()
            .asSequence()
            .map(::loadBitmapFromResource)
            .mapIndexed { index, bitmap -> Pair(index % BATCH_SIZE, bitmap) }
            .groupBy { it.first }
            .map { it.value }
            .map { it.map { pair -> pair.second } }
            .toList()

    private fun loadBitmapFromResource(resourceId: Int): Bitmap {

        val bitmap = BitmapFactory.decodeResource(resources, resourceId)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)

        bitmap.recycle()

        return scaledBitmap
    }

    companion object {
        const val BATCH_SIZE = 4
    }

}
