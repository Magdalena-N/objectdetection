package pl.mikron.objectdetection.network.result

data class FinalResult(

    var systemInfo: SystemInfo = SystemInfo(),

    var modelResults: List<ModelResult> = emptyList()
)
