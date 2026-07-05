package com.ixeken.motoko.presentation.newitem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import com.ixeken.motoko.presentation.main.MainViewModel
import kotlinx.coroutines.flow.first

/**
 * Mapeo de nombre de icono a recurso drawable.
 */
private fun getIconRes(name: String): Int {
    return when (name) {
        "Soup" -> R.drawable.lucide_ic_soup
        "Ticket" -> R.drawable.lucide_ic_ticket
        "Train" -> R.drawable.lucide_ic_train_front
        "Dollar" -> R.drawable.lucide_ic_circle_dollar_sign
        "Briefcase" -> R.drawable.lucide_ic_briefcase
        "Globe" -> R.drawable.lucide_ic_globe
        "CreditCard" -> R.drawable.lucide_ic_credit_card
        "Wallet" -> R.drawable.lucide_ic_wallet
        "Settings" -> R.drawable.lucide_ic_settings
        "Camera" -> R.drawable.lucide_ic_camera
        "Lock" -> R.drawable.lucide_ic_lock
        "Palette" -> R.drawable.lucide_ic_palette
        "Type" -> R.drawable.lucide_ic_type
        "Navigation" -> R.drawable.lucide_ic_navigation
        "Cat" -> R.drawable.lucide_ic_cat
        "Shield" -> R.drawable.lucide_ic_shield
        "FileText" -> R.drawable.lucide_ic_file_text
        "Refresh" -> R.drawable.lucide_ic_refresh_cw
        "Folder" -> R.drawable.lucide_ic_folder_code
        "User" -> R.drawable.lucide_ic_user
        "UserPlus" -> R.drawable.lucide_ic_user_plus
        "Search" -> R.drawable.lucide_ic_search
        "Plus" -> R.drawable.lucide_ic_plus
        "House" -> R.drawable.lucide_ic_house
        "Apple" -> R.drawable.lucide_ic_apple
        "Gamepad" -> R.drawable.lucide_ic_gamepad_2
        "Clapperboard" -> R.drawable.lucide_ic_clapperboard
        "ShoppingCart" -> R.drawable.lucide_ic_shopping_cart
        else -> R.drawable.lucide_ic_circle_dollar_sign
    }
}

