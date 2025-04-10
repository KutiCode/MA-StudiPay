package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningSendingBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.hce.AppHostApduService
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionStatus
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionStatusHolder
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment.BeginningSendingViewModel

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

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)
        AppHostApduService.isTokenTransmissionAllowed = true
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.userInfoSender.text = name
        }

        currentUsername?.let {
            viewModel.loadUserName(requireContext(), it)
        } ?: run {
            binding.userInfoSender.text = "Hallo, Benutzer"
        }

        TransactionStatusHolder.transactionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                TransactionStatus.FINISHED -> {
                    val navOptions = NavigationHelper.buildFadeNavOptions()
                    findNavController().navigate(R.id.senderSuccessFragment, null, navOptions)
                }

                TransactionStatus.RESET -> {

                }
            }
        }


        binding.cancelButton.setOnClickListener {
            AppHostApduService.isTokenTransmissionAllowed = false
            findNavController().navigate(R.id.dashboardFragment)
        }
    }


    override fun onPause() {
        super.onPause()
        AppHostApduService.isTokenTransmissionAllowed = false


    }

    override fun onDestroy() {
        super.onDestroy()
        AppHostApduService.isTokenTransmissionAllowed = false

    }

    override fun onResume() {
        super.onResume()
        AppHostApduService.isTokenTransmissionAllowed = true
    }
}
