package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.dashboard

/**
 * Data class representing the UI state for the dashboard.
 *
 * @property firstName The first name of the user to be displayed.
 * @property balance The user's balance formatted as a string.
 * @property matrikelNumber The user's matriculation number.
 */
data class DashboardUiState(
    val firstName: String,
    val balance: String,
    val matrikelNumber: String
)
