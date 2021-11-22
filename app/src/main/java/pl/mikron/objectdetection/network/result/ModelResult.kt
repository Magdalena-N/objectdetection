package pl.mikron.objectdetection.network.result

data class ModelResult(

    val modelName: String = "",

    val round: Int = 0,

    val results: List<SingleInferenceResult> = emptyList()
)
