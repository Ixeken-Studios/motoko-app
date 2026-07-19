package com.ixeken.motoko.presentation.main

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ixeken.motoko.presentation.subscription.SubscriptionsUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.ixeken.motoko.R as AppR
import com.composables.icons.lucide.R
import com.ixeken.motoko.presentation.resolveIconRes
import com.ixeken.motoko.presentation.dashboard.DashboardScreen
import com.ixeken.motoko.presentation.dashboard.DashboardViewModel
import com.ixeken.motoko.presentation.dashboard.DashboardPeriod
import com.ixeken.motoko.presentation.dashboard.DashboardViewMode
import com.ixeken.motoko.presentation.dashboard.MotokoDonutChart
import com.ixeken.motoko.presentation.history.HistoryScreen
import com.ixeken.motoko.presentation.history.HistoryViewModel
import com.ixeken.motoko.presentation.history.HistoryTypeFilter
import com.ixeken.motoko.presentation.history.HistoryTimeFilter
import com.ixeken.motoko.presentation.settings.SettingsScreen
import com.ixeken.motoko.presentation.settings.ManageViewModel
import com.ixeken.motoko.presentation.subscription.SubscriptionsScreen
import com.ixeken.motoko.presentation.subscription.SubscriptionsViewModel
import com.ixeken.motoko.presentation.responsiveWidth
import com.ixeken.motoko.presentation.isWideScreen
import com.ixeken.motoko.ui.theme.bounceClick
import com.ixeken.motoko.presentation.newitem.NewItemScreen
import com.ixeken.motoko.presentation.newitem.NewItemViewModel
import com.ixeken.motoko.presentation.newitem.ItemType
import com.ixeken.motoko.presentation.newitem.ItemDetailBottomSheet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import com.ixeken.motoko.ui.theme.MotokoAnimation
import kotlinx.coroutines.launch

