package pl.mikron.objectdetection.models

import pl.mikron.objectdetection.network.result.SingleInferenceResult

interface ModelLifecycle {

    val name: String

    suspend fun initialize()

    suspend fun infer() : List<SingleInferenceResult>

    suspend fun inferOnBatch() : List<SingleInferenceResult>
}
