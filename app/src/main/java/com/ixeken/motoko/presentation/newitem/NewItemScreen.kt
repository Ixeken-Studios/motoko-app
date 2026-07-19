package com.ixeken.motoko.presentation.newitem

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.composables.icons.lucide.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.presentation.responsiveWidth
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Pestañas o tipos de registros seleccionables.
 */
enum class ItemType {
    INCOME,
    EXPENSE,
    SUBSCRIPTION
}

/**
 * Pantalla de creación de registros con formulario reactivo y dinámico.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItemScreen(
    viewModel: NewItemViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.saveEvent.collect {
            onBackClick()
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetTab by remember { mutableStateOf(SelectorTab.WALLET) }
    var showBillingDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReceiptSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.onReceiptSelectedFromUri(context, uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onReceiptCaptured()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .responsiveWidth(600.dp)
        ) {
            // 1. Cabecera Oscura y Selector Tripartito
        Surface(
            color = LocalMotokoColors.current.primaryDark,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp)
            ) {
                // Fila Superior con Botón de Regreso y Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            painter = painterResource(id = R.drawable.lucide_ic_arrow_left),
                            contentDescription = stringResource(id = AppR.string.desc_back),
                            tint = LocalMotokoColors.current.primaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val headerTitleRes = when (uiState.selectedType) {
                        ItemType.INCOME -> AppR.string.new_item_title_income
                        ItemType.EXPENSE -> AppR.string.new_item_title_expense
                        ItemType.SUBSCRIPTION -> AppR.string.new_item_title_subscription
                    }
                    Text(
                        text = stringResource(id = headerTitleRes),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textOnDark
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selector Segmentado de Tres Pestañas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SegmentButton(
                        text = stringResource(id = AppR.string.new_item_tab_income),
                        isActive = uiState.selectedType == ItemType.INCOME,
                        onClick = {
                            hasAttemptedSave = false
                            viewModel.onTypeSelected(ItemType.INCOME)
                        },
                        iconRes = R.drawable.lucide_ic_arrow_down_left,
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        text = stringResource(id = AppR.string.new_item_tab_expense),
                        isActive = uiState.selectedType == ItemType.EXPENSE,
                        onClick = {
                            hasAttemptedSave = false
                            viewModel.onTypeSelected(ItemType.EXPENSE)
                        },
                        iconRes = R.drawable.lucide_ic_arrow_up_right,
                        modifier = Modifier.weight(1f)
                    )
                    SegmentButton(
                        text = stringResource(id = AppR.string.new_item_tab_subscription),
                        isActive = uiState.selectedType == ItemType.SUBSCRIPTION,
                        onClick = {
                            hasAttemptedSave = false
                            viewModel.onTypeSelected(ItemType.SUBSCRIPTION)
                        },
                        iconRes = R.drawable.lucide_ic_repeat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Área de Formulario Dinámico
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .animateContentSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState.selectedType) {
                ItemType.INCOME -> {
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_name),
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_name),
                            isError = hasAttemptedSave && uiState.name.isBlank(),
                            required = true
                        )
                    }
                    item {
                        AmountFormInput(
                            label = stringResource(id = AppR.string.new_item_label_amount),
                            value = uiState.amount,
                            onValueChange = { viewModel.onAmountChanged(it) },
                            currencySymbol = currencySymbol,
                            isError = hasAttemptedSave && (uiState.amount.isBlank() || (uiState.amount.toDoubleOrNull() ?: 0.0) <= 0.0),
                            required = true
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_icon),
                                value = uiState.icon,
                                onValueChange = { viewModel.onIconChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                iconRes = getCategoryIconRes(uiState.icon),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ICON
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_date),
                                value = uiState.date,
                                onValueChange = { viewModel.onDateChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                iconRes = R.drawable.lucide_ic_calendar,
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_wallet),
                                value = uiState.wallet,
                                onValueChange = { viewModel.onWalletChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                isError = hasAttemptedSave && uiState.wallet.isBlank(),
                                required = true,
                                iconRes = getWalletIconRes(uiState.wallet),
                                onClick = {
                                    bottomSheetTab = SelectorTab.WALLET
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_account),
                                value = uiState.account,
                                onValueChange = { viewModel.onAccountChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                iconRes = getAccountIconRes(uiState.account),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ACCOUNT
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_note),
                            value = uiState.note,
                            onValueChange = { viewModel.onNoteChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                        )
                    }
                }
                ItemType.EXPENSE -> {
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_name),
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_name),
                            isError = hasAttemptedSave && uiState.name.isBlank(),
                            required = true
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AmountFormInput(
                                label = stringResource(id = AppR.string.new_item_label_amount),
                                value = uiState.amount,
                                onValueChange = { viewModel.onAmountChanged(it) },
                                currencySymbol = currencySymbol,
                                isError = hasAttemptedSave && (uiState.amount.isBlank() || (uiState.amount.toDoubleOrNull() ?: 0.0) <= 0.0),
                                required = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_icon),
                                value = uiState.icon,
                                onValueChange = { viewModel.onIconChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                iconRes = getCategoryIconRes(uiState.icon),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ICON
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_category),
                                value = uiState.category,
                                onValueChange = { viewModel.onCategoryChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_category),
                                iconRes = getCategoryIconRes(uiState.category),
                                onClick = {
                                    bottomSheetTab = SelectorTab.CATEGORY
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_date),
                                value = uiState.date,
                                onValueChange = { viewModel.onDateChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                iconRes = R.drawable.lucide_ic_calendar,
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_wallet),
                                value = uiState.wallet,
                                onValueChange = { viewModel.onWalletChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                isError = hasAttemptedSave && uiState.wallet.isBlank(),
                                required = true,
                                iconRes = getWalletIconRes(uiState.wallet),
                                onClick = {
                                    bottomSheetTab = SelectorTab.WALLET
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_account),
                                value = uiState.account,
                                onValueChange = { viewModel.onAccountChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                iconRes = getAccountIconRes(uiState.account),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ACCOUNT
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_note),
                            value = uiState.note,
                            onValueChange = { viewModel.onNoteChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                        )
                    }
                    item {
                        val displayName = remember(uiState.receipt) {
                            if (uiState.receipt.isNotEmpty()) {
                                java.io.File(uiState.receipt).name
                            } else {
                                ""
                            }
                        }
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_receipt),
                            value = displayName,
                            onValueChange = { viewModel.onReceiptChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_receipt),
                            iconRes = R.drawable.lucide_ic_camera,
                            onClick = { showReceiptSourceDialog = true }
                        )
                    }
                }
                ItemType.SUBSCRIPTION -> {
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_name),
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_name),
                            isError = hasAttemptedSave && uiState.name.isBlank(),
                            required = true
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AmountFormInput(
                                label = stringResource(id = AppR.string.new_item_label_amount),
                                value = uiState.amount,
                                onValueChange = { viewModel.onAmountChanged(it) },
                                currencySymbol = currencySymbol,
                                isError = hasAttemptedSave && (uiState.amount.isBlank() || (uiState.amount.toDoubleOrNull() ?: 0.0) <= 0.0),
                                required = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_icon),
                                value = uiState.icon,
                                onValueChange = { viewModel.onIconChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                iconRes = getCategoryIconRes(uiState.icon),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ICON
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_date),
                                value = uiState.date,
                                onValueChange = { viewModel.onDateChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                iconRes = R.drawable.lucide_ic_calendar,
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_billing_period),
                                value = uiState.period,
                                onValueChange = { viewModel.onPeriodChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_billing_period),
                                iconRes = R.drawable.lucide_ic_chevron_down,
                                onClick = {
                                    showBillingDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_wallet),
                                value = uiState.wallet,
                                onValueChange = { viewModel.onWalletChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                isError = hasAttemptedSave && uiState.wallet.isBlank(),
                                required = true,
                                iconRes = getWalletIconRes(uiState.wallet),
                                onClick = {
                                    bottomSheetTab = SelectorTab.WALLET
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            NewFormInput(
                                label = stringResource(id = AppR.string.new_item_label_account),
                                value = uiState.account,
                                onValueChange = { viewModel.onAccountChanged(it) },
                                placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                iconRes = getAccountIconRes(uiState.account),
                                onClick = {
                                    bottomSheetTab = SelectorTab.ACCOUNT
                                    showBottomSheet = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    item {
                        NewFormInput(
                            label = stringResource(id = AppR.string.new_item_label_note),
                            value = uiState.note,
                            onValueChange = { viewModel.onNoteChanged(it) },
                            placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                        )
                    }
                }
            }

            // 3. Botón de Guardado Inferior
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            hasAttemptedSave = true
                            viewModel.saveItem()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(id = AppR.string.new_item_btn_save),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textOnDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_save),
                            contentDescription = stringResource(id = AppR.string.new_item_btn_save),
                            tint = LocalMotokoColors.current.iconOnDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

        if (showBottomSheet) {
            val isExpenseMode = bottomSheetTab == SelectorTab.CATEGORY || (uiState.selectedType == ItemType.EXPENSE && bottomSheetTab != SelectorTab.WALLET)
            val currentWalletOrCategoryValue = if (bottomSheetTab == SelectorTab.CATEGORY) uiState.category else uiState.wallet
            val sheetTitle = when (bottomSheetTab) {
                SelectorTab.CATEGORY -> stringResource(id = AppR.string.new_item_label_category)
                SelectorTab.WALLET -> stringResource(id = AppR.string.new_item_label_wallet)
                SelectorTab.ICON -> stringResource(id = AppR.string.new_item_label_icon)
                SelectorTab.ACCOUNT -> stringResource(id = AppR.string.new_item_label_account)
            }
            SelectorBottomSheet(
                title = sheetTitle,
                initialTab = bottomSheetTab,
                currentWalletOrCategory = currentWalletOrCategoryValue,
                currentIcon = uiState.icon,
                currentAccount = uiState.account,
                walletsList = uiState.wallets,
                categoriesList = uiState.categories,
                accountsList = uiState.accounts,
                onQuickCreate = { type, name ->
                    when (type) {
                        SelectorTab.WALLET -> viewModel.createNewWallet(name)
                        SelectorTab.CATEGORY -> viewModel.createNewCategory(name)
                        SelectorTab.ACCOUNT -> viewModel.createNewAccount(name)
                        else -> {}
                    }
                },
                onDismissRequest = { showBottomSheet = false },
                onConfirmSelection = { walletOrCategory, icon, account ->
                    if (bottomSheetTab == SelectorTab.CATEGORY) {
                        viewModel.onCategoryChanged(walletOrCategory)
                    } else {
                        viewModel.onWalletChanged(walletOrCategory)
                    }
                    viewModel.onIconChanged(icon)
                    viewModel.onAccountChanged(account)
                    showBottomSheet = false
                },
                isExpense = isExpenseMode
            )
        }

        if (showBillingDialog) {
            BillingPeriodDialog(
                currentPeriod = uiState.period,
                onPeriodSelected = {
                    viewModel.onPeriodChanged(it)
                    showBillingDialog = false
                },
                onDismissRequest = { showBillingDialog = false }
            )
        }

        if (showReceiptSourceDialog) {
            Dialog(onDismissRequest = { showReceiptSourceDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LocalMotokoColors.current.primaryDark,
                    modifier = Modifier
                        .width(280.dp)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Receipt Source",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textOnDark
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(LocalMotokoColors.current.activeTab, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showReceiptSourceDialog = false
                                    val uri = viewModel.createTempImageUri(context)
                                    if (uri != null) {
                                        tempPhotoUri = uri
                                        cameraLauncher.launch(uri)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Take Photo",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(LocalMotokoColors.current.activeTab, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showReceiptSourceDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Choose from Gallery",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = try {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    sdf.parse(uiState.date)?.time
                } catch (e: Exception) {
                    null
                } ?: System.currentTimeMillis()
            )
            val isThemeDark = LocalMotokoColors.current.textPrimary != Color(0xFF212529)
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = if (isThemeDark) LocalMotokoColors.current.textPrimary else LocalMotokoColors.current.primaryDark,
                onPrimary = if (isThemeDark) Color.Black else Color.White,
                surface = LocalMotokoColors.current.surfaceCard,
                surfaceContainer = LocalMotokoColors.current.surfaceCard,
                surfaceContainerHigh = LocalMotokoColors.current.surfaceCard,
                surfaceContainerHighest = LocalMotokoColors.current.surfaceCard,
                onSurface = LocalMotokoColors.current.textPrimary,
                onSurfaceVariant = LocalMotokoColors.current.textMuted,
                surfaceVariant = LocalMotokoColors.current.primaryLight,
                secondaryContainer = LocalMotokoColors.current.primaryLight,
                onSecondaryContainer = LocalMotokoColors.current.textPrimary
            )
        ) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply {
                                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                                    }
                                    viewModel.onDateChanged(formatter.format(java.util.Date(millis)))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text(
                                text = "Aceptar",
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isThemeDark) LocalMotokoColors.current.textPrimary else LocalMotokoColors.current.primaryDark
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(
                                text = "Cancelar",
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textMuted
                            )
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = LocalMotokoColors.current.surfaceCard,
                            titleContentColor = LocalMotokoColors.current.textPrimary,
                            headlineContentColor = LocalMotokoColors.current.textPrimary,
                            weekdayContentColor = LocalMotokoColors.current.textMuted,
                            subheadContentColor = LocalMotokoColors.current.textMuted,
                            navigationContentColor = LocalMotokoColors.current.textPrimary,
                            yearContentColor = LocalMotokoColors.current.textMuted,
                            selectedYearContentColor = LocalMotokoColors.current.textOnDark,
                            selectedYearContainerColor = LocalMotokoColors.current.primaryDark,
                            dayContentColor = LocalMotokoColors.current.textPrimary,
                            selectedDayContentColor = LocalMotokoColors.current.textOnDark,
                            selectedDayContainerColor = LocalMotokoColors.current.primaryDark,
                            todayContentColor = LocalMotokoColors.current.primaryDark,
                            todayDateBorderColor = LocalMotokoColors.current.primaryDark
                        )
                    )
                }
            }
        }
    }
}
@Composable
private fun SegmentButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    iconRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (isActive) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = LocalMotokoColors.current.textOnDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textOnDark
            )
        }
    }
}

@Composable
private fun AmountFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    currencySymbol: String,
    isError: Boolean = false,
    required: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isError) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onBackground
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.textMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isError) LocalMotokoColors.current.colorExpense.copy(alpha = 0.2f)
                    else LocalMotokoColors.current.activeTab
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currencySymbol,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textOnDark
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textMuted,
                            textAlign = TextAlign.End
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textOnDark,
                        textAlign = TextAlign.End,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                    ),
                    cursorBrush = SolidColor(LocalMotokoColors.current.textOnDark),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun NewFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    required: Boolean = false,
    iconRes: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onBackground
                )
            )
            if (required) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.textMuted
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isError) LocalMotokoColors.current.colorExpense.copy(alpha = 0.2f)
                    else LocalMotokoColors.current.activeTab
                )
                .let { if (onClick != null) it.clickable(onClick = onClick) else it },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textMuted
                        )
                    )
                }
                if (onClick != null) {
                    Text(
                        text = value,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 13.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textOnDark
                        )
                    )
                } else {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textOnDark,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                        ),
                        cursorBrush = SolidColor(LocalMotokoColors.current.textOnDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .background(LocalMotokoColors.current.textMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = LocalMotokoColors.current.iconOnDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Cuadro de dialogo de periodo de facturacion.
 */
@Composable
fun BillingPeriodDialog(
    currentPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
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
                    val monthlyText = stringResource(id = AppR.string.billing_period_monthly)
                    val annualText = stringResource(id = AppR.string.billing_period_annual)
                    
                    BillingDialogButton(
                        text = monthlyText,
                        isActive = currentPeriod == monthlyText,
                        onClick = { onPeriodSelected(monthlyText) }
                    )
                    BillingDialogButton(
                        text = annualText,
                        isActive = currentPeriod == annualText,
                        onClick = { onPeriodSelected(annualText) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(id = AppR.string.dialog_close),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = onDismissRequest)
                )
            }
        }
    }
}

/**
 * Boton de opcion para el dialogo de periodo de facturacion.
 */
@Composable
private fun BillingDialogButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = if (isActive) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textOnDark
        )
    }
}
