package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.transactions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.*
class UserSendingMoneyFragment : Fragment() {
    private lateinit var amountInput: EditText
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_send_money, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val fiveEuroButton = view.findViewById<MaterialButton>(R.id.send_button_5_euro)
        fiveEuroButton.setOnClickListener {
            amountInput.setText("")
            amountInput.setText("5")
        }
        val fifteenEuroButton = view.findViewById<MaterialButton>(R.id.send_button_15_euro)
        fifteenEuroButton.setOnClickListener {
            amountInput.setText("")
            amountInput.setText("15")
        }
        val twentyfiveEuroButton = view.findViewById<MaterialButton>(R.id.send_button_25_euro)
        twentyfiveEuroButton.setOnClickListener {
            amountInput.setText("")
            amountInput.setText("25")
        }
        val fiftyEuroButton = view.findViewById<MaterialButton>(R.id.send_button_50_euro)
        fiftyEuroButton.setOnClickListener {
            amountInput.setText("")
            amountInput.setText("50")
        }

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> { // Menüpunkt für Einstellungen
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
                    // Optional: Verhindere Navigation zum Dashboard, wenn du bereits dort bist
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.navigation_dashboard, null,navOptions)
                    }
                    true
                }

                else -> false
            }
        }
    }


}