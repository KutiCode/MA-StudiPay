package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentNoWifiBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// Fragment displayed when there is no WiFi connection.
class NoWifiFragment : Fragment() {

    // Nullable binding variable for view binding.
    private var _binding: FragmentNoWifiBinding? = null

    // Non-nullable accessor for the binding.
    private val binding get() = _binding!!

    // Inflate the fragment layout and initialize the binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using the generated binding class.
        _binding = FragmentNoWifiBinding.inflate(inflater, container, false)
        return binding.root  // Return the root view.
    }

    // Setup the UI behavior after the view has been created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up a click listener on the dashboard button in the "no WiFi" layout.
        binding.failedDashboardButton.setOnClickListener {
            // Build fade navigation animation options.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Navigate to the dashboard fragment using the fade animation.
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)
        }
    }

    // Clear the binding when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
