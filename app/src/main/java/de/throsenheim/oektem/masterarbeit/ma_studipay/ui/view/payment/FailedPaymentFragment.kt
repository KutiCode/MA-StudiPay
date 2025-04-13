package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentFailedPaymentBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// Fragment displayed when a payment transaction has failed.
class FailedPaymentFragment : Fragment() {

    // Private binding variable for accessing views in fragment_failed_payment.xml using ViewBinding.
    private var _binding: FragmentFailedPaymentBinding? = null

    // Non-nullable accessor for _binding.
    private val binding get() = _binding!!

    // Inflate the layout and initialize binding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using the generated binding class.
        _binding = FragmentFailedPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Setup UI behavior after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set a click listener on the button to navigate back to the dashboard.
        binding.failedDashboardButton.setOnClickListener {
            // Build fade navigation animation options via NavigationHelper.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Navigate to the dashboard fragment with the specified navigation options.
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)
        }
    }

    // Clear the binding reference when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
