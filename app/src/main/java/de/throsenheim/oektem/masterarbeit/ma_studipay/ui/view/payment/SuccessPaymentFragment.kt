package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentSuccessPaymentBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// Fragment that displays a success message after a payment transaction.
class SuccessPaymentFragment : Fragment() {

    // ViewBinding instance for accessing views in fragment_success_payment.xml.
    private var _binding: FragmentSuccessPaymentBinding? = null

    // Non-null property accessor for the binding.
    private val binding get() = _binding!!

    // Inflate the layout for this fragment using view binding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuccessPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView; initializes UI elements and sets up button behavior.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve payment amount and sender information from the fragment arguments.
        val amount = arguments?.getDouble("amount", 0.0) ?: 0.0
        val sender = arguments?.getString("sender") ?: "Unbekannt"  // "Unknown" if not provided

        // Display the received amount appended with the euro symbol.
        view.findViewById<TextView>(R.id.received_amount).text = "$amount €"
        // Display the sender's information in a descriptive text.
        view.findViewById<TextView>(R.id.sender_text_view).text = "$sender, hat dir Geld gesendet"

        playSinglePing()
        // Set up the button to navigate back to the dashboard.
        binding.successDashboardButton.setOnClickListener {
            // Build fade animation options for a smooth transition.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Navigate to the dashboard fragment.
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)
        }
    }

    // Clean up binding when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Play a single notification sound.
    private fun playSinglePing() {
        val mp: MediaPlayer = MediaPlayer.create(context, R.raw.game_success_alert)
        mp.start()
    }
}
