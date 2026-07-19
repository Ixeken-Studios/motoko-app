package com.ixeken.motoko.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import com.ixeken.motoko.ui.theme.LocalAnimationsEnabled
import com.ixeken.motoko.ui.theme.MotokoAnimation
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.ixeken.motoko.R
import com.ixeken.motoko.presentation.main.MainViewModel
import com.ixeken.motoko.ui.theme.AppFontType
import com.ixeken.motoko.ui.theme.AppFontProvider
import com.ixeken.motoko.presentation.responsiveWidth
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import androidx.biometric.BiometricManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

// Interceptor para limitar el gesto de deslizar hacia abajo en hojas flotantes (Bottom Sheets)
private val stopSheetSwipeConnection = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return if (available.y > 0f) Offset(0f, available.y) else Offset.Zero
    }
}

// Space Mono Bold font loading read dynamically from the theme
private val SpaceMonoBoldFamily: FontFamily?
    @Composable
    get() = MaterialTheme.typography.bodyLarge.fontFamily

/**
 * Pantalla de configuración del usuario que implementa las secciones General, Security, Appearance y About.
 */
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    manageViewModel: ManageViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRepeatOnboarding: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val backupError by viewModel.backupError.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val repoUriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val profileUriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var showFontDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showThemesScreen by remember { mutableStateOf(false) }
    var showManageScreen by remember { mutableStateOf(false) }
    var showAboutAppScreen by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showThemesScreen || showManageScreen || showAboutAppScreen) {
        when {
            showThemesScreen -> showThemesScreen = false
            showManageScreen -> showManageScreen = false
            showAboutAppScreen -> showAboutAppScreen = false
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.updateMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri)
        }
    }

    // Mapear el dockAlignment de Alignment a String para saber qué botón segmentado seleccionar
    val currentAlignment = when (state.dockAlignment) {
        Alignment.BottomStart -> "left"
        Alignment.BottomEnd -> "right"
        else -> "center"
    }

    val activeColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.primaryDark
    val inactiveColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color(0xFFD1D5DB)

    val subAnimSpec = MotokoAnimation.screenSpec<IntOffset>()
    val subFadeSpec = MotokoAnimation.screenSpec<Float>()

    val currentSubScreenMode = when {
        showThemesScreen -> 1
        showManageScreen -> 2
        showAboutAppScreen -> 3
        else -> 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight)
    ) {
        AnimatedContent(
            targetState = currentSubScreenMode,
            transitionSpec = {
                if (targetState != 0) {
                    // Abrir subpantalla (Themes/Customization, Manage, About): de derecha a izquierda
                    slideInHorizontally(
                        animationSpec = subAnimSpec,
                        initialOffsetX = { fullWidth -> fullWidth }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = subAnimSpec,
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        )
                    ).apply {
                        targetContentZIndex = 1f
                    }
                } else {
                    // Cerrar subpantalla (Regresar a Ajustes principales): de izquierda a derecha
                    slideInHorizontally(
                        animationSpec = subAnimSpec,
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = subAnimSpec,
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    ).apply {
                        targetContentZIndex = 0f
                    }
                }
            },
            label = "subScreenTransition"
        ) { mode ->
        when (mode) {
            1 -> ThemesScreen(
                viewModel = viewModel,
                onBackClick = { showThemesScreen = false }
            )
            2 -> ManageScreen(
                viewModel = manageViewModel,
                onBackClick = { showManageScreen = false },
                coloredElementsEnabled = state.coloredElementsEnabled
            )
            3 -> AboutAppScreen(
                viewModel = viewModel,
                onBackClick = { showAboutAppScreen = false },
                onShowPrivacy = { showPrivacyDialog = true },
                onShowChangelog = { showChangelogDialog = true }
            )
            else -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(LocalMotokoColors.current.primaryLight)
                ) {
        // Cabecera oscura con bordes inferiores redondeados
        Surface(
            color = LocalMotokoColors.current.primaryDark,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de regreso con fondo blanco de 8dp y flecha oscura
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(LocalMotokoColors.current.textOnDark, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_arrow_left),
                        contentDescription = stringResource(id = R.string.desc_back),
                        tint = LocalMotokoColors.current.primaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.settings_title),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }

        // Listado de opciones de configuración
        // Listado de opciones de configuración
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- SECCIÓN GENERAL ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_general))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_download,
                            title = stringResource(id = R.string.settings_import_title),
                            subtext = stringResource(id = R.string.settings_import_desc),
                            onClick = { importLauncher.launch(arrayOf("*/*")) }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_upload,
                            title = stringResource(id = R.string.settings_export_title),
                            subtext = stringResource(id = R.string.settings_export_desc),
                            onClick = { exportLauncher.launch("backup.motoko") }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_file_text,
                            title = stringResource(id = R.string.settings_categories_title),
                            subtext = stringResource(id = R.string.settings_categories_desc),
                            onClick = { showManageScreen = true }
                        )
                        SettingsDashedDivider()
                        val currencyLabel = when (state.currencySymbol) {
                            "$" -> stringResource(id = R.string.currency_dollar)
                            "€" -> stringResource(id = R.string.currency_euro)
                            "£" -> stringResource(id = R.string.currency_pound)
                            "¥" -> stringResource(id = R.string.currency_yen)
                            "₹" -> stringResource(id = R.string.currency_rupee)
                            "₩" -> stringResource(id = R.string.currency_won)
                            "R$" -> stringResource(id = R.string.currency_real)
                            "₽" -> stringResource(id = R.string.currency_ruble)
                            "₺" -> stringResource(id = R.string.currency_lira)
                            "kr" -> stringResource(id = R.string.currency_corona)
                            "₫" -> stringResource(id = R.string.currency_dong)
                            "₦" -> stringResource(id = R.string.currency_naira)
                            "CHF" -> stringResource(id = R.string.currency_franc)
                            else -> state.currencySymbol
                        }
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_circle_dollar_sign,
                            title = stringResource(id = R.string.settings_currency_title),
                            subtext = currencyLabel,
                            onClick = { showCurrencyDialog = true }
                        )
                        SettingsDashedDivider()
                        SettingSwitchCard(
                            iconResId = LucideR.drawable.lucide_ic_trash_2,
                            title = stringResource(id = R.string.settings_swipe_delete_title),
                            subtext = stringResource(id = R.string.settings_swipe_delete_desc),
                            checked = state.swipeToDeleteEnabled,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onCheckedChange = { viewModel.setSwipeToDeleteEnabled(it) }
                        )
                    }
                }
            }

            // --- SECCIÓN SECURITY ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_security))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SettingSwitchCard(
                            iconResId = LucideR.drawable.lucide_ic_credit_card,
                            title = stringResource(id = R.string.settings_hide_budget_title),
                            subtext = stringResource(id = R.string.settings_hide_budget_desc),
                            checked = state.hideBudgetCard,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onCheckedChange = { viewModel.setHideBudgetCard(it) }
                        )
                        SettingsDashedDivider()
                        SettingSwitchCard(
                            iconResId = LucideR.drawable.lucide_ic_lock,
                            title = stringResource(id = R.string.settings_app_lock_title),
                            subtext = stringResource(id = R.string.settings_app_lock_desc),
                            checked = state.appLockEnabled,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val biometricManager = BiometricManager.from(context)
                                    val canAuthenticate = biometricManager.canAuthenticate(
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                                    )
                                    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                                        viewModel.setAppLock(true)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.settings_biometric_no_security),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        viewModel.setAppLock(false)
                                    }
                                } else {
                                    viewModel.setAppLock(false)
                                }
                            }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_trash_2,
                            title = stringResource(id = R.string.settings_clear_data_title),
                            subtext = stringResource(id = R.string.settings_clear_data_desc),
                            onClick = { showClearDataDialog = true }
                        )
                    }
                }
            }

            // --- SECCIÓN APPEARANCE ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_appearance))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_palette,
                            title = stringResource(id = R.string.settings_themes_title),
                            subtext = stringResource(id = R.string.settings_themes_desc),
                            onClick = { showThemesScreen = true }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_type,
                            title = stringResource(id = R.string.settings_font_title),
                            subtext = stringResource(id = R.string.settings_font_desc),
                            onClick = { showFontDialog = true }
                        )
                        SettingsDashedDivider()
                        SettingTextSizeCard(
                            iconResId = LucideR.drawable.lucide_ic_case_sensitive,
                            title = stringResource(id = R.string.settings_text_size_title),
                            subtext = stringResource(id = R.string.settings_text_size_desc),
                            currentIndex = state.textSizeIndex,
                            onIndexChange = { viewModel.setTextSizeIndex(it) }
                        )
                        SettingsDashedDivider()
                        SettingAlignmentCard(
                            iconResId = LucideR.drawable.lucide_ic_navigation,
                            title = stringResource(id = R.string.settings_alignment_title),
                            subtext = stringResource(id = R.string.settings_alignment_desc),
                            currentAlignment = currentAlignment,
                            onAlignmentChange = { viewModel.setDockAlignment(it) }
                        )
                    }
                }
            }

            // --- SECCIÓN ABOUT ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_about))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SettingNavigationCard(
                            iconResId = com.ixeken.motoko.R.drawable.ic_motoko_app,
                            title = stringResource(id = R.string.settings_motoko_title),
                            subtext = stringResource(id = R.string.settings_motoko_version),
                            iconTint = Color.White,
                            onClick = { showAboutAppScreen = true }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_rotate_cw,
                            title = stringResource(id = R.string.settings_repeat_onboarding),
                            subtext = stringResource(id = R.string.settings_repeat_onboarding_desc),
                            onClick = onRepeatOnboarding
                        )
                    }
                }
            }
        }
    }
    }
    }
    }
    }

    if (showFontDialog) {
        FontSelectionBottomSheet(
            currentFont = state.appFont,
            onFontSelect = { fontName ->
                viewModel.setAppFont(fontName)
                showFontDialog = false
            },
            onDismissRequest = { showFontDialog = false },
            coloredElementsEnabled = state.coloredElementsEnabled
        )
    }
    if (showThemeDialog) {
        ThemeSelectionBottomSheet(
            currentMode = state.themeMode,
            onModeSelected = { viewModel.setThemeMode(it) },
            onDismissRequest = { showThemeDialog = false },
            coloredElementsEnabled = state.coloredElementsEnabled
        )
    }
    if (showPrivacyDialog) {
        PrivacyBottomSheet(
            onDismissRequest = { showPrivacyDialog = false },
            coloredElementsEnabled = state.coloredElementsEnabled
        )
    }
    if (showChangelogDialog) {
        ChangelogBottomSheet(
            onDismissRequest = { showChangelogDialog = false },
            coloredElementsEnabled = state.coloredElementsEnabled
        )
    }
    if (showCurrencyDialog) {
        CurrencySelectionBottomSheet(
            currentSymbol = state.currencySymbol,
            onSymbolSelected = { symbol ->
                viewModel.setCurrencySymbol(symbol)
                showCurrencyDialog = false
            },
            onDismissRequest = { showCurrencyDialog = false },
            coloredElementsEnabled = state.coloredElementsEnabled
        )
    }
    if (backupError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearBackupError() },
            confirmButton = {
                Text(
                    text = stringResource(id = R.string.dialog_close),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable(onClick = { viewModel.clearBackupError() })
                        .padding(8.dp)
                )
            },
            title = {
                Text(
                    text = "Error",
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .heightIn(max = 300.dp)
                ) {
                    Text(
                        text = backupError ?: "",
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 14.sp,
                        color = LocalMotokoColors.current.textMuted
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = LocalMotokoColors.current.surfaceCard
        )
    }
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.settings_clear_data_confirm_title),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.settings_clear_data_confirm_message),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 14.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_yes),
                        fontFamily = SpaceMonoBoldFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataDialog = false }
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_no),
                        fontFamily = SpaceMonoBoldFamily,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = LocalMotokoColors.current.surfaceCard
        )
    }
}

