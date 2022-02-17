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

    protected lateinit var binding: Binding

    abstract fun createBinding(binding: Binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)

        binding.lifecycleOwner = viewLifecycleOwner

        createBinding(binding)

        return binding.root
    }
}
