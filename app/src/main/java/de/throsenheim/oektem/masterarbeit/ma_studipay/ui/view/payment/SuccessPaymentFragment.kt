package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentSuccessPaymentBinding

class SuccessPaymentFragment : Fragment() {

    private var _binding: FragmentSuccessPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuccessPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val amount = arguments?.getDouble("amount", 0.0) ?: 0.0
        val sender = arguments?.getString("sender") ?: "Unbekannt"
        view.findViewById<TextView>(R.id.received_amount).text = "$amount €"
        view.findViewById<TextView>(R.id.sender_text_view).text = "$sender, hat dir Geld gesendet"
        binding.successDashboardButton.setOnClickListener {
            // Navigate to the dashboard
            findNavController().navigate(R.id.dashboardFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
