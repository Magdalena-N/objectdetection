package pl.mikron.objectdetection.main.inference

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.mikron.objectdetection.models.ModelLifecycle
import pl.mikron.objectdetection.network.Database
import pl.mikron.objectdetection.network.result.ModelResult
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class InferenceViewModel @Inject constructor(
    private val database: Database,
    _models: Set<@JvmSuppressWildcards ModelLifecycle>
) : ViewModel() {

    private val models = _models.toList()

    internal fun performTest() = viewModelScope.launch(Dispatchers.Default + errorHandler) {

        _inProgress.postValue(true)

        val modelResults: MutableList<ModelResult> = mutableListOf()

        repeat(INFERENCES_PER_DATA_SET) {round ->
            val newResults: List<ModelResult> =
                models.map { model ->
                    increaseProgress()
                    ModelResult(model.getName(), round, model.inferOnSingle())
                }
            modelResults.addAll(newResults)
        }

        database.addInferenceResult(modelResults)

        _inProgress.postValue(false)
    }

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Logger.getGlobal().log(Level.SEVERE, error) { error.toString() }
        _inProgress.postValue(false)
    }

    val phaseTotalProgress: LiveData<Int> =
        MutableLiveData(models.count())

    private val _currentProgress: MutableLiveData<Int> =
        MutableLiveData(0)

    val currentProgress: LiveData<Int> =
        _currentProgress

    val phaseProgress: LiveData<Int> =
        _currentProgress.map { it % INFERENCES_PER_DATA_SET }

    private fun increaseProgress() {
        _currentProgress.postValue(currentProgress.value?.plus(1))
    }

    val totalProgress: LiveData<Int> =
        phaseTotalProgress.map { it * INFERENCES_PER_DATA_SET }

    private val _inProgress: MutableLiveData<Boolean> =
        MutableLiveData(false)

    val inProgress: LiveData<Boolean> =
        _inProgress

    companion object {
        const val INFERENCES_PER_DATA_SET = 5
    }
}
