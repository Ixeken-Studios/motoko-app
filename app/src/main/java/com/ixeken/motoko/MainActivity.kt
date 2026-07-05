package com.ixeken.motoko

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ixeken.motoko.presentation.main.MainScreen
import com.ixeken.motoko.presentation.main.MainViewModel
import com.ixeken.motoko.presentation.dashboard.DashboardViewModel
import com.ixeken.motoko.presentation.history.HistoryViewModel
import com.ixeken.motoko.presentation.subscription.SubscriptionsViewModel
import com.ixeken.motoko.presentation.newitem.NewItemViewModel
import com.ixeken.motoko.presentation.settings.ManageViewModel
import com.ixeken.motoko.ui.theme.MotokoTheme
import com.ixeken.motoko.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.presentation.onboarding.OnboardingScreen
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferences: UserPreferences

    private val mainViewModel: MainViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val subscriptionsViewModel: SubscriptionsViewModel by viewModels()
    private val newItemViewModel: NewItemViewModel by viewModels()
    private val manageViewModel: ManageViewModel by viewModels()

    private var showOnboarding by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        lifecycleScope.launch {
            showOnboarding = preferences.isFirstRun.first()
            if (!showOnboarding && preferences.appLock.first()) {
                showBiometricPrompt()
            } else {
                showContent()
            }
        }
    }

    /**
     * Muestra el cuadro de diálogo de autenticación biométrica del sistema.
     * Utiliza credenciales de respaldo (PIN, patrón o contraseña) si los biométricos no están disponibles.
     */
    private fun showBiometricPrompt() {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showContent()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Motoko Locked")
            .setSubtitle("Authenticate to access your local financial data")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Punto único de entrada Compose. Muestra OnboardingScreen o MainScreen
     * según el estado de showOnboarding.
     */
    private fun showContent() {
        setContent {
            val state by mainViewModel.uiState.collectAsState()
            val themeMode = remember(state.themeMode) {
                try { ThemeMode.valueOf(state.themeMode.uppercase()) } catch (e: Exception) { ThemeMode.LIGHT }
            }
            MotokoTheme(
                themeMode = themeMode,
                appFont = state.appFont,
                textSizeIndex = state.textSizeIndex,
                animationsEnabled = state.animationsEnabled
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                LaunchedEffect(state.checkUpdateOnStart) {
                    if (state.checkUpdateOnStart) {
                        mainViewModel.checkForUpdates(context = context, manual = false)
                    }
                }

                if (showOnboarding) {
                    OnboardingScreen(
                        onComplete = {
                            lifecycleScope.launch {
                                preferences.setFirstRunComplete()
                                showOnboarding = false
                            }
                        }
                    )
                } else {
                    MainScreen(
                        viewModel = mainViewModel,
                        dashboardViewModel = dashboardViewModel,
                        historyViewModel = historyViewModel,
                        subscriptionsViewModel = subscriptionsViewModel,
                        newItemViewModel = newItemViewModel,
                        manageViewModel = manageViewModel,
                        modifier = Modifier.fillMaxSize(),
                        onRepeatOnboarding = {
                            lifecycleScope.launch {
                                preferences.setFirstRun(true)
                                showOnboarding = true
                            }
                        }
                    )
                }
            }
        }
    }
}