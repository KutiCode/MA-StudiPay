package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.UiHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.payment.UserTransactionViewModel

// Fragment that handles user transactions by sending or receiving funds.
class UserTransactionFragment : Fragment() {

    // Obtain the ViewModel instance using Kotlin's viewModels delegate.
    private val viewModel: UserTransactionViewModel by viewModels()

    // EditText for user to enter an amount.
    private lateinit var amountInput: EditText

    // Type of transaction; defaults to "SEND" if not provided.
    private var transactionType: String = "SEND"

    // Optional parameter to further determine the transaction source.
    private var source: String? = null

    // Retrieve transaction type and source from fragment arguments.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionType = it.getString("TRANSACTION_TYPE") ?: "SEND"
            source = it.getString("SOURCE")
        }
    }

    // Inflate the fragment's layout from XML.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_transaction, container, false)
    }

    // Called after the view is created; initialize UI elements, set listeners and navigation.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the current user's username from shared preferences.
        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val currentUsername = sharedPref.getString("current_username", null)
        // Reference to the TextView that displays the current balance.
        val balanceAmount = view.findViewById<TextView>(R.id.user_transaction_balance_amount)

        // If username is available, fetch the user balance via the ViewModel.
        currentUsername?.let {
            viewModel.fetchUserBalance(requireContext(), it)
        } ?: run {
            balanceAmount.text = "Fehlender Wert"
        }

        // Observe LiveData for balance and update UI when it changes.
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            balanceAmount.text = balance
        }

        // Set the transaction title based on whether the transaction is SEND or RECEIVE.
        val titleTextView = view.findViewById<TextView>(R.id.transaction_title)
        titleTextView.text =
            if (transactionType == "SEND") "Wie viel möchtest du senden?" else "Wie viel möchtest du empfangen?"

        // Initialize the amount input field.
        amountInput = view.findViewById(R.id.amount_input)

        // Get reference to the button to continue the transaction.
        val sendButton = view.findViewById<MaterialButton>(R.id.continue_button)

        // Set click listener for the send/continue button.
        sendButton.setOnClickListener {
            // Retrieve amount entered by the user as a string.
            val amountString = amountInput.text.toString()
            Log.d("UserTransactionFragment", "Amount: $amountString")

            // Proceed if input is not empty.
            if (amountString.isNotEmpty()) {
                // Convert the input amount to a Double value (or 0.0 if invalid).
                val inputAmount = amountString.toDoubleOrNull() ?: 0.0

                // Process transaction based on type (SEND or RECEIVE)
                if (transactionType == "SEND") {
                    // For SEND transactions and if source matches "orangeDetails".
                    if (source == "orangeDetails") {
                        // Deduct the specified amount from the user's balance.
                        viewModel.deductBalance(requireContext(), currentUsername!!, inputAmount)
                    }
                } else { // For RECEIVE transactions.
                    // Check if source is "orangeDetails" to use balance addition logic.
                    if (source == "orangeDetails") {
                        viewModel.addBalance(requireContext(), currentUsername!!, inputAmount)
                    } else {
                        // For other sources: Ensure valid amount and check WiFi status.
                        if (inputAmount != 0.0) {
                            // Ensure WiFi is enabled and connected before proceeding.
                            if (UiHelper.isWifiEnabled(requireContext()) &&
                                UiHelper.isWifiConnected(requireContext())
                            ) {
                                // Pass the input amount to BeginningRecieveFragment (static variable).
                                BeginningRecieveFragment.amount = inputAmount
                                val navOptions = NavigationHelper.buildSlideNavOptions()
                                // Navigate to BeginningRecieveFragment with slide animation.
                                findNavController().navigate(
                                    R.id.action_userPin_to_beginningRecieveFragment,
                                    null,
                                    navOptions
                                )
                            } else {
                                // If WiFi is not connected, navigate to the NoWifiFragment.
                                val navOptions = NavigationHelper.buildSlideNavOptions()
                                findNavController().navigate(
                                    R.id.noWifiFragment, null,
                                    navOptions
                                )
                            }
                        } else {
                            // If input amount is invalid (e.g., 0.0), show a toast message.
                            Toast.makeText(
                                requireContext(),
                                "Ungültige Eingabe",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        // Setup bottom navigation for navigation between main sections.
        val bottomNavigationView =
            view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }
}
