package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R

class UserPinEntryFragment : Fragment() {

    private lateinit var pinInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pin_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referenz zum Betragsfeld
        pinInput = view.findViewById(R.id.pin_entry)

        // Zahlen-Buttons referenzieren
        val buttons = listOf(
            R.id.pin_number_0,
            R.id.pin_number_1,
            R.id.pin_number_2,
            R.id.pin_number_3,
            R.id.pin_number_4,
            R.id.pin_number_5,
            R.id.pin_number_6,
            R.id.pin_number_7,
            R.id.pin_number_8,
            R.id.pin_number_9
        )

        // Listener für jeden Button hinzufügen
        for (buttonId in buttons) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                val currentText = pinInput.text.toString()
                val newText = currentText + button.text.toString()
                pinInput.setText(newText)
            }
        }

        // Sondertasten behandeln (z. B. Zurück-Taste oder Komma)
        val clearButton = view.findViewById<MaterialButton>(R.id.pin_number_ac)
        clearButton.setOnClickListener {
            pinInput.setText("")
        }

        val deleteButton = view.findViewById<MaterialButton>(R.id.pin_number_delete)
        deleteButton.setOnClickListener {
            val currentText = pinInput.text.toString()
            if (currentText.isNotEmpty()) {
                // Entferne das letzte Zeichen
                pinInput.setText(currentText.substring(0, currentText.length - 1))
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