/**
 * ModalBottomSheet unificado para ver detalles, editar y borrar un registro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailBottomSheet(
    id: Long = 0L,
    name: String,
    amount: String,
    type: ItemType,
    wallet: String,
    category: String,
    date: String,
    note: String,
    account: String,
    iconName: String,
    receiptPath: String = "",
    onDismissRequest: () -> Unit,
    onDeleteConfirmed: (dontShowAgain: Boolean) -> Unit,
    onSaveEdited: (name: String, amount: String, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String, billingPeriod: String?) -> Unit,
    walletsList: List<String> = listOf("Cash", "Debit Card", "Savings"),
    categoriesList: List<String> = listOf("Food", "House", "Entertainment", "Transport", "Gaming", "Shopping", "Others"),
    accountsList: List<String> = emptyList(),
    muteDeleteWarnings: Boolean = false,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var isEditingMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(name) }
    var editAmount by remember { mutableStateOf(amount) }
    var editWallet by remember { mutableStateOf(wallet) }
    var editCategory by remember { mutableStateOf(category) }
    var editDate by remember { mutableStateOf(date) }
    var editNote by remember { mutableStateOf(note) }
    var editAccount by remember { mutableStateOf(account) }
    var editIcon by remember { mutableStateOf(iconName) }
    var editReceipt by remember { mutableStateOf(receiptPath) }
    var editPeriod by remember { mutableStateOf(com.ixeken.motoko.data.local.BillingPeriod.MONTHLY) }

    var showSelectorSheet by remember { mutableStateOf(false) }
    var selectorTab by remember { mutableStateOf(SelectorTab.WALLET) }
    var showBillingDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReceiptSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            try {
                val cacheDir = context.cacheDir
                val tempFile = java.io.File.createTempFile("receipt_gallery_", ".jpg", cacheDir)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                editReceipt = tempFile.absolutePath
            } catch (e: Exception) { }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val path = tempPhotoUri?.let { uri ->
                try {
                    val cacheDir = context.cacheDir
                    val tempFile = java.io.File.createTempFile("receipt_", ".jpg", cacheDir)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.absolutePath
                } catch (e: Exception) { null }
            }
            if (path != null) {
                editReceipt = path
            }
        }
    }

    var walletsState by remember(walletsList) { mutableStateOf(walletsList) }
    var categoriesState by remember(categoriesList) { mutableStateOf(categoriesList) }
    var accountsState by remember(accountsList) { mutableStateOf(accountsList) }

    LaunchedEffect(account) {
        val accId = account.toLongOrNull()
        if (accId != null) {
            val accName = viewModel.getAccountNameById(accId)
            if (accName != null) {
                editAccount = accName
            }
        }
    }

    LaunchedEffect(id, type) {
        if (id > 0L) {
            if (type == ItemType.SUBSCRIPTION) {
                val sub = viewModel.getSubscriptionById(id)
                if (sub != null) {
                    editName = sub.name
                    editAmount = String.format(java.util.Locale.US, "%.2f", sub.amount)
                    editWallet = when (sub.wallet) {
                        com.ixeken.motoko.data.local.WalletType.CASH -> "Cash"
                        com.ixeken.motoko.data.local.WalletType.SAVINGS -> "Savings"
                        else -> "Debit Card"
                    }
                    editCategory = sub.category
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    editDate = sdf.format(java.util.Date(sub.startDate))
                    editPeriod = sub.billingPeriod
                    editNote = sub.note ?: ""
                    editAccount = viewModel.getAccountNameById(sub.accountId) ?: "Personal"
                    editIcon = sub.iconName
                }
            } else {
                val tx = viewModel.getTransactionById(id)
                if (tx != null) {
                    editName = tx.title
                    editAmount = String.format(java.util.Locale.US, "%.2f", tx.amount)
                    editWallet = when (tx.wallet) {
                        com.ixeken.motoko.data.local.WalletType.CASH -> "Cash"
                        com.ixeken.motoko.data.local.WalletType.SAVINGS -> "Savings"
                        else -> "Debit Card"
                    }
                    editCategory = tx.category
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    editDate = sdf.format(java.util.Date(tx.timestamp))
                    editNote = tx.note ?: ""
                    editAccount = viewModel.getAccountNameById(tx.accountId) ?: "Personal"
                    editIcon = tx.iconName
                    editReceipt = tx.receiptPath ?: ""
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LocalMotokoColors.current.primaryLight,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            if (!isEditingMode) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                // 1. Tarjeta Fija de Cabecera
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
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
                                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = getIconRes(iconName)),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (type) {
                                    ItemType.INCOME -> stringResource(id = AppR.string.new_item_tab_income)
                                    ItemType.EXPENSE -> stringResource(id = AppR.string.new_item_tab_expense)
                                    ItemType.SUBSCRIPTION -> stringResource(id = AppR.string.new_item_tab_subscription)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = amount,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (type == ItemType.INCOME) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.colorExpense
                            )
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_chevron_right),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Lista de Datos: Wallet/Category, Account, Note
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (type == ItemType.EXPENSE) {
                        DetailField(
                            label = stringResource(id = AppR.string.new_item_label_category),
                            value = category
                        )
                    }
                    DetailField(
                        label = stringResource(id = AppR.string.new_item_label_wallet),
                        value = wallet
                    )
                    DetailField(
                        label = stringResource(id = AppR.string.new_item_label_date),
                        value = date
                    )
                    if (type == ItemType.SUBSCRIPTION) {
                        DetailField(
                            label = stringResource(id = AppR.string.new_item_label_billing_period),
                            value = if (editPeriod == com.ixeken.motoko.data.local.BillingPeriod.ANNUAL) "Annual" else "Monthly"
                        )
                    }
                    if (note.isNotEmpty()) {
                        DetailField(
                            label = stringResource(id = AppR.string.new_item_label_note),
                            value = note
                        )
                    }
                    DetailField(
                        label = stringResource(id = AppR.string.new_item_label_account),
                        value = editAccount
                    )
                    if (editReceipt.isNotEmpty()) {
                        val receiptFile = java.io.File(editReceipt)
                        if (receiptFile.exists()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(id = AppR.string.new_item_label_receipt),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(LocalMotokoColors.current.activeTab, shape = RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val bitmap = remember(editReceipt) {
                                        android.graphics.BitmapFactory.decodeFile(editReceipt)
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Fila de Acciones: Edit y Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable { isEditingMode = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(id = AppR.string.btn_edit),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                if (muteDeleteWarnings) {
                                    onDeleteConfirmed(false)
                                    onDismissRequest()
                                } else {
                                    showDeleteDialog = true
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(id = AppR.string.btn_delete),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de cierre
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(onClick = onDismissRequest),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = AppR.string.dialog_close),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                }
            } else {
                // Formulario de Edición
                Text(
                    text = stringResource(id = AppR.string.edit_item_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (type) {
                        ItemType.INCOME -> {
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_name),
                                    value = editName,
                                    onValueChange = { editName = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_name)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_amount),
                                    value = editAmount,
                                    onValueChange = { editAmount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_amount)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_wallet),
                                    value = editWallet,
                                    onValueChange = { editWallet = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.WALLET
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_date),
                                    value = editDate,
                                    onValueChange = { editDate = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                    iconRes = R.drawable.lucide_ic_calendar,
                                    onClick = { showDatePicker = true }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_note),
                                    value = editNote,
                                    onValueChange = { editNote = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                                )
                            }
                            item {
                                val displayName = remember(editReceipt) {
                                    if (editReceipt.isNotEmpty()) java.io.File(editReceipt).name else ""
                                }
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_receipt),
                                    value = displayName,
                                    onValueChange = { },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_receipt),
                                    iconRes = R.drawable.lucide_ic_camera,
                                    onClick = { showReceiptSourceDialog = true }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_icon),
                                    value = editIcon,
                                    onValueChange = { editIcon = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ICON
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_account),
                                    value = editAccount,
                                    onValueChange = { editAccount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ACCOUNT
                                        showSelectorSheet = true
                                    }
                                )
                            }
                        }
                        ItemType.EXPENSE -> {
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_name),
                                    value = editName,
                                    onValueChange = { editName = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_name)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_amount),
                                    value = editAmount,
                                    onValueChange = { editAmount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_amount)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_category),
                                    value = editCategory,
                                    onValueChange = { editCategory = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_category),
                                    iconRes = R.drawable.lucide_ic_layout_grid,
                                    onClick = {
                                        selectorTab = SelectorTab.CATEGORY
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_wallet),
                                    value = editWallet,
                                    onValueChange = { editWallet = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.WALLET
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_date),
                                    value = editDate,
                                    onValueChange = { editDate = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                    iconRes = R.drawable.lucide_ic_calendar,
                                    onClick = { showDatePicker = true }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_note),
                                    value = editNote,
                                    onValueChange = { editNote = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                                )
                            }
                            item {
                                val displayName = remember(editReceipt) {
                                    if (editReceipt.isNotEmpty()) java.io.File(editReceipt).name else ""
                                }
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_receipt),
                                    value = displayName,
                                    onValueChange = { },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_receipt),
                                    iconRes = R.drawable.lucide_ic_camera,
                                    onClick = { showReceiptSourceDialog = true }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_icon),
                                    value = editIcon,
                                    onValueChange = { editIcon = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ICON
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_account),
                                    value = editAccount,
                                    onValueChange = { editAccount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ACCOUNT
                                        showSelectorSheet = true
                                    }
                                )
                            }
                        }
                        ItemType.SUBSCRIPTION -> {
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_name),
                                    value = editName,
                                    onValueChange = { editName = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_name)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_amount),
                                    value = editAmount,
                                    onValueChange = { editAmount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_amount)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_date),
                                    value = editDate,
                                    onValueChange = { editDate = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_date),
                                    iconRes = R.drawable.lucide_ic_calendar,
                                    onClick = { showDatePicker = true }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_billing_period),
                                    value = when (editPeriod) {
                                        com.ixeken.motoko.data.local.BillingPeriod.ANNUAL -> "Annual"
                                        else -> "Monthly"
                                    },
                                    onValueChange = { },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_billing_period),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        showBillingDialog = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_note),
                                    value = editNote,
                                    onValueChange = { editNote = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_note)
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_wallet),
                                    value = editWallet,
                                    onValueChange = { editWallet = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_wallet),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.WALLET
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_icon),
                                    value = editIcon,
                                    onValueChange = { editIcon = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_icon),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ICON
                                        showSelectorSheet = true
                                    }
                                )
                            }
                            item {
                                NewFormInput(
                                    label = stringResource(id = AppR.string.new_item_label_account),
                                    value = editAccount,
                                    onValueChange = { editAccount = it },
                                    placeholder = stringResource(id = AppR.string.new_item_placeholder_account),
                                    iconRes = R.drawable.lucide_ic_chevron_down,
                                    onClick = {
                                        selectorTab = SelectorTab.ACCOUNT
                                        showSelectorSheet = true
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fila de doble control: Cancel y Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                editName = name
                                editAmount = amount
                                editWallet = wallet
                                editCategory = category
                                editDate = date
                                editNote = note
                                editAccount = account
                                editIcon = iconName
                                editReceipt = receiptPath
                                isEditingMode = false
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(id = AppR.string.btn_cancel),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                onSaveEdited(
                                    editName,
                                    editAmount,
                                    editWallet,
                                    editCategory,
                                    editDate,
                                    editNote,
                                    editAccount,
                                    editIcon,
                                    editReceipt,
                                    if (type == ItemType.SUBSCRIPTION) {
                                        if (editPeriod == com.ixeken.motoko.data.local.BillingPeriod.ANNUAL) "Annual" else "Monthly"
                                    } else null
                                )
                                isEditingMode = false
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
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(id = R.drawable.lucide_ic_save),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = { dontShowAgain ->
                showDeleteDialog = false
                onDeleteConfirmed(dontShowAgain)
                onDismissRequest()
            },
            onDismissRequest = { showDeleteDialog = false }
        )
    }

    if (showSelectorSheet) {
        val isExpenseMode = selectorTab == SelectorTab.CATEGORY || (type == ItemType.EXPENSE && selectorTab != SelectorTab.WALLET)
        val currentWalletOrCategoryValue = if (selectorTab == SelectorTab.CATEGORY) editCategory else editWallet
        val sheetTitle = when (selectorTab) {
            SelectorTab.CATEGORY -> stringResource(id = AppR.string.new_item_label_category)
            SelectorTab.WALLET -> stringResource(id = AppR.string.new_item_label_wallet)
            SelectorTab.ICON -> stringResource(id = AppR.string.new_item_label_icon)
            SelectorTab.ACCOUNT -> stringResource(id = AppR.string.new_item_label_account)
        }
        SelectorBottomSheet(
            title = sheetTitle,
            initialTab = selectorTab,
            currentWalletOrCategory = currentWalletOrCategoryValue,
            currentIcon = editIcon,
            currentAccount = editAccount,
            walletsList = walletsState,
            categoriesList = categoriesState,
            accountsList = accountsState,
            onQuickCreate = { tab, name ->
                when (tab) {
                    SelectorTab.WALLET -> {
                        walletsState = walletsState + name
                    }
                    SelectorTab.CATEGORY -> {
                        categoriesState = categoriesState + name
                    }
                    SelectorTab.ACCOUNT -> {
                        viewModel.insertAccount(name)
                        accountsState = accountsState + name
                    }
                    else -> {}
                }
            },
            onDismissRequest = { showSelectorSheet = false },
            onConfirmSelection = { walletOrCategoryVal, iconVal, accountVal ->
                if (selectorTab == SelectorTab.CATEGORY) {
                    editCategory = walletOrCategoryVal
                } else {
                    editWallet = walletOrCategoryVal
                }
                editIcon = iconVal
                editAccount = accountVal
                showSelectorSheet = false
            },
            isExpense = isExpenseMode
        )
    }

    if (showBillingDialog) {
        BillingPeriodDialog(
            currentPeriod = if (editPeriod == com.ixeken.motoko.data.local.BillingPeriod.ANNUAL) "Annual" else "Monthly",
            onPeriodSelected = { period ->
                editPeriod = if (period.lowercase() == "annual") com.ixeken.motoko.data.local.BillingPeriod.ANNUAL else com.ixeken.motoko.data.local.BillingPeriod.MONTHLY
                showBillingDialog = false
            },
            onDismissRequest = { showBillingDialog = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                sdf.parse(editDate)?.time
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
                                editDate = formatter.format(java.util.Date(millis))
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
                                val uri = try {
                                    val cacheDir = context.cacheDir
                                    val tempFile = java.io.File.createTempFile("receipt_", ".jpg", cacheDir)
                                    tempPhotoUri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "com.ixeken.motoko.fileprovider",
                                        tempFile
                                    )
                                    tempPhotoUri
                                } catch (e: Exception) { null }
                                if (uri != null) {
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
}

/**
 * Caja de lectura estatica de metadatos.
 */
@Composable
private fun DetailField(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(LocalMotokoColors.current.activeTab, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White
            )
        }
    }
}

/**
 * Cuadro de dialogo para confirmacion de borrado.
 */
@Composable
fun DeleteConfirmationDialog(
    onConfirm: (dontShowAgain: Boolean) -> Unit,
    onDismissRequest: () -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LocalMotokoColors.current.surfaceCard,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(id = AppR.string.delete_confirm_message),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = AppR.string.delete_confirm_dont_show),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = stringResource(id = AppR.string.btn_no),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = { onConfirm(dontShowAgain) }) {
                        Text(
                            text = stringResource(id = AppR.string.btn_yes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Input para edicion.
 */
@Composable
private fun NewFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LocalMotokoColors.current.activeTab)
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = LocalMotokoColors.current.textMuted
                    )
                }
                if (onClick != null) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                } else {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(Color.White),
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
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