/**
 * Contenedor visual base (Shell) de la aplicación que organiza la cabecera,
 * el lienzo de contenido y los controles flotantes inferiores.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    dashboardViewModel: DashboardViewModel,
    historyViewModel: HistoryViewModel,
    subscriptionsViewModel: SubscriptionsViewModel,
    newItemViewModel: NewItemViewModel,
    manageViewModel: ManageViewModel,
    modifier: Modifier = Modifier,
    onRepeatOnboarding: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val subState by subscriptionsViewModel.uiState.collectAsStateWithLifecycle()
    val newItemState by newItemViewModel.uiState.collectAsStateWithLifecycle()
    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val dashboardViewMode by dashboardViewModel.viewMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val density = LocalDensity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSettings by remember { mutableStateOf(false) }
    var showNewItem by remember { mutableStateOf(false) }
    var historySearchQuery by remember { mutableStateOf("") }
    val historyTypeFilter by historyViewModel.typeFilter.collectAsStateWithLifecycle()
    val historyTimeFilter by historyViewModel.timeFilter.collectAsStateWithLifecycle()
    val historyCategoryFilter by historyViewModel.categoryFilter.collectAsStateWithLifecycle()
    var subscriptionsBillingFilter by remember { mutableStateOf("") }
    var isHeaderMinimized by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    var showDetailSheet by remember { mutableStateOf(false) }
    var detailId by remember { mutableStateOf(0L) }
    var detailName by remember { mutableStateOf("") }
    var detailAmount by remember { mutableStateOf("") }
    var detailType by remember { mutableStateOf(ItemType.EXPENSE) }
    var detailWallet by remember { mutableStateOf("") }
    var detailCategory by remember { mutableStateOf("") }
    var detailDate by remember { mutableStateOf("") }
    var detailNote by remember { mutableStateOf("") }
    var detailAccount by remember { mutableStateOf("") }
    var detailIcon by remember { mutableStateOf("") }
    var detailReceipt by remember { mutableStateOf("") }

    val onTransactionClick = { id: Long, name: String, amount: String, type: ItemType, wallet: String, category: String, date: String, note: String, account: String, icon: String, receipt: String ->
        detailId = id
        detailName = name
        detailAmount = amount
        detailType = type
        detailWallet = wallet
        detailCategory = category
        detailDate = date
        detailNote = note
        detailAccount = account
        detailIcon = icon
        detailReceipt = receipt
        showDetailSheet = true
    }

    LaunchedEffect(viewModel) {
        viewModel.backupMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler(enabled = showSettings || showNewItem || isFabMenuExpanded || state.currentTab != MotokoTab.DASHBOARD) {
        when {
            showSettings -> showSettings = false
            showNewItem -> {
                newItemViewModel.resetForm()
                showNewItem = false
            }
            isFabMenuExpanded -> isFabMenuExpanded = false
            state.currentTab != MotokoTab.DASHBOARD -> viewModel.selectTab(MotokoTab.DASHBOARD)
        }
    }

    val overlayAnimSpec = MotokoAnimation.screenSpec<androidx.compose.ui.unit.IntOffset>()
    val overlayFadeSpec = MotokoAnimation.screenSpec<Float>()

    val currentOverlayMode = when {
        showSettings -> 1 // Settings
        showNewItem -> 2  // New Item
        else -> 0         // Main Tabs
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight)
    ) {
        AnimatedContent(
            targetState = currentOverlayMode,
            transitionSpec = {
                if (targetState != 0) {
                    // Abrir (Settings o NewItem): de derecha a izquierda
                    slideInHorizontally(
                        animationSpec = overlayAnimSpec,
                        initialOffsetX = { fullWidth -> fullWidth }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = overlayAnimSpec,
                            targetOffsetX = { fullWidth -> -fullWidth / 3 }
                        )
                    ).apply {
                        targetContentZIndex = 1f
                    }
                } else {
                    // Cerrar (Regresar a Tabs): de izquierda a derecha
                    slideInHorizontally(
                        animationSpec = overlayAnimSpec,
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                    ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = overlayAnimSpec,
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    ).apply {
                        targetContentZIndex = 0f
                    }
                }
            },
            label = "overlayTransition"
        ) { mode ->
            when (mode) {
                1 -> SettingsScreen(
                    viewModel = viewModel,
                    manageViewModel = manageViewModel,
                    onBackClick = { showSettings = false },
                    onRepeatOnboarding = onRepeatOnboarding
                )
                2 -> NewItemScreen(
                    viewModel = newItemViewModel,
                    currencySymbol = state.currencySymbol,
                    onBackClick = {
                        newItemViewModel.resetForm()
                        showNewItem = false
                    }
                )
                else -> {
                    // CAPA 1: CABECERA DINÁMICA Y CONTENIDO SCROLLABLE
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalMotokoColors.current.primaryLight),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .responsiveWidth(840.dp)
                ) {
                
                // Cabecera oscura con bordes inferiores redondeados
                Surface(
                    color = LocalMotokoColors.current.primaryDark,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount < -10f) {
                                    isHeaderMinimized = true
                                } else if (dragAmount > 10f) {
                                    isHeaderMinimized = false
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .animateContentSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                        ) {
                            // Fila de herramientas superiores
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Selector / Badge de Cuenta
                                AccountSelector(
                                    selectedAccount = state.selectedAccount,
                                    accounts = state.accounts,
                                    onAccountSelected = { viewModel.selectAccount(it) }
                                )

                                // Acciones: Privacidad (Ojo) y Ajustes (Engranaje)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.togglePrivacy() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = if (state.isPrivacyEnabled) R.drawable.lucide_ic_eye_off else R.drawable.lucide_ic_eye),
                                            contentDescription = stringResource(id = AppR.string.desc_privacy_global),
                                            tint = LocalMotokoColors.current.iconOnLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showSettings = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.lucide_ic_settings),
                                            contentDescription = stringResource(id = AppR.string.desc_settings),
                                            tint = LocalMotokoColors.current.iconOnLight,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            when (state.currentTab) {
                                MotokoTab.DASHBOARD -> DashboardHeaderContent(
                                    isPrivacyEnabled = state.isPrivacyEnabled,
                                    isMinimized = isHeaderMinimized,
                                    categories = dashboardState.categories,
                                    expensesAmount = dashboardState.expensesAmount,
                                    incomeAmount = dashboardState.incomeAmount,
                                    selectedPeriod = dashboardState.selectedPeriod,
                                    onPeriodSelected = { dashboardViewModel.setPeriod(it) },
                                    coloredElementsEnabled = state.coloredElementsEnabled,
                                    viewMode = dashboardViewMode,
                                    onViewModeSelected = { dashboardViewModel.setViewMode(it) },
                                    currencySymbol = state.currencySymbol
                                )
                                MotokoTab.HISTORY -> HistoryHeaderContent(
                                    searchQuery = historySearchQuery,
                                    onQueryChange = { historySearchQuery = it },
                                    isMinimized = isHeaderMinimized,
                                    selectedType = historyTypeFilter,
                                    onTypeSelected = { historyViewModel.setTypeFilter(it) },
                                    selectedTime = historyTimeFilter,
                                    onTimeSelected = { historyViewModel.setTimeFilter(it) },
                                    selectedCategory = historyCategoryFilter,
                                    onCategorySelected = { historyViewModel.setCategoryFilter(it) },
                                    coloredElementsEnabled = state.coloredElementsEnabled,
                                    categoriesList = state.categories
                                )
                                MotokoTab.SUBSCRIPTIONS -> {
                                    val monthlyVal = if (subState is SubscriptionsUiState.Success) {
                                        (subState as SubscriptionsUiState.Success).monthlyTotal
                                    } else {
                                        0.0
                                    }
                                    val annualVal = if (subState is SubscriptionsUiState.Success) {
                                        (subState as SubscriptionsUiState.Success).annualTotal
                                    } else {
                                        0.0
                                    }
                                    SubscriptionsHeaderContent(
                                        isPrivacyEnabled = state.isPrivacyEnabled,
                                        isMinimized = isHeaderMinimized,
                                        monthlyTotal = monthlyVal,
                                        annualTotal = annualVal,
                                        billingFilter = subscriptionsBillingFilter,
                                        onBillingFilterChange = { subscriptionsBillingFilter = it },
                                        currencySymbol = state.currencySymbol
                                    )
                                }
                            }
                        }

                        // Barra de arrastre (Drag Handle)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isHeaderMinimized = !isHeaderMinimized }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .background(LocalMotokoColors.current.surfaceCard.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }

            // Lienzo central de contenido (inyectado reactivamente según la pestaña activa)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val tabAnimSpec = MotokoAnimation.screenSpec<androidx.compose.ui.unit.IntOffset>()
                val tabFadeSpec = MotokoAnimation.screenSpec<Float>()
                AnimatedContent(
                    targetState = state.currentTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(
                            animationSpec = tabAnimSpec,
                            initialOffsetX = { fullWidth -> direction * (fullWidth / 4) }
                        ) + fadeIn(animationSpec = tabFadeSpec)) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tabAnimSpec,
                            targetOffsetX = { fullWidth -> -direction * (fullWidth / 4) }
                        ) + fadeOut(animationSpec = tabFadeSpec))
                    },
                    label = "tabTransition"
                ) { tab ->
                    when (tab) {
                        MotokoTab.DASHBOARD -> DashboardScreen(
                            viewModel = dashboardViewModel,
                            isPrivacyEnabled = state.isPrivacyEnabled,
                            hideBudgetCard = state.hideBudgetCard,
                            coloredElementsEnabled = state.coloredElementsEnabled,
                            currencySymbol = state.currencySymbol,
                            walletsList = state.wallets,
                            onTransactionClick = onTransactionClick
                        )
                        MotokoTab.HISTORY -> HistoryScreen(
                            viewModel = historyViewModel,
                            isPrivacyEnabled = state.isPrivacyEnabled,
                            searchQuery = historySearchQuery,
                            coloredElementsEnabled = state.coloredElementsEnabled,
                            currencySymbol = state.currencySymbol,
                            swipeToDeleteEnabled = state.swipeToDeleteEnabled,
                            onSwipeDelete = { txId ->
                                viewModel.deleteTransactionWithUndo(txId)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = context.getString(AppR.string.snackbar_transaction_deleted),
                                        actionLabel = context.getString(AppR.string.snackbar_undo),
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoLastDelete()
                                    } else {
                                        viewModel.clearUndo()
                                    }
                                }
                            },
                            walletsList = state.wallets,
                            onTransactionClick = onTransactionClick
                        )
                        MotokoTab.SUBSCRIPTIONS -> SubscriptionsScreen(
                            viewModel = subscriptionsViewModel,
                            isPrivacyEnabled = state.isPrivacyEnabled,
                            currencySymbol = state.currencySymbol,
                            billingFilter = subscriptionsBillingFilter,
                            walletsList = state.wallets,
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }
        }
    }
}
}
}

        val rotationAngle by animateFloatAsState(
            targetValue = if (isFabMenuExpanded) 135f else 0f,
            animationSpec = if (state.animationsEnabled) {
                androidx.compose.animation.core.spring(
                    dampingRatio = 0.72f,
                    stiffness = 380f
                )
            } else androidx.compose.animation.core.snap(),
            label = "fabRotation"
        )

        val menuTransitionProgress by animateFloatAsState(
            targetValue = if (isFabMenuExpanded) 1f else 0f,
            animationSpec = if (state.animationsEnabled) {
                androidx.compose.animation.core.spring(
                    dampingRatio = 0.72f,
                    stiffness = 350f
                )
            } else androidx.compose.animation.core.snap(),
            label = "menuTransitionProgress"
        )

        AnimatedVisibility(
            visible = currentOverlayMode == 0,
            enter = fadeIn(animationSpec = overlayFadeSpec) + slideInVertically(
                animationSpec = overlayAnimSpec,
                initialOffsetY = { fullHeight -> fullHeight }
            ),
            exit = fadeOut(animationSpec = overlayFadeSpec) + slideOutVertically(
                animationSpec = overlayAnimSpec,
                targetOffsetY = { fullHeight -> fullHeight }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isFabMenuExpanded || menuTransitionProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = (menuTransitionProgress * 0.5f).coerceIn(0f, 0.5f) }
                            .background(Color.Black)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isFabMenuExpanded = false
                            }
                    )
                }

                // CAPA 2: CONTROLES FLOTANTES INFERIORES (BARRA DE NAVEGACIÓN Y FAB ADYACENTE)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, start = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .responsiveWidth(840.dp),
                        contentAlignment = state.dockAlignment
                    ) {
                        val isLeftAligned = state.dockAlignment == Alignment.BottomStart

                        val tabsPill = @Composable {
                            Row(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = Color.Black.copy(alpha = 0.5f),
                                        spotColor = Color.Black.copy(alpha = 0.5f)
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                NavigationTabItem(
                                    iconResId = R.drawable.lucide_ic_layout_dashboard,
                                    isSelected = state.currentTab == MotokoTab.DASHBOARD,
                                    onClick = { viewModel.selectTab(MotokoTab.DASHBOARD) }
                                )
                                NavigationTabItem(
                                    iconResId = R.drawable.lucide_ic_history,
                                    isSelected = state.currentTab == MotokoTab.HISTORY,
                                    onClick = { viewModel.selectTab(MotokoTab.HISTORY) }
                                )
                                NavigationTabItem(
                                    iconResId = R.drawable.lucide_ic_calendar_clock,
                                    isSelected = state.currentTab == MotokoTab.SUBSCRIPTIONS,
                                    onClick = { viewModel.selectTab(MotokoTab.SUBSCRIPTIONS) }
                                )
                            }
                        }

                        val fabButton = @Composable {
                            Box(
                                contentAlignment = Alignment.TopStart,
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(65.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(20.dp),
                                            ambientColor = Color.Black.copy(alpha = 0.5f),
                                            spotColor = Color.Black.copy(alpha = 0.5f)
                                        )
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(20.dp))
                                        .bounceClick(pressedScale = 0.92f) {
                                            isFabMenuExpanded = !isFabMenuExpanded
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.lucide_ic_plus),
                                        contentDescription = stringResource(id = AppR.string.desc_add_transaction),
                                        tint = LocalMotokoColors.current.iconOnDark,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .graphicsLayer { rotationZ = rotationAngle }
                                    )
                                }

                                if (isFabMenuExpanded || menuTransitionProgress > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .layout { measurable, constraints ->
                                                val placeable = measurable.measure(
                                                    constraints.copy(
                                                        minWidth = 0,
                                                        maxWidth = androidx.compose.ui.unit.Constraints.Infinity
                                                    )
                                                )
                                                layout(0, 0) {
                                                    val xOffset = when (state.dockAlignment) {
                                                        Alignment.BottomStart -> 0
                                                        else -> -placeable.width + 65.dp.roundToPx()
                                                    }
                                                    placeable.place(xOffset, -placeable.height - 16.dp.roundToPx())
                                                }
                                            }
                                    ) {
                                        Column(
                                            horizontalAlignment = when (state.dockAlignment) {
                                                Alignment.BottomStart -> Alignment.Start
                                                else -> Alignment.End
                                            },
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val speedDialOptions = listOf(
                                                Triple(AppR.string.new_item_tab_subscription, R.drawable.lucide_ic_repeat, ItemType.SUBSCRIPTION),
                                                Triple(AppR.string.new_item_tab_expense, R.drawable.lucide_ic_trending_down, ItemType.EXPENSE),
                                                Triple(AppR.string.new_item_tab_income, R.drawable.lucide_ic_trending_up, ItemType.INCOME)
                                            )

                                            speedDialOptions.forEachIndexed { index, (textRes, iconRes, type) ->
                                                val staggerOffset = (speedDialOptions.size - 1 - index) * 0.16f
                                                val itemProgress = if (state.animationsEnabled) {
                                                    ((menuTransitionProgress - staggerOffset) / (1f - staggerOffset)).coerceIn(0f, 1f)
                                                } else menuTransitionProgress

                                                val itemScale = 0.65f + (itemProgress * 0.35f)
                                                val itemOffsetY = (1f - itemProgress) * 20.dp.value

                                                Box(
                                                    modifier = Modifier.graphicsLayer {
                                                        alpha = itemProgress
                                                        scaleX = itemScale
                                                        scaleY = itemScale
                                                        translationY = with(density) { itemOffsetY.dp.toPx() }
                                                    }
                                                ) {
                                                    SpeedDialItem(
                                                        text = stringResource(id = textRes),
                                                        iconRes = iconRes,
                                                        onClick = {
                                                            newItemViewModel.onTypeSelected(type)
                                                            showNewItem = true
                                                            isFabMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isLeftAligned) {
                                fabButton()
                                tabsPill()
                            } else {
                                tabsPill()
                                fabButton()
                            }
                        }
            }
        }
        }
        }

        if (showDetailSheet) {
            ItemDetailBottomSheet(
                id = detailId,
                name = detailName,
                amount = detailAmount,
                type = detailType,
                wallet = detailWallet,
                category = detailCategory,
                date = detailDate,
                note = detailNote,
                account = detailAccount,
                iconName = detailIcon,
                receiptPath = detailReceipt,
                onDismissRequest = { showDetailSheet = false },
                onDeleteConfirmed = { dontShowAgain ->
                    if (detailType == ItemType.SUBSCRIPTION) {
                        // Suscripciones: borrado directo sin undo (tienen semántica recurrente)
                        viewModel.deleteItem(detailId, detailType)
                        if (dontShowAgain) viewModel.setMuteDeleteWarnings(true)
                        Toast.makeText(context, context.getString(AppR.string.snackbar_transaction_deleted), Toast.LENGTH_SHORT).show()
                    } else {
                        // Transacciones: borrado con undo vía Snackbar
                        if (dontShowAgain) viewModel.setMuteDeleteWarnings(true)
                        viewModel.deleteTransactionWithUndo(detailId)
                        showDetailSheet = false
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(AppR.string.snackbar_transaction_deleted),
                                actionLabel = context.getString(AppR.string.snackbar_undo),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLastDelete()
                            } else {
                                viewModel.clearUndo()
                            }
                        }
                    }
                },
                onSaveEdited = { name, amount, wallet, category, date, note, account, iconName, receipt, billingPeriod ->
                    detailName = name
                    detailAmount = amount
                    detailWallet = wallet
                    detailCategory = category
                    detailDate = date
                    detailNote = note
                    detailAccount = account
                    detailIcon = iconName
                    detailReceipt = receipt
                    viewModel.updateItem(
                        id = detailId,
                        type = detailType,
                        name = name,
                        amountStr = amount,
                        walletName = wallet,
                        categoryName = category,
                        dateStr = date,
                        noteStr = note,
                        accountName = account,
                        iconName = iconName,
                        receiptPath = receipt,
                        billingPeriodStr = billingPeriod
                    )
                    Toast.makeText(context, context.getString(AppR.string.settings_changes_saved), Toast.LENGTH_SHORT).show()
                },
                walletsList = newItemState.wallets,
                categoriesList = newItemState.categories,
                accountsList = state.accounts,
                muteDeleteWarnings = state.muteDeleteWarnings,
                viewModel = viewModel
            )
        }

        // CAPA 3: SNACKBAR para mensajes de undo (anclado sobre la barra flotante)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }

        if (viewModel.showUpdateDialog && !showSettings) {
            com.ixeken.motoko.presentation.settings.MotokoUpdateDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.clearUpdateResult() }
            )
        }
    }
}

/**
 * Selector desplegable de cuenta (Badge dropdown).
 */
