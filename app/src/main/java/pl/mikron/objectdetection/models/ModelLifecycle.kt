package pl.mikron.objectdetection.models

import pl.mikron.objectdetection.network.result.SingleInferenceResult

interface ModelLifecycle {

    fun getName(): String

    suspend fun initModel()

    suspend fun inferOnSingle() : List<SingleInferenceResult>
}
