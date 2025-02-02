package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.security.viewmodel.UserPinEntryViewModel

class UserPinEntryFragment : Fragment() {

    private lateinit var pinInput: EditText
    private val viewModel: UserPinEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pin_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinInput = view.findViewById(R.id.pin_entry)

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

        for (buttonId in buttons) {
            val button = view.findViewById<MaterialButton>(buttonId)
            button.setOnClickListener {
                viewModel.appendDigit(button.text.toString())
            }
        }

        val clearButton = view.findViewById<MaterialButton>(R.id.pin_number_ac)
        clearButton.setOnClickListener {
            viewModel.clearPin()
        }

        val deleteButton = view.findViewById<MaterialButton>(R.id.pin_number_delete)
        deleteButton.setOnClickListener {
            viewModel.deleteLastDigit()
        }

        viewModel.pin.observe(viewLifecycleOwner, Observer { pin ->
            pinInput.setText(pin)
        })

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