package pl.mikron.objectdetection.main.inference

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import pl.mikron.objectdetection.R
import pl.mikron.objectdetection.base.BaseFragment
import pl.mikron.objectdetection.databinding.FragmentInferenceBinding

@AndroidEntryPoint
class InferenceFragment : BaseFragment<FragmentInferenceBinding, InferenceViewModel>() {

    override val viewModel: InferenceViewModel
        by viewModels()

    override val layoutRes: Int
        get() = R.layout.fragment_inference

    override fun createBinding(binding: FragmentInferenceBinding) {
        binding
            .also { it.viewModel = viewModel }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.performTest()
    }
}