/**
 * Cabecera de sección.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = SpaceMonoBoldFamily,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsDashedDivider() {
    val lineColor = LocalMotokoColors.current.colorLines
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}

/**
 * Tarjeta de navegación general (Chevron a la derecha).
 */
@Composable
private fun SettingNavigationCard(
    iconResId: Int,
    title: String,
    subtext: String? = null,
    iconTint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 11.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }
        Icon(
            painter = painterResource(id = LucideR.drawable.lucide_ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Tarjeta con interruptor (Switch a la derecha).
 */
@Composable
private fun SettingSwitchCard(
    iconResId: Int,
    title: String,
    subtext: String? = null,
    checked: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 11.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        CustomSwitch(
            checked = checked,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            modifier = Modifier.clickable {
                onCheckedChange(!checked)
            }
        )
    }
}

/**
 * Tarjeta con selector segmentado de alineación (Left, Center, Right).
 */
@Composable
private fun SettingAlignmentCard(
    iconResId: Int,
    title: String,
    subtext: String,
    currentAlignment: String,
    onAlignmentChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 11.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented control (Left, Center, Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("left", "center", "right").forEach { align ->
                val isSelected = currentAlignment == align
                val label = when (align) {
                    "left" -> stringResource(id = R.string.alignment_left)
                    "center" -> stringResource(id = R.string.alignment_center)
                    else -> stringResource(id = R.string.alignment_right)
                }
                val bg = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .background(bg, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAlignmentChange(align) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta con Slider discreto para seleccionar el tamaño de texto (5 pasos).
 */
@Composable
private fun SettingTextSizeCard(
    iconResId: Int,
    title: String,
    subtext: String,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit
) {
    val labels = listOf(
        stringResource(id = R.string.text_size_0),
        stringResource(id = R.string.text_size_1),
        stringResource(id = R.string.text_size_2),
        stringResource(id = R.string.text_size_3),
        stringResource(id = R.string.text_size_4)
    )

    var sliderValue by remember(currentIndex) { mutableFloatStateOf(currentIndex.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 11.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                val snapped = kotlin.math.round(sliderValue).toInt().coerceIn(0, 4)
                sliderValue = snapped.toFloat()
                onIndexChange(snapped)
            },
            valueRange = 0f..4f,
            steps = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = LocalMotokoColors.current.textPrimary,
                activeTrackColor = LocalMotokoColors.current.textPrimary,
                inactiveTrackColor = LocalMotokoColors.current.colorLines,
                activeTickColor = LocalMotokoColors.current.primaryLight,
                inactiveTickColor = LocalMotokoColors.current.activeTab
            )
        )

        val labelIndex = kotlin.math.round(sliderValue).toInt().coerceIn(0, 4)
        Text(
            text = labels[labelIndex],
            fontFamily = SpaceMonoBoldFamily,
            fontSize = 14.sp,
            color = LocalMotokoColors.current.primaryDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Tarjeta de información (estática).
 */
@Composable
private fun SettingInfoCard(
    iconResId: Int,
    title: String,
    subtext: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 11.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }
    }
}

/**
 * Interruptor ergonómico personalizado con fondo de color y botón deslizante blanco.
 */
@Composable
private fun CustomSwitch(
    checked: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val padding = 3.dp

    val alignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    val trackColor = if (checked) activeColor else inactiveColor

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .background(trackColor, RoundedCornerShape(14.dp))
            .padding(padding),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Diálogo de selección de tema de la aplicación.
 */
/**
 * Hoja flotante de selección de tema de la aplicación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionBottomSheet(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    coloredElementsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animateDismiss: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
                onDismissRequest()
            }
        }
    }

    val options = listOf(
        "light" to stringResource(id = R.string.theme_mode_option_light),
        "dark" to stringResource(id = R.string.theme_mode_option_dark),
        "dark_olive" to stringResource(id = R.string.theme_mode_option_olive)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .nestedScroll(stopSheetSwipeConnection)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Theme mode",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { animateDismiss {} },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = R.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.activeTab.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    options.forEachIndexed { index, (key, label) ->
                        val isSelected = currentMode == key
                        val animState = remember(key) { Animatable(0f) }
                        val animationsEnabled = LocalAnimationsEnabled.current
                        val animSpec = MotokoAnimation.screenSpec<Float>()
                        LaunchedEffect(key) {
                            if (animationsEnabled) {
                                kotlinx.coroutines.delay(index * 30L)
                            }
                            animState.animateTo(
                                targetValue = 1f,
                                animationSpec = animSpec
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = animState.value
                                    translationY = (1f - animState.value) * 30f
                                }
                                .height(48.dp)
                                .background(
                                    if (isSelected) LocalMotokoColors.current.activeTab
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    animateDismiss { onModeSelected(key) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textOnDark
                                )
                            )
                        }

                        if (index < options.lastIndex) {
                            val lineColor = LocalMotokoColors.current.activeTab.copy(alpha = 0.3f)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                            ) {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    pathEffect = pathEffect,
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSelectionBottomSheet(
    currentFont: String,
    onFontSelect: (String) -> Unit,
    onDismissRequest: () -> Unit,
    coloredElementsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animateDismiss: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
                onDismissRequest()
            }
        }
    }

    val fonts = listOf(
        "default" to "System font",
        "space_mono" to "Space mono",
        "space_grotesk" to "Space Grotesk",
        "dm_sans" to "DM Sans",
        "inter" to "Inter",
        "daruma_drop_one" to "DarumaDrop One",
        "dela_gothic_one" to "Dela Gothic One",
        "dotgothic16" to "DotGothic16"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .nestedScroll(stopSheetSwipeConnection)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Font style",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { animateDismiss {} },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = R.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.activeTab.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    fonts.forEachIndexed { index, (fontKey, displayName) ->
                        val isSelected = currentFont == fontKey
                        val animState = remember(fontKey) { Animatable(0f) }
                        val animationsEnabled = LocalAnimationsEnabled.current
                        val animSpec = MotokoAnimation.screenSpec<Float>()
                        LaunchedEffect(fontKey) {
                            if (animationsEnabled) {
                                kotlinx.coroutines.delay(index * 30L)
                            }
                            animState.animateTo(
                                targetValue = 1f,
                                animationSpec = animSpec
                            )
                        }
                        
                        val optionFontType = when (fontKey) {
                            "default" -> AppFontType.DEFAULT
                            "space_mono" -> AppFontType.SPACE_MONO
                            "space_grotesk" -> AppFontType.SPACE_GROTESK
                            "dm_sans" -> AppFontType.DM_SANS
                            "inter" -> AppFontType.INTER
                            "daruma_drop_one" -> AppFontType.DARUMA_DROP_ONE
                            "dela_gothic_one" -> AppFontType.DELA_GOTHIC_ONE
                            "dotgothic16" -> AppFontType.DOTGOTHIC16
                            else -> AppFontType.DEFAULT
                        }
                        val optionFontFamily = AppFontProvider.getFontFamily(optionFontType)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = animState.value
                                    translationY = (1f - animState.value) * 30f
                                }
                                .height(48.dp)
                                .background(
                                    if (isSelected) LocalMotokoColors.current.activeTab
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    animateDismiss { onFontSelect(fontKey) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName,
                                fontFamily = optionFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                        }

                        if (index < fonts.lastIndex) {
                            val lineColor = LocalMotokoColors.current.activeTab.copy(alpha = 0.3f)
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                            ) {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    pathEffect = pathEffect,
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangelogBottomSheet(
    onDismissRequest: () -> Unit,
    coloredElementsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animateDismiss: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .nestedScroll(stopSheetSwipeConnection)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_changelog_title),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { animateDismiss {} },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = R.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            val rawChangelog = stringResource(id = R.string.settings_changelog_body)
            val lines = rawChangelog.split("\n").filter { it.isNotBlank() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                lines.forEach { line ->
                    if (line.startsWith("Motoko")) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textPrimary
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (coloredElementsEnabled) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.textPrimary
                                )
                            )
                            Text(
                                text = line.removePrefix("- ").removePrefix("• ").trim(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = LocalMotokoColors.current.textPrimary
                                )
                            )
                        }
                    }
                }
            }


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyBottomSheet(
    onDismissRequest: () -> Unit,
    coloredElementsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animateDismiss: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .nestedScroll(stopSheetSwipeConnection)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_privacy_dialog_title),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { animateDismiss {} },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = R.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = R.string.settings_privacy_dialog_body),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )
            }


        }
    }
}

@Composable
private fun UpdateAvailableDialog(
    version: String,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard),
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.settings_update_dialog_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.settings_update_dialog_body, version),
                    fontSize = 14.sp,
                    color = LocalMotokoColors.current.textMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_close),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(id = R.string.settings_update_dialog_download),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onDownload)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hoja flotante de selección de moneda con las 13 opciones de monedas más populares.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelectionBottomSheet(
    currentSymbol: String,
    onSymbolSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    coloredElementsEnabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val animateDismiss: (() -> Unit) -> Unit = { action ->
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                action()
                onDismissRequest()
            }
        }
    }

    val options = listOf(
        "$" to stringResource(id = R.string.currency_dollar),
        "€" to stringResource(id = R.string.currency_euro),
        "£" to stringResource(id = R.string.currency_pound),
        "¥" to stringResource(id = R.string.currency_yen),
        "₹" to stringResource(id = R.string.currency_rupee),
        "₩" to stringResource(id = R.string.currency_won),
        "R$" to stringResource(id = R.string.currency_real),
        "₽" to stringResource(id = R.string.currency_ruble),
        "₺" to stringResource(id = R.string.currency_lira),
        "kr" to stringResource(id = R.string.currency_corona),
        "₫" to stringResource(id = R.string.currency_dong),
        "₦" to stringResource(id = R.string.currency_naira),
        "CHF" to stringResource(id = R.string.currency_franc)
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .nestedScroll(stopSheetSwipeConnection)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_currency_title),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { animateDismiss {} },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = R.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(options.size) { index ->
                        val (symbol, label) = options[index]
                        val isSelected = currentSymbol == symbol
                        val animState = remember(symbol) { Animatable(0f) }
                        val animationsEnabled = LocalAnimationsEnabled.current
                        val animSpec = MotokoAnimation.screenSpec<Float>()
                        LaunchedEffect(symbol) {
                            if (animationsEnabled) {
                                kotlinx.coroutines.delay(index * 25L)
                            }
                            animState.animateTo(
                                targetValue = 1f,
                                animationSpec = animSpec
                            )
                        }

                        val bg = if (isSelected) LocalMotokoColors.current.activeTab
                                 else LocalMotokoColors.current.activeTab.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = animState.value
                                    translationY = (1f - animState.value) * 30f
                                }
                                .height(48.dp)
                                .background(bg, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    animateDismiss { onSymbolSelected(symbol) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textOnDark
                                )
                            )
                        }
                    }
                }
            }


        }
    }
}

@Composable
private fun AboutAppScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onShowPrivacy: () -> Unit,
    onShowChangelog: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val repoUriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val profileUriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    val activeColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.primaryDark
    val inactiveColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color(0xFFD1D5DB)

    var showStartUpdateInternetConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveWidth(720.dp)
        ) {
        Surface(
            color = LocalMotokoColors.current.primaryDark,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(LocalMotokoColors.current.textOnDark, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = LucideR.drawable.lucide_ic_arrow_left),
                        contentDescription = stringResource(id = R.string.desc_back),
                        tint = LocalMotokoColors.current.primaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.settings_motoko_title),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_github,
                            title = stringResource(id = R.string.settings_repo_title),
                            onClick = { repoUriHandler.openUri("https://github.com/Ixeken-Studios/motoko-app") }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_file_text,
                            title = stringResource(id = R.string.settings_changelog_title),
                            onClick = onShowChangelog
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_refresh_cw,
                            title = stringResource(id = R.string.settings_updates_title),
                            onClick = { viewModel.showInternetConfirmDialog = true }
                        )
                        SettingsDashedDivider()
                        SettingSwitchCard(
                            iconResId = LucideR.drawable.lucide_ic_rotate_cw,
                            title = stringResource(id = R.string.settings_update_start_title),
                            subtext = stringResource(id = R.string.settings_update_start_desc),
                            checked = state.checkUpdateOnStart,
                            activeColor = activeColor,
                            inactiveColor = inactiveColor,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    showStartUpdateInternetConfirmDialog = true
                                } else {
                                    viewModel.setCheckUpdateOnStart(false)
                                }
                            }
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = LucideR.drawable.lucide_ic_shield,
                            title = stringResource(id = R.string.settings_privacy_title),
                            onClick = onShowPrivacy
                        )
                        SettingsDashedDivider()
                        SettingNavigationCard(
                            iconResId = com.ixeken.motoko.R.drawable.ic_ixeken_logo,
                            title = stringResource(id = R.string.settings_created_by_title),
                            subtext = stringResource(id = R.string.settings_created_by_desc),
                            iconTint = Color.White,
                            onClick = { profileUriHandler.openUri("https://github.com/Ixeken-Studios") }
                        )
                    }
                }
            }
        }
    }
}

    if (showStartUpdateInternetConfirmDialog) {
        InternetConfirmDialog(
            onConfirm = {
                showStartUpdateInternetConfirmDialog = false
                viewModel.setCheckUpdateOnStart(true)
            },
            onDismiss = {
                showStartUpdateInternetConfirmDialog = false
            }
        )
    }

    if (viewModel.showInternetConfirmDialog) {
        InternetConfirmDialog(
            onConfirm = {
                viewModel.showInternetConfirmDialog = false
                viewModel.checkUpdatesManual(context)
            },
            onDismiss = {
                viewModel.showInternetConfirmDialog = false
            }
        )
    }

    if (viewModel.showUpdateDialog) {
        MotokoUpdateDialog(
            viewModel = viewModel,
            onDismiss = {
                viewModel.clearUpdateResult()
            }
        )
    }
}
