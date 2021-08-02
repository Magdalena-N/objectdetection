package pl.mikron.objectdetection.main.inference

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

        val modelResults: List<ModelResult> =
            models.mapIndexed { index, model ->
                _currentProgress.postValue(index)
                ModelResult(model.getName(), model.inferOnSingle())
            }

        // TODO: Enable save again.
//        database.addInferenceResult(modelResults)

        _inProgress.postValue(false)
    }

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Logger.getGlobal().log(Level.SEVERE, error) { error.toString() }
        _inProgress.postValue(false)
    }

    val totalProgress: LiveData<Int> =
        MutableLiveData(models.count())

    private val _currentProgress: MutableLiveData<Int> =
        MutableLiveData(0)

    val currentProgress: LiveData<Int> =
        _currentProgress

    private val _inProgress: MutableLiveData<Boolean> =
        MutableLiveData(false)

    val inProgress: LiveData<Boolean> =
        _inProgress
}
