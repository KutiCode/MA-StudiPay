package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R

// NavigationHelper is an object that provides utility functions for configuring navigation animations
// and handling navigation interactions, such as bottom navigation setup.
object NavigationHelper {

    /**
     * Builds navigation options that use sliding animations.
     *
     * @return A NavOptions object configured with slide-in and slide-out animations.
     */
    fun buildSlideNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)   // Animation for the new screen entering from the right.
            .setExitAnim(R.anim.slide_out_left)      // Animation for the current screen exiting to the left.
            .setPopEnterAnim(R.anim.slide_in_left)   // Animation for re-entering the previous screen from the left.
            .setPopExitAnim(R.anim.slide_out_right)  // Animation for the screen popping out to the right.
            .build()
    }

    /**
     * Builds navigation options that use fade animations.
     *
     * @return A NavOptions object configured with fade-in and fade-out animations.
     */
    fun buildFadeNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)    // Animation for the new screen fading in.
            .setExitAnim(R.anim.fade_out)    // Animation for the current screen fading out.
            .setPopEnterAnim(R.anim.fade_in) // Animation for re-entering the previous screen by fading in.
            .setPopExitAnim(R.anim.fade_out) // Animation for the screen popping out by fading out.
            .build()
    }

    /**
     * Sets up the bottom navigation view to handle navigation actions.
     *
     * @param bottomNavigationView The BottomNavigationView to configure.
     * @param navController The NavController used to perform navigation.
     */
    fun setupBottomNavigation(
        bottomNavigationView: BottomNavigationView,
        navController: NavController
    ) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            // Handle navigation based on the selected item.
            when (item.itemId) {

                // If the home navigation item is selected, navigate to the dashboard.
                R.id.settingsFragment -> {
                    if (navController.currentDestination?.id != R.id.settingsFragment) {
                        navController.navigate(
                            R.id.settingsFragment,
                            null,
                            buildSlideNavOptions() // Use slide animations for the transition.
                        )
                    }
                    true
                }

                // If the settings navigation item is selected, navigate to the settings fragment.
                // This check ensures that if the current destination is not the dashboard, navigate to settings.
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.dashboardFragment) {
                        navController.navigate(
                            R.id.dashboardFragment,
                            null,
                            buildSlideNavOptions() // Use slide animations.
                        )
                    }
                    true
                }

                // Return false if the selected item does not match any known navigation actions.
                else -> false
            }
        }
    }
}
