package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.settings

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import kotlinx.coroutines.*
class UserInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_info, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingsFragment = view.findViewById<View>(R.id.info_card)
        settingsFragment.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            findNavController().navigate(
                R.id.action_UserInfoFragment_to_settingsFragment,
                null,
                navOptions
            )
        }
        val sharedPref = requireActivity().getSharedPreferences(
            "user_prefs",
            Context.MODE_PRIVATE
        )

        val currentUsername = sharedPref.getString("current_username", null)
        val infoFullNameValue = view.findViewById<TextView>(R.id.infoFullNameValue)
        val infoMatrikelnummerValue = view.findViewById<TextView>(R.id.infoMatrikelnummerValue)
        val infoAccountNumberValue = view.findViewById<TextView>(R.id.infoAccountNumberValue)
        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByUsername(currentUsername)

                if (user != null) {
                infoFullNameValue.text = "${user.firstName} ${user.lastName}"
                infoMatrikelnummerValue.text = "${user.matrikelnumber}"
                infoAccountNumberValue.text = "${user.accountNumber}"

                }
            }
        } else {
            infoFullNameValue.text = "Fehlende Werte"
            infoMatrikelnummerValue.text = "Fehlende Werte"
            infoAccountNumberValue.text = "Fehlende Werte"
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