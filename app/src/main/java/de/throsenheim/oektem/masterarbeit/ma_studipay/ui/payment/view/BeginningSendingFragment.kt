package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningSendingBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.hce.PaymentHCEService
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.DashboardUiState
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.viewmodel.BeginningSendingViewModel

class BeginningSendingFragment : Fragment() {

    private var _binding: FragmentBeginningSendingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BeginningSendingViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginningSendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        PaymentHCEService.isActive = true
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)

        // Observer für den Benutzernamen einrichten
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.userInfoSender.text = name
        }

        // Wenn ein Username vorhanden ist, lade den Namen über das ViewModel,
        // andernfalls setze einen Standardtext.
        currentUsername?.let {
            viewModel.loadUserName(requireContext(), it)
        } ?: run {
            binding.userInfoSender.text = "Hallo, Benutzer"
        }

        // Optional: Weitere UI-Anpassungen (z.B. Text anpassen, Bilder setzen etc.)

        // Beim Klick auf den Abbrechen-Button wird zum Dashboard navigiert.
        binding.cancelButton.setOnClickListener {
            PaymentHCEService.isActive = false
            findNavController().navigate(R.id.navigation_dashboard)
        }
    }

    /**
     * Diese Methode liefert den aktuellen Benutzernamen.
     * Ersetze den Rückgabewert mit echter Logik (z.B. aus SharedPreferences oder einem Repository).
     */

    override fun onPause() {
        super.onPause()
        // Wenn das Fragment nicht mehr im Vordergrund ist, deaktiviere HCE.
        PaymentHCEService.isActive = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
