package pl.mikron.objectdetection.models.utils

import com.google.android.gms.tasks.Task
import com.google.firebase.ml.modeldownloader.CustomModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> Task<T>.awaitSuccess(action: (CustomModel) -> Unit): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            val e = task.exception
            if (e == null) {
                if (task.isCanceled)
                {
                    cont.cancel()
                } else {
                    action.invoke(task.result as CustomModel)
                    cont.resume(task.result as T)
                }
            } else {
                cont.resumeWithException(e)
            }
        }
    }
