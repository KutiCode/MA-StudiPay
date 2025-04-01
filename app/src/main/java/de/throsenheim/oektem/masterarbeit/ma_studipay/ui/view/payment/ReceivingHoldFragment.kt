package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentReceivingHoldBinding

class ReceivingHoldFragment : Fragment() {
    private var _binding: FragmentReceivingHoldBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceivingHoldBinding.inflate(inflater, container, false)
        return binding.root
    }


}