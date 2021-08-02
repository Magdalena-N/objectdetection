package pl.mikron.objectdetection.network.result

data class ModelResult(

    val modelName: String = "",

    val results: List<SingleInferenceResult> = emptyList()
)
