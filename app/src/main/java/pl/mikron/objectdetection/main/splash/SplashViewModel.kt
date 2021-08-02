package pl.mikron.objectdetection.main.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.mikron.objectdetection.models.ModelLifecycle
import pl.mikron.objectdetection.utils.SingleLiveEvent
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    _models: Set<@JvmSuppressWildcards ModelLifecycle>
    ) : ViewModel() {

    val models = _models.toList()

    private val _modelInitialized: SingleLiveEvent<Unit> =
        SingleLiveEvent()

    val modelInitialized: LiveData<Unit> =
        _modelInitialized

    private val _initError: SingleLiveEvent<Unit> =
        SingleLiveEvent()

    val initError: LiveData<Unit> =
        _initError

    internal fun initializeModel() =
        viewModelScope.launch(Dispatchers.Default + errorHandler) {

            models.forEach { it.initModel() }

            _modelInitialized.post()
        }

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Logger.getGlobal().log(Level.SEVERE, error) { error.toString() }
        _initError.post()
    }
}
