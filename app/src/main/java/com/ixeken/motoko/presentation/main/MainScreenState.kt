package com.ixeken.motoko.presentation.main

import androidx.compose.ui.Alignment

/**
 * Pestañas disponibles en la barra flotante de navegación.
 */
enum class MotokoTab {
    DASHBOARD,
    HISTORY,
    SUBSCRIPTIONS
}

/**
 * Estado de la pantalla principal que controla la cabecera y el shell visual.
 */
data class MainScreenState(
    val currentTab: MotokoTab = MotokoTab.DASHBOARD,
    val isPrivacyEnabled: Boolean = false,
    val dockAlignment: Alignment = Alignment.BottomCenter,
    val accounts: List<String> = listOf("Personal", "Trabajo", "Familia"),
    val selectedAccount: String = "Personal",
    val appFont: String = "space_mono",
    val muteDeleteWarnings: Boolean = false,
    val hideBudgetCard: Boolean = false,
    val appLockEnabled: Boolean = false,
    val coloredElementsEnabled: Boolean = true,
    val themeMode: String = "light",
    val checkUpdateOnStart: Boolean = false,
    val swipeToDeleteEnabled: Boolean = true,
    val textSizeIndex: Int = 1,
    val currencySymbol: String = "$",
    val animationsEnabled: Boolean = true,
    val wallets: List<String> = emptyList(),
    val categories: List<String> = emptyList()
)
