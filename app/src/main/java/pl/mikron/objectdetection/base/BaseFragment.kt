package pl.mikron.objectdetection.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel

abstract class BaseFragment<Binding: ViewDataBinding, VM: ViewModel> : Fragment() {

    abstract val viewModel: VM

    @get:LayoutRes
    abstract val layoutRes: Int

    private var _binding: Binding? = null

    protected val binding: Binding
        get() = _binding ?: throw Throwable("Binding not available.")

    abstract fun createBinding(binding: Binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)

        binding.lifecycleOwner = this

        createBinding(binding)

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null

        super.onDestroyView()
    }

}
