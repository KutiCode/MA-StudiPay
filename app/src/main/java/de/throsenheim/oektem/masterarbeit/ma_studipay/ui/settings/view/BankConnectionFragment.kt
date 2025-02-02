package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings.viewmodel.BankConnectionViewModel

class BankConnectionFragment : Fragment() {

    private val viewModel: BankConnectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bank_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsFragment = view.findViewById<View>(R.id.bank_info_card)
        settingsFragment.setOnClickListener {
            viewModel.onSettingsClicked()
        }

        viewModel.navigateToSettings.observe(viewLifecycleOwner, Observer { navigate ->
            if (navigate) {
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .build()
                findNavController().navigate(
                    R.id.action_BankConnectionFragment_to_settingsFragment,
                    null,
                    navOptions
                )
                viewModel.onNavigatedToSettings()
            }
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