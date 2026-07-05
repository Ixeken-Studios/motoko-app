package com.ixeken.motoko.presentation.settings

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.ixeken.motoko.R
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                            text = stringResource(id = R.string.manage_title),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Row con tres botones segmentados equitativos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_category),
                            isActive = uiState.activeTarget == ManageTarget.CATEGORY,
                            onClick = { viewModel.setActiveTarget(ManageTarget.CATEGORY) },
                            modifier = Modifier.weight(1f)
                        )
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_wallet),
                            isActive = uiState.activeTarget == ManageTarget.WALLET,
                            onClick = { viewModel.setActiveTarget(ManageTarget.WALLET) },
                            modifier = Modifier.weight(1f)
                        )
                        ManageSegmentButton(
                            text = stringResource(id = R.string.manage_tab_account),
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
                        Text(
                            text = stringResource(id = R.string.manage_mode_new),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
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
                        Text(
                            text = stringResource(id = R.string.manage_mode_edit),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
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
                        containerColor = if (uiState.mode == OperationMode.DELETE) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.primaryDark
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.manage_mode_delete),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            val groupedCategoryRows = remember(uiState.categoriesList) {
                val result = mutableListOf<List<Pair<String, Int>>>()
                var i = 0
                while (i < uiState.categoriesList.size) {
                    val current = uiState.categoriesList[i]
                    if (current.first.length > 10) {
                        result.add(listOf(current))
                        i++
                    } else {
                        if (i + 1 < uiState.categoriesList.size && uiState.categoriesList[i + 1].first.length <= 10) {
                            result.add(listOf(current, uiState.categoriesList[i + 1]))
                            i += 2
                        } else {
                            result.add(listOf(current))
                            i++
                        }
                    }
                }
                result
            }

            // Listado Dinámico
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (uiState.activeTarget) {
                    ManageTarget.CATEGORY -> {
                        groupedCategoryRows.forEach { rowItems ->
                            item {
                                if (rowItems.size == 1) {
                                    val cat = rowItems[0]
                                    CategoryManageCard(
                                        name = cat.first,
                                        iconRes = cat.second,
                                        isSelected = uiState.selectedItemsForDelete.contains(cat.first) && uiState.mode == OperationMode.DELETE,
                                        onClick = { handleItemClick(cat.first) }
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { cat ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                CategoryManageCard(
                                                    name = cat.first,
                                                    iconRes = cat.second,
                                                    isSelected = uiState.selectedItemsForDelete.contains(cat.first) && uiState.mode == OperationMode.DELETE,
                                                    onClick = { handleItemClick(cat.first) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ManageTarget.WALLET -> {
                        uiState.walletsList.forEach { wallet ->
                            item {
                                WalletManageCard(
                                    name = wallet,
                                    isSelected = uiState.selectedItemsForDelete.contains(wallet) && uiState.mode == OperationMode.DELETE,
                                    onClick = { handleItemClick(wallet) }
                                )
                            }
                        }
                    }
                    ManageTarget.ACCOUNT -> {
                        uiState.accountsList.forEach { account ->
                            item {
                                AccountManageCard(
                                    name = account,
                                    isSelected = uiState.selectedItemsForDelete.contains(account) && uiState.mode == OperationMode.DELETE,
                                    onClick = { handleItemClick(account) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lógica de Consolidación Inferior en base
        if (uiState.mode == OperationMode.EDIT || uiState.mode == OperationMode.DELETE) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable {
                            viewModel.clearSelectedItemsForDelete()
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.manage_btn_cancel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(2f)
                        .height(60.dp)
                        .clickable {
                            if (uiState.mode == OperationMode.DELETE) {
                                when (uiState.activeTarget) {
                                    ManageTarget.CATEGORY -> {
                                        viewModel.deleteCategories(uiState.selectedItemsForDelete)
                                        viewModel.clearSelectedItemsForDelete()
                                    }
                                    ManageTarget.WALLET -> {
                                        viewModel.requestDeleteWallets(uiState.selectedItemsForDelete)
                                    }
                                    ManageTarget.ACCOUNT -> {
                                        viewModel.requestDeleteAccounts(uiState.selectedItemsForDelete)
                                    }
                                }
                            }
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
                            text = stringResource(id = R.string.manage_btn_save),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = LucideR.drawable.lucide_ic_save),
                            contentDescription = stringResource(id = R.string.manage_btn_save),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal de Creación y Edición Compartidos
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

                Text(
                    text = modalTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Campo Name
                Text(
                    text = "Name",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textMuted
                            )
                        }
                        BasicTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textOnDark
                            ),
                            cursorBrush = SolidColor(LocalMotokoColors.current.textOnDark),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (uiState.activeTarget == ManageTarget.CATEGORY) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Icon",
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
                            .clickable { showIconPicker = !showIconPicker },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedIconName.isEmpty()) stringResource(id = R.string.manage_placeholder_select_icon) else selectedIconName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textOnDark,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .background(LocalMotokoColors.current.textMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_chevron_down),
                                contentDescription = null,
                                tint = LocalMotokoColors.current.textOnDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (showIconPicker) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val icons = com.ixeken.motoko.presentation.curatedIcons
                        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            items(icons.size) { index ->
                                val (name, res) = icons[index]
                                val isSelected = selectedIconName == name
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1.5f)
                                        .background(
                                            color = if (isSelected) LocalMotokoColors.current.textMuted else LocalMotokoColors.current.primaryDark,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedIconName = name
                                            showIconPicker = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = res),
                                        contentDescription = name,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Buttons (Cancel & Save)
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
                                showBottomSheet = false
                                showIconPicker = false
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(id = R.string.manage_btn_cancel),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                if (nameInput.isNotEmpty()) {
                                    if (isEditAction) {
                                        // Edit
                                        if (uiState.activeTarget == ManageTarget.CATEGORY) {
                                            viewModel.editCategory(editItemName, nameInput, selectedIconName)
                                        } else if (uiState.activeTarget == ManageTarget.WALLET) {
                                            viewModel.editWallet(editItemName, nameInput)
                                        } else {
                                            viewModel.editAccount(editItemName, nameInput)
                                        }
                                    } else {
                                        // New
                                        if (uiState.activeTarget == ManageTarget.CATEGORY) {
                                            viewModel.addCategory(nameInput, selectedIconName)
                                        } else if (uiState.activeTarget == ManageTarget.WALLET) {
                                            viewModel.addWallet(nameInput)
                                        } else {
                                            viewModel.addAccount(nameInput)
                                        }
                                    }
                                }
                                showBottomSheet = false
                                showIconPicker = false
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
                                text = stringResource(id = R.string.manage_btn_save),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textOnDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(id = LucideR.drawable.lucide_ic_save),
                                contentDescription = null,
                                tint = LocalMotokoColors.current.textOnDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
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
                        color = LocalMotokoColors.current.colorExpense
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
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun CategoryManageCard(
    name: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard),
        border = if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
}

@Composable
private fun WalletManageCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard),
        border = if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textPrimary
            )
        }
    }
}

@Composable
private fun AccountManageCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard),
        border = if (isSelected) BorderStroke(1.dp, LocalMotokoColors.current.colorExpense) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.textPrimary
            )
        }
    }
}
