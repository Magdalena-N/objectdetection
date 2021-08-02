package pl.mikron.objectdetection.main.home

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.mikron.objectdetection.R
import pl.mikron.objectdetection.base.BaseFragment
import pl.mikron.objectdetection.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel
            by viewModels()

    override val layoutRes: Int
        get() = R.layout.fragment_home

    override fun createBinding(binding: FragmentHomeBinding) {
        binding
            .also { it.viewModel = viewModel }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                requireActivity().finish()
            }

        viewModel
            .testRequested
            .observe(viewLifecycleOwner) { navigateToTest() }
    }

    private fun navigateToTest() {
        findNavController()
            .navigate(HomeFragmentDirections.homeToInference())
    }
}
