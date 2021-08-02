package pl.mikron.objectdetection.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.mikron.objectdetection.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {


    private val _testRequested: SingleLiveEvent<Any> =
        SingleLiveEvent()

    val testRequested: LiveData<Any> =
        _testRequested

    fun testClicked() =
        _testRequested.post()
}
