package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningRecieveBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.nfc.NfcPaymentReceiver
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// BeginningReceiveFragment manages the receiving process of a payment through NFC.
// It initializes the NFC payment receiver, observes the token, and navigates accordingly.
class BeginningReceiveFragment : Fragment() {

    // ViewBinding instance to safely access UI elements.
    private var _binding: FragmentBeginningRecieveBinding? = null

    // Non-nullable accessor for binding.
    private val binding get() = _binding!!

    // Instance of NfcPaymentReceiver that handles NFC token reception.
    private lateinit var nfcReader: NfcPaymentReceiver

    // Inflate the layout for this fragment using the generated binding class.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginningRecieveBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called after the view is created; initializes the NFC reader, sets observers, and configures UI listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create an instance of NfcPaymentReceiver with the current activity context.
        nfcReader = NfcPaymentReceiver(requireActivity())
        // Set the amount to be received (stored in a companion object variable).
        nfcReader.amount = amount

        // Observe the LiveData flag to check if an NFC token has been received.
        nfcReader.tokenReceivedLiveData.observe(viewLifecycleOwner) { tokenReceived ->
            // Navigate to the Receiving Hold screen if a token is successfully received.
            if (tokenReceived) {
                findNavController().navigate(R.id.receivingHoldFragment)
            }
        }

        // Set a click listener on the cancel button.
        binding.cancelButton.setOnClickListener {
            // Disable NFC reader before navigating away.
            nfcReader.disableNfcReader()
            // Build navigation options using a fade animation.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Navigate back to the dashboard fragment.
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)
        }
    }

    // Enable the NFC reader when the fragment resumes.
    override fun onResume() {
        super.onResume()
        nfcReader.enableNfcReader()
    }

    // Disable the NFC reader when the fragment is paused.
    override fun onPause() {
        super.onPause()
        nfcReader.disableNfcReader()
    }

    // Clear the binding reference when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Companion object to store static data (in this case, the amount to be received).
    companion object {
        var amount: Double = 0.0
    }
}
