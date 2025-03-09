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
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel.BeginningRecieveViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel.BeginningRecieveViewModelFactory

class BeginningRecieveFragment : Fragment() {

    private var _binding: FragmentBeginningRecieveBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BeginningRecieveViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginningRecieveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisiere das ViewModel über die Factory
        viewModel = ViewModelProvider(
            this,
            BeginningRecieveViewModelFactory(requireContext(), requireActivity())
        ).get(BeginningRecieveViewModel::class.java)

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }

        // Beobachte Fehler aus dem NFC-Service
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }

        // Beobachte die INIT-Antwort
        viewModel.initResponse.observe(viewLifecycleOwner) { response ->
            Toast.makeText(requireContext(), "INIT-Antwort empfangen: $response", Toast.LENGTH_LONG)
                .show()
            // Hier kannst du z.B. zum nächsten Screen navigieren
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startNfc()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopNfc()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
