package pl.mikron.objectdetection.network.result

data class MultipleInferenceResult(

    var modelName: String = "",
    var duration: Long = 0L,
    var latency: Int = 0
)
