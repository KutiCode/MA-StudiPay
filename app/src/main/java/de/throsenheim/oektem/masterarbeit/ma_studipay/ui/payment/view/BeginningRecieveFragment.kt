package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningRecieveBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.nfc.NfcPaymentReceiver
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel.BeginningRecieveViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel.BeginningRecieveViewModelFactory

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
}
