package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
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
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view.BeginningRecieveFragment
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
        titleTextView.text =
            if (transactionType == "SEND") "Wie viel möchtest du senden?" else "Wie viel möchtest du empfangen?"

        amountInput = view.findViewById(R.id.amount_input)

        val sendButton = view.findViewById<MaterialButton>(R.id.continue_button)

        sendButton.setOnClickListener {
            val amountString = amountInput.text.toString()
            Log.d("UserTransactionFragment", "Amount: $amountString")
            if (amountString.isNotEmpty()) {
                val inputAmount = amountString.toDoubleOrNull() ?: 0.0
                if (transactionType == "SEND") {
                    if (source == "orangeDetails") {
                        viewModel.deductBalance(requireContext(), currentUsername!!, inputAmount)
                    }
                } else { // RECEIVE
                    if (source == "orangeDetails") {
                        viewModel.addBalance(requireContext(), currentUsername!!, inputAmount)
                    } else {

                        if (inputAmount != 0.0) {
                            if (isWifiEnabled(requireContext()) && isWifiConnected(requireContext())) {

                                BeginningRecieveFragment.amount = inputAmount
                                findNavController().navigate(R.id.action_userPin_to_beginningRecieveFragment)
                            } else {
                                findNavController().navigate(R.id.fragment_no_wifi)
                            }
                        } else {
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



        val bottomNavigationView =
            view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
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

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }


    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

}