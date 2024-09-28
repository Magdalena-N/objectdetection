package pl.mikron.objectdetection.models

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
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

    val conditions = CustomModelDownloadConditions
        .Builder()
        .build()

    protected lateinit var interpreterCPU1: Interpreter
    protected lateinit var interpreterCPU2: Interpreter
    protected lateinit var interpreterCPU4: Interpreter
    protected lateinit var interpreterCPU5: Interpreter
    protected lateinit var interpreterCPU6: Interpreter
    protected lateinit var interpreterGPU: Interpreter


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
//                    val options = Interpreter.Options().setNumThreads(1)
//                    val compatList = CompatibilityList()
//                    val delegateOptions = compatList.bestOptionsForThisDevice
//                    val options = Interpreter.Options().addDelegate(GpuDelegate(delegateOptions))
                    interpreterCPU1 = Interpreter(modelFile, Interpreter.Options().setNumThreads(1))
                    interpreterCPU2 = Interpreter(modelFile, Interpreter.Options().setNumThreads(2))
                    interpreterCPU4 = Interpreter(modelFile, Interpreter.Options().setNumThreads(4))
                    interpreterCPU5 = Interpreter(modelFile, Interpreter.Options().setNumThreads(5))
                    interpreterCPU6 = Interpreter(modelFile, Interpreter.Options().setNumThreads(6))
                    interpreterGPU = Interpreter(modelFile, Interpreter.Options().addDelegate(GpuDelegate()))
                    Logger.getGlobal().log(Level.SEVERE, "Created interpreter for $name.")
                } else {
                    throw Throwable("Model file does not exist.")
                }
            }
    }

    override suspend fun infer(delegateName: String): List<SingleInferenceResult> =
        suspendCoroutine { continuation ->
            val results = loadSingleInferenceImages()
                .mapIndexed { index, bitmap ->
                    Log.e("TAG", "Inference $index/250")
                    inferSingleImage(bitmap, delegateName)
                }
                .drop(1)
            continuation.resume(results)
        }

    fun selectInterpreter(delegateName: String): Interpreter {
        return when (delegateName) {
            "CPU1" -> interpreterCPU1
            "CPU2" -> interpreterCPU2
            "CPU4" -> interpreterCPU4
            "CPU5" -> interpreterCPU5
            "CPU6" -> interpreterCPU6
            "GPU" -> interpreterGPU
            else -> throw IllegalArgumentException("Unknown delegate: $delegateName")
        }
    }

    abstract fun inferSingleImage(bitmap: Bitmap, delegateName: String): SingleInferenceResult

    private fun loadSingleInferenceImages(): List<Bitmap> =
        TestData.getSingleInferenceDataSet().map(::loadBitmapFromResource)

    private fun loadBitmapFromResource(resourceId: Int): Bitmap {

        val bitmap = BitmapFactory.decodeResource(resources, resourceId)

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)

        bitmap.recycle()

        return scaledBitmap
    }
}
