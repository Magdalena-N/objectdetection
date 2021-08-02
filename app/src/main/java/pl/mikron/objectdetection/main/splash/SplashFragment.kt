package pl.mikron.objectdetection.main.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pl.mikron.objectdetection.R
import pl.mikron.objectdetection.base.BaseFragment
import pl.mikron.objectdetection.databinding.FragmentSplashBinding

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding, SplashViewModel>() {

    override val viewModel: SplashViewModel
            by viewModels()

    override val layoutRes: Int
        get() = R.layout.fragment_splash

    override fun createBinding(binding: FragmentSplashBinding) {
        binding.also { it.viewModel = viewModel }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeModels()
    }

    private fun initializeModels() {

        viewModel.initializeModel()

        viewModel
            .modelInitialized
            .observe(viewLifecycleOwner) { navigateToHome() }

        viewModel
            .initError
            .observe(viewLifecycleOwner) { showErrorMessage() }
    }

    private fun navigateToHome() {
        findNavController()
            .navigate(SplashFragmentDirections.splashToHome())
    }

    private fun showErrorMessage() {
        Snackbar.make(
            binding.rootView,
            "Failed to initialize the models.",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("Exit") { requireActivity().finishAffinity() }
            .show()
    }
}
