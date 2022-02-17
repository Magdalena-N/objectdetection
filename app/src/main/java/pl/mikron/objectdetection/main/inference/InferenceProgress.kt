package pl.mikron.objectdetection.main.inference

class InferenceProgress(
    private val total: Int,
    private val phase: Int,
    private val current: Int
) {

    fun get() =
        when (current == total) {
            true -> phase
            false -> current % phase
        }
}
