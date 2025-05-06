package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentReceivingHoldBinding

// ReceivingHoldFragment: A simple fragment for the receiving hold screen.
// It currently handles inflating its layout using view binding.
class ReceivingHoldFragment : Fragment() {

    // Private nullable binding variable to hold the instance of the generated binding class.
    private var _binding: FragmentReceivingHoldBinding? = null

    // Non-nullable accessor that throws an exception if _binding is not initialized.
    private val binding get() = _binding!!

    // Inflate the layout using the binding class and return the root view.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding.
        _binding = FragmentReceivingHoldBinding.inflate(inflater, container, false)
        // Return the root view of the inflated layout.
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