@Composable
fun AccountSelector(
    selectedAccount: String,
    accounts: List<String>,
    onAccountSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .height(32.dp)
            .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedAccount,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
            contentDescription = stringResource(id = AppR.string.desc_choose_account),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(12.dp)
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalMotokoColors.current.surfaceCard,
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(accounts.size) { index ->
                                val account = accounts[index]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(
                                            color = if (account == selectedAccount) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onAccountSelected(account)
                                            showDialog = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = account,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalMotokoColors.current.textOnDark
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = stringResource(id = AppR.string.dialog_close),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary,
                        modifier = Modifier.clickable { showDialog = false }
                    )
                }
            }
        }
    }
}

/**
 * Item individual de la barra de navegación con animación de selección.
 */
@Composable
fun NavigationTabItem(
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animaciones suaves de color de fondo, escala y opacidad
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) LocalMotokoColors.current.activeTab else Color.Transparent,
        animationSpec = MotokoAnimation.microSpec(),
        label = "BgColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = MotokoAnimation.microSpec(),
        label = "IconScale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) LocalMotokoColors.current.surfaceCard else LocalMotokoColors.current.textMuted,
        animationSpec = MotokoAnimation.microSpec(),
        label = "IconTint"
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(backgroundColor, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .bounceClick(pressedScale = 0.92f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = stringResource(id = AppR.string.desc_nav_tab),
            tint = iconTint,
            modifier = Modifier
                .size(28.dp)
                .scale(iconScale)
        )
    }
}



