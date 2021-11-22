package pl.mikron.objectdetection.main.home

import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.mikron.objectdetection.R
import pl.mikron.objectdetection.base.BaseFragment
import pl.mikron.objectdetection.databinding.FragmentHomeBinding
import java.time.Duration
import java.util.concurrent.TimeUnit

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

        if (SystemClock.elapsedRealtime() > Duration.ofMinutes(5).toMillis()) {
            viewModel.disableTest()
            showRestartDialog()
        }

        viewModel
            .testRequested
            .observe(viewLifecycleOwner) { navigateToTest() }
    }

    private fun navigateToTest() {
        findNavController()
            .navigate(HomeFragmentDirections.homeToInference())
    }

    private fun showRestartDialog() {
        AlertDialog
            .Builder(requireContext())
            .setTitle("Reboot required!")
            .setMessage("The device has to be fresh after reboot, when the test starts. Please reboot your device and try again.")
            .setPositiveButton("Ok") { d, _ -> d.cancel() }
            .setCancelable(false)
            .show()
    }
}
