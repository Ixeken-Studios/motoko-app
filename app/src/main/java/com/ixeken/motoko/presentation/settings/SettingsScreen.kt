package com.ixeken.motoko.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import androidx.biometric.BiometricManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler

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
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showThemesScreen || showManageScreen) {
        if (showThemesScreen) {
            showThemesScreen = false
        } else if (showManageScreen) {
            showManageScreen = false
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

    if (showThemesScreen) {
        ThemesScreen(
            viewModel = viewModel,
            onBackClick = { showThemesScreen = false }
        )
    } else if (showManageScreen) {
        ManageScreen(
            viewModel = manageViewModel,
            onBackClick = { showManageScreen = false }
        )
    } else {
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
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_download,
                    title = stringResource(id = R.string.settings_import_title),
                    subtext = stringResource(id = R.string.settings_import_desc),
                    onClick = { importLauncher.launch(arrayOf("*/*")) }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_upload,
                    title = stringResource(id = R.string.settings_export_title),
                    subtext = stringResource(id = R.string.settings_export_desc),
                    onClick = { exportLauncher.launch("backup.motoko") }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_file_text,
                    title = stringResource(id = R.string.settings_categories_title),
                    subtext = stringResource(id = R.string.settings_categories_desc),
                    onClick = { showManageScreen = true }
                )
            }
            item {
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
            }
            item {
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

            // --- SECCIÓN SECURITY ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_security))
            }
            item {
                SettingSwitchCard(
                    iconResId = LucideR.drawable.lucide_ic_credit_card,
                    title = stringResource(id = R.string.settings_hide_budget_title),
                    subtext = stringResource(id = R.string.settings_hide_budget_desc),
                    checked = state.hideBudgetCard,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onCheckedChange = { viewModel.setHideBudgetCard(it) }
                )
            }
            item {
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
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_trash_2,
                    title = stringResource(id = R.string.settings_clear_data_title),
                    subtext = stringResource(id = R.string.settings_clear_data_desc),
                    onClick = { showClearDataDialog = true }
                )
            }

            // --- SECCIÓN APPEARANCE ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_appearance))
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_palette,
                    title = stringResource(id = R.string.settings_themes_title),
                    subtext = stringResource(id = R.string.settings_themes_desc),
                    onClick = { showThemesScreen = true }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_type,
                    title = stringResource(id = R.string.settings_font_title),
                    subtext = stringResource(id = R.string.settings_font_desc),
                    onClick = { showFontDialog = true }
                )
            }
            item {
                SettingTextSizeCard(
                    iconResId = LucideR.drawable.lucide_ic_search,
                    title = stringResource(id = R.string.settings_text_size_title),
                    subtext = stringResource(id = R.string.settings_text_size_desc),
                    currentIndex = state.textSizeIndex,
                    onIndexChange = { viewModel.setTextSizeIndex(it) }
                )
            }
            item {
                SettingAlignmentCard(
                    iconResId = LucideR.drawable.lucide_ic_navigation,
                    title = stringResource(id = R.string.settings_alignment_title),
                    subtext = stringResource(id = R.string.settings_alignment_desc),
                    currentAlignment = currentAlignment,
                    onAlignmentChange = { viewModel.setDockAlignment(it) }
                )
            }

            // --- SECCIÓN ABOUT ---
            item {
                SectionHeader(stringResource(id = R.string.settings_sec_about))
            }
            item {
                SettingInfoCard(
                    iconResId = LucideR.drawable.lucide_ic_cat,
                    title = stringResource(id = R.string.settings_motoko_title),
                    subtext = stringResource(id = R.string.settings_motoko_version)
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_shield,
                    title = stringResource(id = R.string.settings_privacy_title),
                    onClick = { showPrivacyDialog = true }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_file_text,
                    title = stringResource(id = R.string.settings_changelog_title),
                    onClick = { showChangelogDialog = true }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_refresh_cw,
                    title = stringResource(id = R.string.settings_updates_title),
                    onClick = { viewModel.checkForUpdates(context, manual = true) }
                )
            }
            item {
                SettingSwitchCard(
                    iconResId = LucideR.drawable.lucide_ic_refresh_cw,
                    title = stringResource(id = R.string.settings_update_start_title),
                    subtext = stringResource(id = R.string.settings_update_start_desc),
                    checked = state.checkUpdateOnStart,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onCheckedChange = { viewModel.setCheckUpdateOnStart(it) }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_folder_code,
                    title = stringResource(id = R.string.settings_repo_title),
                    onClick = { repoUriHandler.openUri("https://github.com/Ixeken-Studios/motoko-app") }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_user,
                    title = stringResource(id = R.string.settings_created_by_title),
                    subtext = stringResource(id = R.string.settings_created_by_desc),
                    onClick = { profileUriHandler.openUri("https://github.com/Ixeken-Studios") }
                )
            }
            item {
                SettingNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_rotate_cw,
                    title = stringResource(id = R.string.settings_repeat_onboarding),
                    subtext = stringResource(id = R.string.settings_repeat_onboarding_desc),
                    onClick = onRepeatOnboarding
                )
            }
        }
        if (showFontDialog) {
            FontSelectionDialog(
                currentFont = state.appFont,
                onFontSelect = { fontName ->
                    viewModel.setAppFont(fontName)
                    showFontDialog = false
                },
                onDismiss = { showFontDialog = false }
            )
        }
        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentMode = state.themeMode,
                onModeSelected = { viewModel.setThemeMode(it) },
                onDismiss = { showThemeDialog = false }
            )
        }
        if (showPrivacyDialog) {
            PrivacyDialog(
                onDismiss = { showPrivacyDialog = false }
            )
        }
        if (showChangelogDialog) {
            ChangelogDialog(
                onDismiss = { showChangelogDialog = false }
            )
        }
        if (showCurrencyDialog) {
            CurrencySelectionDialog(
                currentSymbol = state.currencySymbol,
                onSymbolSelected = { symbol ->
                    viewModel.setCurrencySymbol(symbol)
                    showCurrencyDialog = false
                },
                onDismiss = { showCurrencyDialog = false }
            )
        }
        if (viewModel.updateAvailableVersion != null) {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            UpdateAvailableDialog(
                version = viewModel.updateAvailableVersion ?: "",
                onDownload = {
                    viewModel.updateHtmlUrl?.let { url ->
                        uriHandler.openUri(url)
                    }
                    viewModel.clearUpdateState()
                },
                onDismiss = { viewModel.clearUpdateState() }
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
                            color = LocalMotokoColors.current.colorExpense
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

/**
 * Tarjeta de navegación general (Chevron a la derecha).
 */
@Composable
private fun SettingNavigationCard(
    iconResId: Int,
    title: String,
    subtext: String? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
    var isChecked by remember(checked) { mutableStateOf(checked) }
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
                checked = isChecked,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.clickable {
                    val next = !isChecked
                    isChecked = next
                    onCheckedChange(next)
                }
            )
        }
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
                    if (subtext.isNotBlank()) {
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
@Composable
fun ThemeSelectionDialog(
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "light" to stringResource(id = R.string.theme_mode_option_light),
        "dark" to stringResource(id = R.string.theme_mode_option_dark),
        "dark_olive" to stringResource(id = R.string.theme_mode_option_olive)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Text(
                text = stringResource(id = R.string.dialog_close),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(8.dp)
            )
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                options.forEach { (key, label) ->
                    val isSelected = currentMode == key
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (isSelected) LocalMotokoColors.current.activeTab
                                else LocalMotokoColors.current.primaryDark,
                                RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onModeSelected(key)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textOnDark
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = LocalMotokoColors.current.surfaceCard
    )
}

@Composable
private fun FontSelectionDialog(
    currentFont: String,
    onFontSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Text(
                text = stringResource(id = R.string.dialog_close),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(8.dp)
            )
        },
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val fonts = listOf(
                    "default" to "System font",
                    "space_mono" to "Space mono",
                    "space_grotesk" to "Space Grotesk",
                    "dm_sans" to "DM Sans",
                    "inter" to "Inter"
                )

                fonts.forEach { (fontKey, displayName) ->
                    val isSelected = currentFont == fontKey
                    val bg = when {
                        isSelected -> LocalMotokoColors.current.activeTab
                        fontKey == "default" -> LocalMotokoColors.current.textMuted
                        else -> LocalMotokoColors.current.primaryDark
                    }

                    // Resolve the font family specifically for this option
                    val optionFontType = when (fontKey) {
                        "default" -> AppFontType.DEFAULT
                        "space_mono" -> AppFontType.SPACE_MONO
                        "space_grotesk" -> AppFontType.SPACE_GROTESK
                        "dm_sans" -> AppFontType.DM_SANS
                        "inter" -> AppFontType.INTER
                        else -> AppFontType.DEFAULT
                    }
                    val optionFontFamily = AppFontProvider.getFontFamily(optionFontType)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(bg, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onFontSelect(fontKey)
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
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = LocalMotokoColors.current.surfaceCard
    )
}

@Composable
private fun ChangelogDialog(
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
                    text = "v${stringResource(id = R.string.settings_motoko_version)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.settings_changelog_body),
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
                }
            }
        }
    }
}

@Composable
private fun PrivacyDialog(
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
                    text = stringResource(id = R.string.settings_privacy_dialog_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.settings_privacy_dialog_body),
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
                }
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
 * Diálogo de selección de moneda con las 13 opciones de monedas más populares.
 */
@Composable
private fun CurrencySelectionDialog(
    currentSymbol: String,
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Text(
                text = stringResource(id = R.string.dialog_close),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(8.dp)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.settings_currency_title),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
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
                        val bg = if (isSelected) LocalMotokoColors.current.activeTab
                                 else LocalMotokoColors.current.primaryDark
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(bg, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSymbolSelected(symbol)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = SpaceMonoBoldFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = LocalMotokoColors.current.surfaceCard
    )
}
