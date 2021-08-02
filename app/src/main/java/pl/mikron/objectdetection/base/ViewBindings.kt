package pl.mikron.objectdetection.base

import android.view.View
import android.view.View.*
import androidx.databinding.BindingAdapter

@BindingAdapter(value = ["visible"])
fun setVisible(view: View, visible: Boolean) {

    view.visibility = when (visible) {
        true -> VISIBLE
        false -> GONE
    }
}
