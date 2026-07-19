package com.ixeken.motoko.presentation.settings

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import com.ixeken.motoko.ui.theme.LocalAnimationsEnabled
import com.ixeken.motoko.ui.theme.MotokoAnimation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.ixeken.motoko.R
import com.ixeken.motoko.presentation.responsiveWidth
import com.ixeken.motoko.ui.theme.LocalMotokoColors

// Targets for selection in manage screen
enum class ManageTarget {
    CATEGORY,
    WALLET,
    ACCOUNT
}

// Operational modes
enum class OperationMode {
    IDLE,
    EDIT,
    DELETE
}

// Space Mono Bold font loading read dynamically from the theme
private val SpaceMonoBoldFamily: FontFamily?
    @Composable
    get() = MaterialTheme.typography.bodyLarge.fontFamily

/**
 * Screen that handles the management of Categories, Wallets, and Accounts.
 * Allows creation, editing, and deletion operations with interactive states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    viewModel: ManageViewModel,
    onBackClick: () -> Unit,
    coloredElementsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setMode(OperationMode.IDLE)
        }
    }

    // State for bottom sheets
    var showBottomSheet by remember { mutableStateOf(false) }
    var isEditAction by remember { mutableStateOf(false) }
    var editItemName by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var selectedIconName by remember { mutableStateOf("Soup") }
    var showIconPicker by remember { mutableStateOf(false) }

    val handleItemClick = { itemName: String ->
        if (uiState.mode == OperationMode.DELETE) {
            viewModel.toggleSelectedItemForDelete(itemName)
        } else if (uiState.mode == OperationMode.EDIT) {
            editItemName = itemName
            nameInput = itemName
            if (uiState.activeTarget == ManageTarget.CATEGORY) {
                val currentPair = uiState.categoriesList.find { it.first == itemName }
                val matched = com.ixeken.motoko.presentation.curatedIcons.find { it.second == currentPair?.second }
                selectedIconName = matched?.first ?: "Folder"
            }
            isEditAction = true
            showBottomSheet = true
        }
    }

    val handleItemLongClick = { itemName: String ->
        viewModel.setMode(OperationMode.EDIT)
        editItemName = itemName
        nameInput = itemName
        if (uiState.activeTarget == ManageTarget.CATEGORY) {
            val currentPair = uiState.categoriesList.find { it.first == itemName }
            val matched = com.ixeken.motoko.presentation.curatedIcons.find { it.second == currentPair?.second }
            selectedIconName = matched?.first ?: "Folder"
        }
        isEditAction = true
        showBottomSheet = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveWidth(720.dp)
        ) {
            // Cabecera Oscura y Selección de Catálogo
            Surface(
                color = LocalMotokoColors.current.primaryDark,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(8.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(LocalMotokoColors.current.textOnDark, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (uiState.mode != OperationMode.IDLE) {
                                        viewModel.clearSelectedItemsForDelete()
                                        viewModel.setMode(OperationMode.IDLE)
                                    } else {
                                        onBackClick()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (uiState.mode != OperationMode.IDLE) LucideR.drawable.lucide_ic_x
                                         else LucideR.drawable.lucide_ic_arrow_left
                                ),
                                contentDescription = if (uiState.mode != OperationMode.IDLE) stringResource(id = R.string.manage_btn_cancel)
                                                     else stringResource(id = R.string.desc_back),
                                tint = LocalMotokoColors.current.primaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = when (uiState.mode) {
                                OperationMode.DELETE -> stringResource(id = R.string.manage_selected_count, uiState.selectedItemsForDelete.size)
                                OperationMode.EDIT -> stringResource(id = R.string.manage_mode_editing)
                                OperationMode.IDLE -> stringResource(id = R.string.settings_categories_title)
                            },
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (uiState.mode == OperationMode.DELETE) {
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(LocalMotokoColors.current.textOnDark, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.executeDeleteSelected()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = LucideR.drawable.lucide_ic_trash_2),
                                    contentDescription = stringResource(id = R.string.manage_mode_delete),
                                    tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Row con tres botones segmentados equitativos con icono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_category),
                            iconRes = LucideR.drawable.lucide_ic_layout_grid,
                            isActive = uiState.activeTarget == ManageTarget.CATEGORY,
                            onClick = { viewModel.setActiveTarget(ManageTarget.CATEGORY) },
                            modifier = Modifier.weight(1f)
                        )
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_wallet),
                            iconRes = LucideR.drawable.lucide_ic_wallet,
                            isActive = uiState.activeTarget == ManageTarget.WALLET,
                            onClick = { viewModel.setActiveTarget(ManageTarget.WALLET) },
                            modifier = Modifier.weight(1f)
                        )
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_account),
                            iconRes = LucideR.drawable.lucide_ic_user,
                            isActive = uiState.activeTarget == ManageTarget.ACCOUNT,
                            onClick = { viewModel.setActiveTarget(ManageTarget.ACCOUNT) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Barra de Modos de Operación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            isEditAction = false
                            nameInput = ""
                            selectedIconName = "Soup"
                            showBottomSheet = true
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_plus),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.manage_mode_new),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            viewModel.setMode(if (uiState.mode == OperationMode.EDIT) OperationMode.IDLE else OperationMode.EDIT)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.mode == OperationMode.EDIT) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_pencil),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.manage_mode_edit),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            viewModel.setMode(if (uiState.mode == OperationMode.DELETE) OperationMode.IDLE else OperationMode.DELETE)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.mode == OperationMode.DELETE) {
                            if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.primaryDark
                        } else {
                            LocalMotokoColors.current.primaryDark
                        }
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_trash_2),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.manage_mode_delete),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Listado Dinámico
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
            ) {
                val listSize = when (uiState.activeTarget) {
                    ManageTarget.CATEGORY -> uiState.categoriesList.size
                    ManageTarget.WALLET -> uiState.walletsList.size
                    ManageTarget.ACCOUNT -> uiState.accountsList.size
                }

                if (listSize > 0) {
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
                                when (uiState.activeTarget) {
                                    ManageTarget.CATEGORY -> {
                                        uiState.categoriesList.forEachIndexed { index, cat ->
                                            CategoryManageRow(
                                                name = cat.first,
                                                iconRes = cat.second,
                                                isSelected = uiState.selectedItemsForDelete.contains(cat.first) && uiState.mode == OperationMode.DELETE,
                                                index = index,
                                                onClick = { handleItemClick(cat.first) },
                                                onLongClick = { handleItemLongClick(cat.first) }
                                            )
                                            if (index < uiState.categoriesList.lastIndex) {
                                                ManageDashedDivider()
                                            }
                                        }
                                    }
                                    ManageTarget.WALLET -> {
                                        uiState.walletsList.forEachIndexed { index, wallet ->
                                            WalletManageRow(
                                                name = wallet,
                                                isSelected = uiState.selectedItemsForDelete.contains(wallet) && uiState.mode == OperationMode.DELETE,
                                                index = index,
                                                onClick = { handleItemClick(wallet) },
                                                onLongClick = { handleItemLongClick(wallet) }
                                            )
                                            if (index < uiState.walletsList.lastIndex) {
                                                ManageDashedDivider()
                                            }
                                        }
                                    }
                                    ManageTarget.ACCOUNT -> {
                                        uiState.accountsList.forEachIndexed { index, account ->
                                            AccountManageRow(
                                                name = account,
                                                isSelected = uiState.selectedItemsForDelete.contains(account) && uiState.mode == OperationMode.DELETE,
                                                index = index,
                                                onClick = { handleItemClick(account) },
                                                onLongClick = { handleItemLongClick(account) }
                                            )
                                            if (index < uiState.accountsList.lastIndex) {
                                                ManageDashedDivider()
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
    }

    // Modal de Creación y Edición Compartidos
    if (showBottomSheet) {
        val scope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val animateDismiss: (() -> Unit) -> Unit = { action ->
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    action()
                    showBottomSheet = false
                    showIconPicker = false
                }
            }
        }

        val stopSheetSwipeConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    return if (available.y > 0f) Offset(0f, available.y) else Offset.Zero
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                showIconPicker = false
            },
            sheetState = sheetState,
            containerColor = LocalMotokoColors.current.primaryLight,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    .nestedScroll(stopSheetSwipeConnection)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, _ -> }
                    }
            ) {
                val modalTitle = if (isEditAction) {
                    when (uiState.activeTarget) {
                        ManageTarget.CATEGORY -> stringResource(id = R.string.manage_modal_edit_category)
                        ManageTarget.WALLET -> stringResource(id = R.string.manage_modal_edit_wallet)
                        ManageTarget.ACCOUNT -> stringResource(id = R.string.manage_modal_edit_account)
                    }
                } else {
                    when (uiState.activeTarget) {
                        ManageTarget.CATEGORY -> stringResource(id = R.string.manage_modal_new_category)
                        ManageTarget.WALLET -> stringResource(id = R.string.manage_modal_new_wallet)
                        ManageTarget.ACCOUNT -> stringResource(id = R.string.manage_modal_new_account)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modalTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón Save (izq de X)
                    Box(
                        modifier = Modifier
                            .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (nameInput.isNotEmpty()) {
                                    animateDismiss {
                                        if (isEditAction) {
                                            // Edit
                                            if (uiState.activeTarget == ManageTarget.CATEGORY) {
                                                viewModel.editCategory(editItemName, nameInput, selectedIconName)
                                            } else if (uiState.activeTarget == ManageTarget.WALLET) {
                                                viewModel.editWallet(editItemName, nameInput, selectedIconName)
                                            } else {
                                                viewModel.editAccount(editItemName, nameInput, selectedIconName)
                                            }
                                            viewModel.setMode(OperationMode.IDLE)
                                        } else {
                                            // New
                                            if (uiState.activeTarget == ManageTarget.CATEGORY) {
                                                viewModel.addCategory(nameInput, selectedIconName)
                                            } else if (uiState.activeTarget == ManageTarget.WALLET) {
                                                viewModel.addWallet(nameInput, selectedIconName)
                                            } else {
                                                viewModel.addAccount(nameInput, selectedIconName)
                                            }
                                        }
                                    }
                                } else {
                                    animateDismiss {}
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_save),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.manage_btn_save),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Botón Close X (der)
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
                            contentDescription = stringResource(id = R.string.manage_btn_cancel),
                            tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Campo Name
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LocalMotokoColors.current.activeTab)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (nameInput.isEmpty()) {
                            Text(
                                text = "Name",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = LocalMotokoColors.current.textMuted
                                )
                            )
                        }
                        BasicTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
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

                // Selección de Icono (Se despliega únicamente al pulsar)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.new_item_label_icon),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LocalMotokoColors.current.activeTab)
                        .clickable { showIconPicker = !showIconPicker }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedIconName.isEmpty()) stringResource(id = R.string.manage_placeholder_select_icon) else selectedIconName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textOnDark
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = com.ixeken.motoko.presentation.newitem.getCategoryIconRes(selectedIconName)),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (showIconPicker) {
                    Spacer(modifier = Modifier.height(12.dp))
                    com.ixeken.motoko.presentation.newitem.IconTabContent(
                        selectedIcon = selectedIconName,
                        onIconSelect = {
                            selectedIconName = it
                            showIconPicker = false
                        }
                    )
                }


            }
        }
    }

    val showDeleteWarning by viewModel.showDeleteWarning.collectAsStateWithLifecycle()
    val deleteWarningText by viewModel.deleteWarningMessage.collectAsStateWithLifecycle()

    if (showDeleteWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteWarning() },
            title = {
                Text(
                    text = stringResource(id = R.string.manage_delete_confirmation_title),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = deleteWarningText,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 14.sp,
                    color = LocalMotokoColors.current.textMuted
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteWithCascade() }) {
                    Text(
                        text = stringResource(id = R.string.btn_delete),
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteWarning() }) {
                    Text(
                        text = stringResource(id = R.string.manage_btn_cancel),
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = LocalMotokoColors.current.surfaceCard
        )
    }
}

@Composable
private fun ManageSegmentButton(
    text: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit,
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun ManageDashedDivider() {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryManageRow(
    name: String,
    iconRes: Int,
    isSelected: Boolean,
    index: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val animState = remember(name) { Animatable(0f) }
    val animationsEnabled = LocalAnimationsEnabled.current
    val animSpec = MotokoAnimation.screenSpec<Float>()
    LaunchedEffect(name) {
        if (animationsEnabled) {
            kotlinx.coroutines.delay(index * 30L)
        }
        animState.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animState.value
                translationY = (1f - animState.value) * 40f
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) LocalMotokoColors.current.colorExpense.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense)
                else BorderStroke(0.dp, Color.Transparent),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.textOnDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textPrimary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WalletManageRow(
    name: String,
    isSelected: Boolean,
    index: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val animState = remember(name) { Animatable(0f) }
    val animationsEnabled = LocalAnimationsEnabled.current
    val animSpec = MotokoAnimation.screenSpec<Float>()
    LaunchedEffect(name) {
        if (animationsEnabled) {
            kotlinx.coroutines.delay(index * 30L)
        }
        animState.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    val iconRes = com.ixeken.motoko.presentation.newitem.getWalletIconRes(name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animState.value
                translationY = (1f - animState.value) * 40f
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) LocalMotokoColors.current.colorExpense.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense)
                else BorderStroke(0.dp, Color.Transparent),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.textOnDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textPrimary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountManageRow(
    name: String,
    isSelected: Boolean,
    index: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val animState = remember(name) { Animatable(0f) }
    val animationsEnabled = LocalAnimationsEnabled.current
    val animSpec = MotokoAnimation.screenSpec<Float>()
    LaunchedEffect(name) {
        if (animationsEnabled) {
            kotlinx.coroutines.delay(index * 30L)
        }
        animState.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    val iconRes = com.ixeken.motoko.presentation.newitem.getAccountIconRes(name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animState.value
                translationY = (1f - animState.value) * 40f
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(
                if (isSelected) LocalMotokoColors.current.colorExpense.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense)
                else BorderStroke(0.dp, Color.Transparent),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.textOnDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LocalMotokoColors.current.textPrimary
        )
    }
}
