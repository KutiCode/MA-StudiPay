package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningRecieveBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc.NfcPaymentReceiver

class BeginningRecieveFragment : Fragment() {

    private var _binding: FragmentBeginningRecieveBinding? = null
    private val binding get() = _binding!!
    private lateinit var nfcReader: NfcPaymentReceiver



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginningRecieveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nfcReader = NfcPaymentReceiver(requireActivity())
        nfcReader.amount = amount
        nfcReader.tokenReceivedLiveData.observe(viewLifecycleOwner) { tokenReceived ->
            if (tokenReceived) {
                playSinglePing()
                findNavController().navigate(R.id.fragment_receiving_hold)
            }
        }


        binding.cancelButton.setOnClickListener {
            nfcReader.disableNfcReader()
            findNavController().navigate(R.id.navigation_dashboard)
        }


    }

    override fun onResume() {
        super.onResume()
        nfcReader.enableNfcReader()
    }

    override fun onPause() {
        super.onPause()
        nfcReader.disableNfcReader()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var amount: Double = 0.0
    }

    private fun playSinglePing() {
        val mp: MediaPlayer = MediaPlayer.create(context, R.raw.notification_decorative_01)
        mp.start()
    }


}
