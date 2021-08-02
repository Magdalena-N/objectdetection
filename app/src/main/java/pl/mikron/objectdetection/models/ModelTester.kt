package pl.mikron.objectdetection.models

import pl.mikron.objectdetection.network.Database
import pl.mikron.objectdetection.network.result.ModelResult
import java.util.logging.Level
import java.util.logging.Logger

class ModelTester(private val models: List<ModelLifecycle>, private val database: Database) {

    suspend fun makeSingleInferenceTest() {

        val modelResults: List<ModelResult> =
            models.map { ModelResult(it.getName(), it.inferOnSingle()) }

        database.addInferenceResult(modelResults)
    }
}
