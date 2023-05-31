package pl.mikron.objectdetection.main.inference

import android.os.Build
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.mikron.objectdetection.models.ModelLifecycle
import pl.mikron.objectdetection.network.Database
import pl.mikron.objectdetection.network.result.ModelResult
import pl.mikron.objectdetection.utils.SingleLiveEvent
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class InferenceViewModel @Inject constructor(
    private val database: Database,
    _models: MutableSet<ModelLifecycle>
) : ViewModel() {

    private val models = _models.toList()

    internal fun performTest() = viewModelScope.launch(Dispatchers.Default + errorHandler) {

        val modelResults: MutableList<ModelResult> = mutableListOf()

        repeat(INFERENCES_PER_DATA_SET) {round ->
            val newResults: List<ModelResult> =
                models.map { model ->
                    increaseProgress()
                    ModelResult(model.name, round, model.inferOnBatch())
                }
            modelResults.addAll(newResults)
        }

        database.addInferenceResult(modelResults)

        _finished.post()
    }

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Logger.getGlobal().log(Level.SEVERE, error) { error.toString() }
    }

    val systemData: LiveData<String> = MutableLiveData(
        listOf(
            Build.MANUFACTURER,
            Build.MODEL,
            Build.HARDWARE,
            Build.BOARD,
            Build.VERSION.SDK_INT
        ).joinToString(separator = "\n")
    )

    private val phase =  models.count()
    val phaseTotalProgress: LiveData<Int> =
        MutableLiveData(phase)

    private val _currentProgress: MutableLiveData<Int> =
        MutableLiveData(0)

    val currentProgress: LiveData<Int> =
        _currentProgress

    val phaseProgress: LiveData<Int> =
        _currentProgress.map { InferenceProgress(total, phase, it).get() }

    private fun increaseProgress() {
        _currentProgress.postValue(currentProgress.value?.plus(1))
    }

    private val total = phase * INFERENCES_PER_DATA_SET
    val totalProgress: LiveData<Int> =
        MutableLiveData(total)

    private val _finished: SingleLiveEvent<Unit> =
        SingleLiveEvent()

    internal val finished: LiveData<Unit> =
        _finished

    companion object {
        const val INFERENCES_PER_DATA_SET = 1
    }
}
