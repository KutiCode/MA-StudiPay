package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R

object NavigationHelper {


    fun buildSlideNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }


    fun buildFadeNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()
    }


    fun buildNavOptions(
        enterAnim: Int,
        exitAnim: Int,
        popEnterAnim: Int = enterAnim,
        popExitAnim: Int = exitAnim
    ): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(enterAnim)
            .setExitAnim(exitAnim)
            .setPopEnterAnim(popEnterAnim)
            .setPopExitAnim(popExitAnim)
            .build()
    }


    fun setupBottomNavigation(
        bottomNavigationView: BottomNavigationView,
        navController: NavController
    ) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.navigation_home -> {
                    navController.navigate(
                        R.id.dashboardFragment,
                        null,
                        buildSlideNavOptions()
                    )
                    true
                }

                R.id.settingsFragment -> {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        navController.navigate(
                            R.id.settingsFragment,
                            null,
                            buildSlideNavOptions()
                        )
                    }
                    true
                }

                else -> false
            }
        }
    }
}