/**
 * Componente gráfico que muestra la distribución de gastos por categoría en forma de gráfica de barras.
 */
@Composable
fun CategoryExpensesBarChart(
    expensesList: List<com.ixeken.motoko.presentation.dashboard.DashboardCategoryItem>,
    totalExpenses: Double,
    isPrivacyEnabled: Boolean,
    periodLabel: String,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    // Animación de crecimiento progresivo para las barras de categorías
    val progress = remember { Animatable(0f) }
    val animSpec = MotokoAnimation.sheetSpec<Float>()
    LaunchedEffect(expensesList) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            
            if (expensesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded",
                        color = LocalMotokoColors.current.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val maxExpense = expensesList.maxOf { it.totalAmount }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expensesList.take(4).forEach { item ->
                        val ratio = if (maxExpense > 0) (item.totalAmount / maxExpense).toFloat() else 0f
                        val animatedRatio = ratio * progress.value
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconRes = resolveIconRes(item.iconName)
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(64.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .background(LocalMotokoColors.current.primaryLight, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedRatio.coerceAtLeast(0.04f))
                                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            val amountText = if (isPrivacyEnabled) "• • •" else com.ixeken.motoko.util.CurrencyFormatter.format(item.totalAmount, currencySymbol, isPrivacyEnabled = false)
                            Text(
                                text = amountText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente gráfico que muestra la distribución de gastos por categoría en forma de gráfica circular.
 */
@Composable
fun CategoryExpensesPieChart(
    expensesList: List<com.ixeken.motoko.presentation.dashboard.DashboardCategoryItem>,
    totalExpenses: Double,
    isPrivacyEnabled: Boolean,
    periodLabel: String,
    coloredElementsEnabled: Boolean,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            
            if (expensesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded",
                        color = LocalMotokoColors.current.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                val sliceColors = listOf(
                    LocalMotokoColors.current.primaryDark,
                    Color(0xFF343A40),
                    Color(0xFF495057),
                    Color(0xFF6C757D),
                    Color(0xFFADB5BD),
                    Color(0xFFDEE2E6)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MotokoDonutChart(
                        categoryExpenses = expensesList.associate { it.name to it.totalAmount },
                        modifier = Modifier.size(160.dp),
                        colors = sliceColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val items = expensesList.take(6).toList()
                    val groupedRows = items.chunked(2)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        groupedRows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { item ->
                                    val idx = items.indexOf(item)
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(sliceColors[idx % sliceColors.size], CircleShape)
                                        )
                                        Text(
                                            text = item.name + ": " + (if (isPrivacyEnabled) "• • •" else "${currencySymbol}${String.format(java.util.Locale.US, "%.1f", item.totalAmount)}"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = false
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeaderContent(
    isPrivacyEnabled: Boolean,
    isMinimized: Boolean,
    categories: List<com.ixeken.motoko.presentation.dashboard.DashboardCategoryItem>,
    expensesAmount: Double,
    incomeAmount: Double,
    selectedPeriod: DashboardPeriod,
    onPeriodSelected: (DashboardPeriod) -> Unit,
    coloredElementsEnabled: Boolean,
    viewMode: DashboardViewMode,
    onViewModeSelected: (DashboardViewMode) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val currentPeriodString = when (selectedPeriod) {
        DashboardPeriod.DAY -> stringResource(id = AppR.string.dashboard_filter_day)
        DashboardPeriod.WEEK -> stringResource(id = AppR.string.dashboard_filter_week)
        DashboardPeriod.MONTH -> stringResource(id = AppR.string.dashboard_filter_month)
        DashboardPeriod.YEAR -> stringResource(id = AppR.string.dashboard_filter_year)
    }

    // Cálculo dinámico de etiquetas y fechas para la cabecera según el periodo seleccionado
    val (periodPrefix, periodSuffix) = remember(selectedPeriod) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        
        when (selectedPeriod) {
            DashboardPeriod.DAY -> {
                val sdf = java.text.SimpleDateFormat("d MMMM, yyyy", java.util.Locale.US)
                val formattedDate = sdf.format(java.util.Date(now))
                Pair("Today, ", formattedDate)
            }
            DashboardPeriod.WEEK -> {
                calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val startDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val startMonth = java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(calendar.time)
                
                calendar.add(java.util.Calendar.DAY_OF_WEEK, 6)
                val endDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                val endMonth = java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(calendar.time)
                
                val range = if (startMonth == endMonth) {
                    "$startDay-$endDay $startMonth"
                } else {
                    "$startDay $startMonth - $endDay $endMonth"
                }
                Pair("This Week, ", range)
            }
            DashboardPeriod.MONTH -> {
                val sdf = java.text.SimpleDateFormat("MMMM", java.util.Locale.US)
                val formattedDate = sdf.format(java.util.Date(now))
                Pair("This Month, ", formattedDate)
            }
            DashboardPeriod.YEAR -> {
                val sdf = java.text.SimpleDateFormat("yyyy", java.util.Locale.US)
                val formattedDate = sdf.format(java.util.Date(now))
                Pair("This Year, ", formattedDate)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = periodPrefix,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = LocalMotokoColors.current.textOnDark
            )
            Text(
                text = periodSuffix,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textOnDark
            )
        }

        if (!isMinimized) {
            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = AppR.string.dashboard_no_categories),
                        color = LocalMotokoColors.current.textOnDark.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Crossfade(targetState = viewMode, label = "DashboardHeaderCrossfade") { mode ->
                    when (mode) {
                        DashboardViewMode.CHART -> {
                            val expensesList = categories.filter { !it.isIncome }.take(6)
                            val totalExpenses = expensesList.sumOf { it.totalAmount }
                            CategoryExpensesBarChart(
                                expensesList = expensesList,
                                totalExpenses = totalExpenses,
                                isPrivacyEnabled = isPrivacyEnabled,
                                periodLabel = currentPeriodString,
                                currencySymbol = currencySymbol
                            )
                        }
                        DashboardViewMode.PIE -> {
                            val expensesList = categories.filter { !it.isIncome }.take(6)
                            val totalExpenses = expensesList.sumOf { it.totalAmount }
                            CategoryExpensesPieChart(
                                expensesList = expensesList,
                                totalExpenses = totalExpenses,
                                isPrivacyEnabled = isPrivacyEnabled,
                                periodLabel = currentPeriodString,
                                coloredElementsEnabled = coloredElementsEnabled,
                                currencySymbol = currencySymbol
                            )
                        }
                        DashboardViewMode.LIST -> {
                            data class CategoryItem(
                                val iconRes: Int,
                                val title: String,
                                val amount: String
                            )

                            val listToShow = listOfNotNull(categories.firstOrNull { it.isIncome }) +
                                    categories.filter { !it.isIncome }.take(5)

                            val displayCategories = listToShow.map { item ->
                                val icon = resolveIconRes(item.iconName)
                                val sign = if (item.isIncome) "+" else "-"
                                CategoryItem(
                                    iconRes = icon,
                                    title = item.name,
                                    amount = if (isPrivacyEnabled) "• • •" else "$sign$currencySymbol ${String.format(java.util.Locale.US, "%.2f", item.totalAmount)}"
                                )
                            }

                            val groupedRows = remember(displayCategories, isPrivacyEnabled) {
                                val result = mutableListOf<List<CategoryItem>>()
                                var i = 0
                                while (i < displayCategories.size) {
                                    val current = displayCategories[i]
                                    if (current.title.length > 10) {
                                        result.add(listOf(current))
                                        i++
                                    } else {
                                        if (i + 1 < displayCategories.size && displayCategories[i + 1].title.length <= 10) {
                                            result.add(listOf(current, displayCategories[i + 1]))
                                            i += 2
                                        } else {
                                            result.add(listOf(current))
                                            i++
                                        }
                                    }
                                }
                                result
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                groupedRows.forEach { rowItems ->
                                    if (rowItems.size == 1) {
                                        val item = rowItems[0]
                                        DashboardCategoryCard(
                                            iconRes = item.iconRes,
                                            title = item.title,
                                            amount = item.amount,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { item ->
                                                DashboardCategoryCard(
                                                    iconRes = item.iconRes,
                                                    title = item.title,
                                                    amount = item.amount,
                                                    modifier = Modifier.weight(1f)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val expensesText = if (isPrivacyEnabled) "• • •" else com.ixeken.motoko.util.CurrencyFormatter.format(expensesAmount, currencySymbol, isPrivacyEnabled = false)
            val incomeText = if (isPrivacyEnabled) "• • •" else com.ixeken.motoko.util.CurrencyFormatter.format(incomeAmount, currencySymbol, isPrivacyEnabled = false)

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(id = AppR.string.dashboard_expenses, expensesText),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(id = AppR.string.dashboard_income, incomeText),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    color = if (incomeAmount < 0) LocalMotokoColors.current.colorExpense else if (coloredElementsEnabled) LocalMotokoColors.current.colorIncome else MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (!isMinimized) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDialog = true }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPeriodString,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (showDialog) {
                    val options = listOf(
                        stringResource(id = AppR.string.dashboard_filter_day),
                        stringResource(id = AppR.string.dashboard_filter_week),
                        stringResource(id = AppR.string.dashboard_filter_month),
                        stringResource(id = AppR.string.dashboard_filter_year)
                    )
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LocalMotokoColors.current.surfaceCard,
                            modifier = Modifier
                                .width(280.dp)
                                .wrapContentHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    options.forEach { option ->
                                        val periodValue = when (option) {
                                            stringResource(id = AppR.string.dashboard_filter_day) -> DashboardPeriod.DAY
                                            stringResource(id = AppR.string.dashboard_filter_week) -> DashboardPeriod.WEEK
                                            stringResource(id = AppR.string.dashboard_filter_month) -> DashboardPeriod.MONTH
                                            stringResource(id = AppR.string.dashboard_filter_year) -> DashboardPeriod.YEAR
                                            else -> DashboardPeriod.DAY
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .background(
                                                    color = if (periodValue == selectedPeriod) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onPeriodSelected(periodValue)
                                                    showDialog = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LocalMotokoColors.current.textOnDark
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = stringResource(id = AppR.string.dialog_close),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textPrimary,
                                    modifier = Modifier.clickable { showDialog = false }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                        .height(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (viewMode == DashboardViewMode.CHART) LocalMotokoColors.current.colorLines else Color.Transparent, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewModeSelected(DashboardViewMode.CHART) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_chart_bar),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (viewMode == DashboardViewMode.PIE) LocalMotokoColors.current.colorLines else Color.Transparent, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewModeSelected(DashboardViewMode.PIE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_chart_pie),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (viewMode == DashboardViewMode.LIST) LocalMotokoColors.current.colorLines else Color.Transparent, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewModeSelected(DashboardViewMode.LIST) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_list),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cabecera de History: título, búsqueda y filtros
// ---------------------------------------------------------------------------

/**
 * Contenido de la cabecera oscura para la pestaña de Historial.
 * Se inyecta dentro del Surface oscuro compartido de MainScreen,
 * al igual que DashboardHeaderContent.
 *
 * @param searchQuery texto actual del campo de búsqueda
 * @param onQueryChange callback cuando el texto cambia
 * @param isMinimized define si la cabecera está contraída
 */
@Composable
fun HistoryHeaderContent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isMinimized: Boolean,
    selectedType: HistoryTypeFilter,
    onTypeSelected: (HistoryTypeFilter) -> Unit,
    selectedTime: HistoryTimeFilter,
    onTimeSelected: (HistoryTimeFilter) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    coloredElementsEnabled: Boolean,
    categoriesList: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = AppR.string.tab_history),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textOnDark
        )

        if (!isMinimized) {
            Spacer(modifier = Modifier.height(16.dp))

            HistorySearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            HistoryFilterRow(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                selectedTime = selectedTime,
                onTimeSelected = onTimeSelected,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                coloredElementsEnabled = coloredElementsEnabled,
                categoriesList = categoriesList,
                onClearAll = {
                    onTimeSelected(HistoryTimeFilter.ALL)
                    onCategorySelected("")
                    onTypeSelected(HistoryTypeFilter.ALL)
                    onQueryChange("")
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cabecera de Subscriptions: título y resúmenes
// ---------------------------------------------------------------------------

/**
 * Contenido de la cabecera oscura para la pestaña de Suscripciones.
 *
 * @param isPrivacyEnabled indica si el modo ocultación de montos está activo
 * @param isMinimized define si la cabecera está contraída
 */
@Composable
fun SubscriptionsHeaderContent(
    isPrivacyEnabled: Boolean,
    isMinimized: Boolean,
    monthlyTotal: Double,
    annualTotal: Double,
    billingFilter: String,
    onBillingFilterChange: (String) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val monthlyText = if (isPrivacyEnabled) "• • •" else com.ixeken.motoko.util.CurrencyFormatter.format(monthlyTotal, currencySymbol, isPrivacyEnabled = false)
    val annualText = if (isPrivacyEnabled) "• • •" else com.ixeken.motoko.util.CurrencyFormatter.format(annualTotal, currencySymbol, isPrivacyEnabled = false)
    var showDialog by remember { mutableStateOf(false) }
    val allBillingText = stringResource(id = AppR.string.sub_billing_all)

    Column(modifier = modifier.fillMaxWidth()) {
        if (!isMinimized) {
            Text(
                text = stringResource(id = AppR.string.sub_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textOnDark
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tarjetas de Gastos Totales (Row con 2 tarjetas)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = AppR.string.sub_monthly_expense),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = monthlyText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = LocalMotokoColors.current.colorExpense
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = AppR.string.sub_annual_expense),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = annualText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = LocalMotokoColors.current.colorExpense
                    )
                }
            }
        }

        if (!isMinimized) {
            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Periodo (Billing period v)
            Row(
                modifier = Modifier
                    .height(32.dp)
                    .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDialog = true }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (billingFilter.isEmpty()) stringResource(id = AppR.string.sub_billing_period) else billingFilter,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
                    contentDescription = null,
                    tint = LocalMotokoColors.current.primaryDark,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        if (showDialog) {
            val options = listOf(
                allBillingText,
                stringResource(id = AppR.string.billing_period_monthly),
                stringResource(id = AppR.string.billing_period_annual)
            )
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LocalMotokoColors.current.surfaceCard,
                    modifier = Modifier
                        .width(280.dp)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            options.forEach { option ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(
                                            color = if (option == billingFilter || (billingFilter.isEmpty() && option == allBillingText)) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onBillingFilterChange(if (option == allBillingText) "" else option)
                                            showDialog = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalMotokoColors.current.textOnDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(id = AppR.string.dialog_close),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textPrimary,
                            modifier = Modifier.clickable { showDialog = false }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de búsqueda con BasicTextField funcional y placeholder superpuesto.
 * El texto y el cursor se centran verticalmente. El botón de icono ocupa
 * el alto completo de la barra con radio de esquina squircle de 8dp.
 */
@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))        // recorta el fondo del icono con la forma de la barra
            .background(LocalMotokoColors.current.activeTab)
            .padding(start = 14.dp),                // solo padding izquierdo para el texto
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Área de texto con placeholder y cursor centrados verticalmente
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 10.dp),              // separación entre texto y zona del icono
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(id = AppR.string.history_search_placeholder),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textOnDark.copy(alpha = 0.6f)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textOnDark,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                ),
                cursorBrush = SolidColor(LocalMotokoColors.current.surfaceCard),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Zona del icono: mismo alto y ancho que la barra, recortada por el clip del padre
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .background(LocalMotokoColors.current.textMuted),     // sin shape propia, el clip del Row la da
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_search),
                contentDescription = stringResource(id = AppR.string.desc_history_search),
                tint = LocalMotokoColors.current.iconOnDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HistoryFilterRow(
    selectedType: HistoryTypeFilter,
    onTypeSelected: (HistoryTypeFilter) -> Unit,
    selectedTime: HistoryTimeFilter,
    onTimeSelected: (HistoryTimeFilter) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    coloredElementsEnabled: Boolean,
    categoriesList: List<String> = emptyList(),
    onClearAll: () -> Unit
) {
    var showTimeDialog by remember { mutableStateOf(false) }

    var showCategoryDialog by remember { mutableStateOf(false) }

    var showTypeDialog by remember { mutableStateOf(false) }

    val allCategoryText = stringResource(id = AppR.string.history_filter_category_all)
    val allTypeText = stringResource(id = AppR.string.history_filter_type_all)

    val timeLabel = when (selectedTime) {
        HistoryTimeFilter.ALL -> stringResource(id = AppR.string.history_filter_time)
        HistoryTimeFilter.TODAY -> stringResource(id = AppR.string.history_filter_time_today)
        HistoryTimeFilter.YESTERDAY -> stringResource(id = AppR.string.history_filter_time_yesterday)
        HistoryTimeFilter.THIS_WEEK -> stringResource(id = AppR.string.history_filter_time_week)
        HistoryTimeFilter.THIS_MONTH -> stringResource(id = AppR.string.history_filter_time_month)
        HistoryTimeFilter.THIS_YEAR -> stringResource(id = AppR.string.history_filter_time_year)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryFilterChip(
                label = timeLabel,
                descRes = AppR.string.desc_history_filter_time,
                onClick = { showTimeDialog = true }
            )
            HistoryFilterChip(
                label = if (selectedCategory.isEmpty()) stringResource(id = AppR.string.history_filter_category) else selectedCategory,
                descRes = AppR.string.desc_history_filter_category,
                onClick = { showCategoryDialog = true }
            )
            HistoryFilterChip(
                label = when (selectedType) {
                    HistoryTypeFilter.ALL -> stringResource(id = AppR.string.history_filter_type)
                    HistoryTypeFilter.INCOME -> stringResource(id = AppR.string.history_filter_type_income)
                    HistoryTypeFilter.EXPENSE -> stringResource(id = AppR.string.history_filter_type_expense)
                },
                descRes = AppR.string.desc_history_filter_type,
                onClick = { showTypeDialog = true }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.primaryDark, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onClearAll()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_trash_2),
                contentDescription = stringResource(id = AppR.string.desc_history_clear),
                tint = LocalMotokoColors.current.iconOnDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showTypeDialog) {
        val options = listOf(
            HistoryTypeFilter.ALL to allTypeText,
            HistoryTypeFilter.INCOME to stringResource(id = AppR.string.history_filter_type_income),
            HistoryTypeFilter.EXPENSE to stringResource(id = AppR.string.history_filter_type_expense)
        )
        Dialog(onDismissRequest = { showTypeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalMotokoColors.current.surfaceCard,
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEach { (typeVal, labelStr) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        color = if (typeVal == selectedType) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onTypeSelected(typeVal)
                                        showTypeDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelStr,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textOnDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = AppR.string.dialog_close),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary,
                        modifier = Modifier.clickable { showTypeDialog = false }
                    )
                }
            }
        }
    }

    if (showTimeDialog) {
        val options = listOf(
            HistoryTimeFilter.ALL to stringResource(id = AppR.string.history_filter_time_all),
            HistoryTimeFilter.TODAY to stringResource(id = AppR.string.history_filter_time_today),
            HistoryTimeFilter.YESTERDAY to stringResource(id = AppR.string.history_filter_time_yesterday),
            HistoryTimeFilter.THIS_WEEK to stringResource(id = AppR.string.history_filter_time_week),
            HistoryTimeFilter.THIS_MONTH to stringResource(id = AppR.string.history_filter_time_month),
            HistoryTimeFilter.THIS_YEAR to stringResource(id = AppR.string.history_filter_time_year)
        )
        Dialog(onDismissRequest = { showTimeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalMotokoColors.current.surfaceCard,
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEach { (filter, label) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        color = if (filter == selectedTime) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onTimeSelected(filter)
                                        showTimeDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textOnDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = AppR.string.dialog_close),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary,
                        modifier = Modifier.clickable { showTimeDialog = false }
                    )
                }
            }
        }
    }

    if (showCategoryDialog) {
        val options = listOf(allCategoryText) + categoriesList
        Dialog(onDismissRequest = { showCategoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LocalMotokoColors.current.surfaceCard,
                modifier = Modifier
                    .width(280.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(options.size) { index ->
                                val option = options[index]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(
                                            color = if (option == selectedCategory || (selectedCategory.isEmpty() && option == allCategoryText)) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onCategorySelected(if (option == allCategoryText) "" else option)
                                            showCategoryDialog = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalMotokoColors.current.textOnDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(id = AppR.string.dialog_close),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary,
                        modifier = Modifier.clickable { showCategoryDialog = false }
                    )
                }
            }
        }
    }
}

/**
 * Botón selector de filtro con texto e icono ChevronDown.
 */
@Composable
private fun HistoryFilterChip(label: String, descRes: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(LocalMotokoColors.current.surfaceCard, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textPrimary
        )
        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
            contentDescription = stringResource(id = descRes),
            tint = LocalMotokoColors.current.textPrimary,
            modifier = Modifier.size(12.dp)
        )
    }
}

// ---------------------------------------------------------------------------

@Composable
fun DashboardCategoryCard(
    iconRes: Int,
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    val fontScale = LocalDensity.current.fontScale
    val titleFontSize = if (fontScale > 1.2f) {
        (14 / fontScale).sp
    } else {
        14.sp
    }
    val amountFontSize = if (fontScale > 1.2f) {
        (12 / fontScale).sp
    } else {
        12.sp
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = LocalMotokoColors.current.iconOnDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = amount,
                    fontSize = amountFontSize,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun SpeedDialItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = LocalMotokoColors.current.primaryDark,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        modifier = modifier
            .wrapContentSize()
            .bounceClick(pressedScale = 0.94f, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.iconOnDark,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = LocalMotokoColors.current.textOnDark
            )
        }
    }
}
