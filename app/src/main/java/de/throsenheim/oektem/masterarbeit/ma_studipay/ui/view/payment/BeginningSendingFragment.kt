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

// Fragment that marks the beginning of the sending process.
// It loads and displays the sender's information and listens for transaction status changes.
class BeginningSendingFragment : Fragment() {

    // ViewBinding instance for safe access to the layout views.
    private var _binding: FragmentBeginningSendingBinding? = null

    // Non-nullable accessor for binding; will throw if accessed when _binding is null.
    private val binding get() = _binding!!

    // ViewModel instance for handling the sender's data.
    private val viewModel: BeginningSendingViewModel by viewModels()

    // Inflate the layout for this fragment using view binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeginningSendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Set up UI components and observers after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the shared preferences and retrieve the current username.
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)

        // Enable token transmission for payment using the AppHostApduService.
        AppHostApduService.isTokenTransmissionAllowed = true

        // Observe the userName LiveData from the ViewModel; update the corresponding TextView when changed.
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.userInfoSender.text = name
        }

        // If a current username exists, load the user's full name into the ViewModel.
        currentUsername?.let {
            viewModel.loadUserName(requireContext(), it)
        } ?: run {
            // Fallback: display a default greeting if no username is found.
            binding.userInfoSender.text = "Hallo, Benutzer"
        }

        // Observe the transactionStatus LiveData to react to changes in transaction status.
        TransactionStatusHolder.transactionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                // If the transaction is finished, navigate to the sender success screen.
                TransactionStatus.FINISHED -> {
                    val navOptions = NavigationHelper.buildFadeNavOptions()
                    findNavController().navigate(R.id.senderSuccessFragment, null, navOptions)
                }
                // Handle RESET status if needed (currently no action is taken).
                TransactionStatus.RESET -> {
                    // You could add additional logic here if needed.
                }
            }
        }

        // Configure the cancel button to stop token transmission and navigate back to the dashboard.
        binding.cancelButton.setOnClickListener {
            // Disable token transmission when cancelling.
            AppHostApduService.isTokenTransmissionAllowed = false
            // Navigate back to the dashboard fragment.
            findNavController().navigate(R.id.dashboardFragment)
        }
    }

    // When the fragment is paused, disable token transmission.
    override fun onPause() {
        super.onPause()
        AppHostApduService.isTokenTransmissionAllowed = false
    }

    // When the fragment is destroyed, ensure token transmission is disabled.
    override fun onDestroy() {
        super.onDestroy()
        AppHostApduService.isTokenTransmissionAllowed = false
    }

    // When the fragment is resumed, re-enable token transmission.
    override fun onResume() {
        super.onResume()
        AppHostApduService.isTokenTransmissionAllowed = true
    }
}
