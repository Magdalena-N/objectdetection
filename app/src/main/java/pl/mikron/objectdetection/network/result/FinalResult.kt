package pl.mikron.objectdetection.network.result

import com.google.firebase.firestore.FieldValue

data class FinalResult(

    var systemInfo: SystemInfo = SystemInfo(),

    var modelResults: List<ModelResult> = emptyList(),

    var createdAt: FieldValue
)
