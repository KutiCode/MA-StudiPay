package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions.viewmodel.UserTransactionViewModel

class UserTransactionFragment : Fragment() {

    private val viewModel: UserTransactionViewModel by viewModels()
    private lateinit var amountInput: EditText
    private var transactionType: String = "SEND"
    private var source: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionType = it.getString("TRANSACTION_TYPE") ?: "SEND"
            source = it.getString("SOURCE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val currentUsername = sharedPref.getString("current_username", null)
        val balanceAmount = view.findViewById<TextView>(R.id.user_transaction_balance_amount)

        currentUsername?.let {
            viewModel.loadUserBalance(requireContext(), it)
        } ?: run {
            balanceAmount.text = "Fehlender Wert"
        }

        viewModel.balance.observe(viewLifecycleOwner, Observer { balance ->
            balanceAmount.text = balance
        })

        val titleTextView = view.findViewById<TextView>(R.id.transaction_title)
        titleTextView.text = if (transactionType == "SEND") "Geld senden" else "Geld empfangen"

        amountInput = view.findViewById(R.id.amount_input)

        val sendButton = view.findViewById<MaterialButton>(R.id.continue_button)

        sendButton.setOnClickListener {
            val amount = amountInput.text.toString()
            if (amount.isNotEmpty()) {
                if (transactionType == "SEND") {
                    if (source == "orangeDetails") {
                        viewModel.deductBalance(
                            requireContext(),
                            currentUsername!!,
                            amount.toDouble()
                        )
                    } else {
                        findNavController().navigate(R.id.action_userPin_to_beginningRecieveFragment)
                    }
                } else {
                    if (source == "orangeDetails") {
                        viewModel.addBalance(requireContext(), currentUsername!!, amount.toDouble())
                    } else {

                    }
                }
            }
        }



        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    true
                }
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    }
                    true
                }
                else -> false
            }
        }
    }
}