package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.launch

class UserTransactionFragment : Fragment() {

    private lateinit var amountInput: EditText
    private var transactionType: String = "SEND" // Standardtyp ist "Senden"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionType = it.getString("TRANSACTION_TYPE") ?: "SEND"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByUsername(currentUsername)

                if (user != null) {
                    balanceAmount.text = "${user.balance} €"

                }
            }
        } else {
            balanceAmount.text = "Fehlender Wert"
        }






        // Titel dynamisch setzen basierend auf dem Typ
        val titleTextView = view.findViewById<TextView>(R.id.transaction_title)
        titleTextView.text = if (transactionType == "SEND") "Geld senden" else "Geld empfangen"

        // Referenz zum Betragsfeld
        amountInput = view.findViewById(R.id.amount_input)

        // Zahlen-Buttons referenzieren
        val buttons = listOf(
            R.id.send_button_0, R.id.send_button_1, R.id.send_button_2, R.id.send_button_3,
            R.id.send_button_4, R.id.send_button_5, R.id.send_button_6, R.id.send_button_7,
            R.id.send_button_8, R.id.send_button_9
        )

        // Listener für jeden Button hinzufügen
        for (buttonId in buttons) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                val currentText = amountInput.text.toString()
                val newText = currentText + button.text.toString()
                amountInput.setText(newText)
            }
        }

        // Sondertasten behandeln (z. B. Zurück-Taste oder Komma)
        val clearButton = view.findViewById<MaterialButton>(R.id.send_button_clear)
        clearButton.setOnClickListener {
            amountInput.setText("")
        }

        val commaButton = view.findViewById<MaterialButton>(R.id.send_button_comma)
        commaButton.setOnClickListener {
            val currentText = amountInput.text.toString()
            if (!currentText.contains(",")) {
                amountInput.setText("$currentText,")
            }
        }

        val fixedAmountButtons = mapOf(
            R.id.send_button_5_euro to "5",
            R.id.send_button_15_euro to "15",
            R.id.send_button_25_euro to "25",
            R.id.send_button_50_euro to "50"
        )

        for ((buttonId, amount) in fixedAmountButtons) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                amountInput.setText(amount)
            }
        }

        // Navigationselemente behandeln
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
