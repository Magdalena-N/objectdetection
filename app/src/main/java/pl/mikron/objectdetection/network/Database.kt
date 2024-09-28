package pl.mikron.objectdetection.network

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pl.mikron.objectdetection.network.result.FinalResult
import pl.mikron.objectdetection.network.result.ModelResult
import pl.mikron.objectdetection.network.result.SystemInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Database @Inject constructor() {

    private val database = Firebase.firestore

    fun addInferenceResult(results: List<ModelResult>) =
        database
            .collection("prod")
            .document()
            .set(FinalResult(
                SystemInfo(),
                results,
                FieldValue.serverTimestamp()
            ))
}
