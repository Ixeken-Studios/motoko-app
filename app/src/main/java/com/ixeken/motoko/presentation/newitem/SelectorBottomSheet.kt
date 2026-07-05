package com.ixeken.motoko.presentation.newitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.ui.theme.LocalMotokoColors

/**
 * Pestañas internas del modal selector.
 */
enum class SelectorTab {
    WALLET,
    CATEGORY,
    ICON,
    ACCOUNT
}

/**
 * Componente ModalBottomSheet unificado para la selección de Wallet, Icon y Account.
 *
 * @param initialTab Pestaña que se mostrará activa al abrir el modal.
 * @param currentWallet Valor actual del campo Wallet.
 * @param currentIcon Valor actual del campo Icon.
 * @param currentAccount Valor actual del campo Account.
 * @param onDismissRequest Callback que se ejecuta cuando se cierra el modal.
 * @param onConfirmSelection Callback que retorna los nuevos valores seleccionados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorBottomSheet(
    title: String,
    initialTab: SelectorTab,
    currentWalletOrCategory: String,
    currentIcon: String,
    currentAccount: String,
    walletsList: List<String> = emptyList(),
    categoriesList: List<String> = emptyList(),
    accountsList: List<String> = emptyList(),
    onQuickCreate: (tab: SelectorTab, name: String) -> Unit = { _, _ -> },
    onDismissRequest: () -> Unit,
    onConfirmSelection: (walletOrCategory: String, icon: String, account: String) -> Unit,
    isExpense: Boolean = false,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(initialTab) }
    var isCreatingMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    // Estados locales para las selecciones temporales
    var selectedWalletOrCategory by remember { mutableStateOf(currentWalletOrCategory) }
    var selectedIcon by remember { mutableStateOf(currentIcon) }
    var selectedAccount by remember { mutableStateOf(currentAccount) }

    val titleText = if (isCreatingMode) {
        if (activeTab == SelectorTab.WALLET) {
            stringResource(id = AppR.string.sub_bottom_sheet_new_wallet)
        } else if (activeTab == SelectorTab.CATEGORY) {
            stringResource(id = AppR.string.sub_bottom_sheet_new_category)
        } else {
            stringResource(id = AppR.string.sub_bottom_sheet_new_account)
        }
    } else {
        title
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
            // 1. Título de la Sección
            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textPrimary
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 2. Fila Segmentada de Selección de Atributos (se oculta en modo creación)
            if (!isCreatingMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabSegmentButton(
                        text = if (isExpense) stringResource(id = AppR.string.new_item_label_category) else stringResource(id = AppR.string.new_item_label_wallet),
                        isActive = activeTab == SelectorTab.WALLET || activeTab == SelectorTab.CATEGORY,
                        onClick = { activeTab = if (isExpense) SelectorTab.CATEGORY else SelectorTab.WALLET },
                        modifier = Modifier.weight(1f)
                    )
                    TabSegmentButton(
                        text = stringResource(id = AppR.string.new_item_label_icon),
                        isActive = activeTab == SelectorTab.ICON,
                        onClick = { activeTab = SelectorTab.ICON },
                        modifier = Modifier.weight(1f)
                    )
                    TabSegmentButton(
                        text = stringResource(id = AppR.string.new_item_label_account),
                        isActive = activeTab == SelectorTab.ACCOUNT,
                        onClick = { activeTab = SelectorTab.ACCOUNT },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Contenido Dinámico según la pestaña activa o modo creación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                if (isCreatingMode) {
                    CreationFormContent(
                        tabType = activeTab,
                        nameInput = nameInput,
                        onNameChange = { nameInput = it }
                    )
                } else {
                    when (activeTab) {
                        SelectorTab.WALLET -> {
                            WalletTabContent(
                                walletsList = walletsList,
                                selectedWallet = selectedWalletOrCategory,
                                onWalletSelect = { selectedWalletOrCategory = it },
                                onNewWalletClick = { isCreatingMode = true }
                            )
                        }
                        SelectorTab.CATEGORY -> {
                            CategoryTabContent(
                                categoriesList = categoriesList,
                                selectedCategory = selectedWalletOrCategory,
                                onCategorySelect = { selectedWalletOrCategory = it },
                                onNewCategoryClick = { isCreatingMode = true }
                            )
                        }
                        SelectorTab.ICON -> {
                            IconTabContent(
                                selectedIcon = selectedIcon,
                                onIconSelect = { selectedIcon = it }
                            )
                        }
                        SelectorTab.ACCOUNT -> {
                            AccountTabContent(
                                accountsList = accountsList,
                                selectedAccount = selectedAccount,
                                onAccountSelect = { selectedAccount = it },
                                onNewAccountClick = { isCreatingMode = true }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Fila Inferior de Acciones Duales o Botón Único
            if (isCreatingMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Cancel
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable { isCreatingMode = false },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = AppR.string.btn_cancel),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Botón Save
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clickable {
                                if (nameInput.isNotBlank()) {
                                    onQuickCreate(activeTab, nameInput)
                                    if (activeTab == SelectorTab.WALLET || activeTab == SelectorTab.CATEGORY) {
                                        selectedWalletOrCategory = nameInput
                                    } else if (activeTab == SelectorTab.ACCOUNT) {
                                        selectedAccount = nameInput
                                    }
                                    nameInput = ""
                                    isCreatingMode = false
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
                                text = stringResource(id = AppR.string.new_item_btn_save),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(id = R.drawable.lucide_ic_save),
                                contentDescription = stringResource(id = AppR.string.new_item_btn_save),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable {
                            onConfirmSelection(selectedWalletOrCategory, selectedIcon, selectedAccount)
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
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = R.drawable.lucide_ic_save),
                            contentDescription = stringResource(id = AppR.string.new_item_btn_save),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Botón segmentado individual para la fila superior del modal.
 */
@Composable
private fun TabSegmentButton(
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
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

/**
 * Contenido específico de la pestaña Wallet.
 */
@Composable
private fun WalletTabContent(
    walletsList: List<String>,
    selectedWallet: String,
    onWalletSelect: (String) -> Unit,
    onNewWalletClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Tarjeta de Creación de Nueva Cartera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewWalletClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_wallet),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_wallet),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones de Selección de Carteras
        if (walletsList.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                walletsList.forEach { wallet ->
                    SelectionCard(
                        text = wallet,
                        isSelected = selectedWallet == wallet,
                        onClick = { onWalletSelect(wallet) }
                    )
                }
            }
        }
    }
}

/**
 * Catálogo curado de iconos relevantes para finanzas personales.
 * Se inicializa una sola vez y se reutiliza en cada recomposición.
 */
private val curatedIcons: List<Pair<String, Int>> = listOf(
    // Dinero y finanzas
    Pair("Dollar", R.drawable.lucide_ic_circle_dollar_sign),
    Pair("Wallet", R.drawable.lucide_ic_wallet),
    Pair("CreditCard", R.drawable.lucide_ic_credit_card),
    Pair("Receipt", R.drawable.lucide_ic_receipt),
    Pair("Coins", R.drawable.lucide_ic_coins),
    Pair("Banknote", R.drawable.lucide_ic_banknote),
    Pair("TrendingUp", R.drawable.lucide_ic_trending_up),
    Pair("TrendingDown", R.drawable.lucide_ic_trending_down),
    Pair("Percent", R.drawable.lucide_ic_percent),
    Pair("Tag", R.drawable.lucide_ic_tag),
    Pair("Briefcase", R.drawable.lucide_ic_briefcase),
    Pair("Store", R.drawable.lucide_ic_store),

    // Comida y bebida
    Pair("Soup", R.drawable.lucide_ic_soup),
    Pair("Pizza", R.drawable.lucide_ic_pizza),
    Pair("Apple", R.drawable.lucide_ic_apple),
    Pair("Coffee", R.drawable.lucide_ic_coffee),
    Pair("Wine", R.drawable.lucide_ic_wine),
    Pair("Beer", R.drawable.lucide_ic_beer),
    Pair("Utensils", R.drawable.lucide_ic_utensils),
    Pair("LeafyGreen", R.drawable.lucide_ic_leafy_green),

    // Transporte
    Pair("Train", R.drawable.lucide_ic_train_front),
    Pair("Car", R.drawable.lucide_ic_car),
    Pair("Bus", R.drawable.lucide_ic_bus),
    Pair("Bike", R.drawable.lucide_ic_bike),
    Pair("Plane", R.drawable.lucide_ic_plane),
    Pair("Ship", R.drawable.lucide_ic_ship),
    Pair("Fuel", R.drawable.lucide_ic_fuel),
    Pair("Truck", R.drawable.lucide_ic_truck),
    Pair("Navigation", R.drawable.lucide_ic_navigation),

    // Hogar y servicios
    Pair("House", R.drawable.lucide_ic_house),
    Pair("Plug", R.drawable.lucide_ic_plug),
    Pair("Wifi", R.drawable.lucide_ic_wifi),
    Pair("Lamp", R.drawable.lucide_ic_lamp),
    Pair("Key", R.drawable.lucide_ic_key),
    Pair("Wrench", R.drawable.lucide_ic_wrench),
    Pair("Hammer", R.drawable.lucide_ic_hammer),
    Pair("Warehouse", R.drawable.lucide_ic_warehouse),

    // Entretenimiento
    Pair("Ticket", R.drawable.lucide_ic_ticket),
    Pair("Gamepad", R.drawable.lucide_ic_gamepad_2),
    Pair("Music", R.drawable.lucide_ic_music),
    Pair("Film", R.drawable.lucide_ic_film),
    Pair("Headphones", R.drawable.lucide_ic_headphones),
    Pair("Tv", R.drawable.lucide_ic_tv),
    Pair("Trophy", R.drawable.lucide_ic_trophy),
    Pair("Medal", R.drawable.lucide_ic_medal),

    // Compras
    Pair("ShoppingCart", R.drawable.lucide_ic_shopping_cart),
    Pair("ShoppingBag", R.drawable.lucide_ic_shopping_bag),
    Pair("Gift", R.drawable.lucide_ic_gift),
    Pair("Package", R.drawable.lucide_ic_package),
    Pair("Gem", R.drawable.lucide_ic_gem),
    Pair("Scissors", R.drawable.lucide_ic_scissors),

    // Salud y bienestar
    Pair("Heart", R.drawable.lucide_ic_heart),
    Pair("Hospital", R.drawable.lucide_ic_hospital),
    Pair("Pill", R.drawable.lucide_ic_pill),
    Pair("Stethoscope", R.drawable.lucide_ic_stethoscope),
    Pair("Dumbbell", R.drawable.lucide_ic_dumbbell),
    Pair("Thermometer", R.drawable.lucide_ic_thermometer),

    // Educación y trabajo
    Pair("GraduationCap", R.drawable.lucide_ic_graduation_cap),
    Pair("Book", R.drawable.lucide_ic_book_open),
    Pair("School", R.drawable.lucide_ic_school),
    Pair("Pen", R.drawable.lucide_ic_pen),
    Pair("Laptop", R.drawable.lucide_ic_laptop),
    Pair("Monitor", R.drawable.lucide_ic_monitor),
    Pair("Phone", R.drawable.lucide_ic_phone),
    Pair("Printer", R.drawable.lucide_ic_printer),

    // Naturaleza y viajes
    Pair("Globe", R.drawable.lucide_ic_globe),
    Pair("Mountain", R.drawable.lucide_ic_mountain),
    Pair("Waves", R.drawable.lucide_ic_waves),
    Pair("Umbrella", R.drawable.lucide_ic_umbrella),
    Pair("Sun", R.drawable.lucide_ic_sun),
    Pair("Tent", R.drawable.lucide_ic_tent),
    Pair("Sailboat", R.drawable.lucide_ic_sailboat),
    Pair("MapPin", R.drawable.lucide_ic_map_pin),

    // Personas y mascotas
    Pair("User", R.drawable.lucide_ic_user),
    Pair("Users", R.drawable.lucide_ic_users),
    Pair("Baby", R.drawable.lucide_ic_baby),
    Pair("Cat", R.drawable.lucide_ic_cat),
    Pair("Dog", R.drawable.lucide_ic_dog),
    Pair("Fish", R.drawable.lucide_ic_fish),

    // Utilidades
    Pair("Folder", R.drawable.lucide_ic_folder_code),
    Pair("Camera", R.drawable.lucide_ic_camera),
    Pair("Shield", R.drawable.lucide_ic_shield),
    Pair("Lock", R.drawable.lucide_ic_lock),
    Pair("Star", R.drawable.lucide_ic_star),
    Pair("Zap", R.drawable.lucide_ic_zap),
    Pair("Flame", R.drawable.lucide_ic_flame),
    Pair("Rocket", R.drawable.lucide_ic_rocket),
    Pair("Flower", R.drawable.lucide_ic_flower)
)

/**
 * Contenido específico de la pestaña Icon con búsqueda funcional.
 */
@Composable
private fun IconTabContent(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredIcons = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            curatedIcons
        } else {
            curatedIcons.filter { (name, _) ->
                name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Barra de búsqueda funcional
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LocalMotokoColors.current.activeTab),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(id = AppR.string.search_icon_placeholder),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = LocalMotokoColors.current.textMuted
                        )
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
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
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(LocalMotokoColors.current.textMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lucide_ic_search),
                    contentDescription = null,
                    tint = LocalMotokoColors.current.textOnDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cuadrícula de iconos filtrada por búsqueda
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            items(filteredIcons) { (name, resId) ->
                val isSelected = selectedIcon == name
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = if (isSelected) LocalMotokoColors.current.textMuted else LocalMotokoColors.current.primaryDark,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onIconSelect(name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = resId),
                        contentDescription = name,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Contenido específico de la pestaña Account.
 */
@Composable
private fun AccountTabContent(
    accountsList: List<String>,
    selectedAccount: String,
    onAccountSelect: (String) -> Unit,
    onNewAccountClick: () -> Unit
) {
    val personalOption = stringResource(id = AppR.string.sub_bottom_sheet_account_personal)
    val workOption = stringResource(id = AppR.string.sub_bottom_sheet_account_work)
    val jointOption = stringResource(id = AppR.string.sub_bottom_sheet_account_joint)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Botón de Creación: New account
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewAccountClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_account),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_user_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Opciones: Personal, Work, Joint
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val listToShow = accountsList.ifEmpty { listOf(personalOption, workOption, jointOption) }
            listToShow.forEach { account ->
                AccountSelectionCard(
                    text = account,
                    isSelected = selectedAccount == account,
                    onClick = { onAccountSelect(account) }
                )
            }
        }
    }
}

/**
 * Tarjeta de selección de cuenta.
 */
@Composable
private fun AccountSelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.surfaceCard,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.textPrimary
            )
        )
    }
}

/**
 * Tarjeta genérica de opción de selección.
 */
@Composable
private fun SelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.surfaceCard,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.textPrimary
            )
        )
    }
}

/**
 * Formulario de creación interna para Wallet o Account o Category.
 */
@Composable
private fun CreationFormContent(
    tabType: SelectorTab,
    nameInput: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo Name
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = AppR.string.new_item_label_name),
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
                    .background(LocalMotokoColors.current.activeTab),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (nameInput.isEmpty()) {
                        Text(
                            text = stringResource(id = AppR.string.new_item_placeholder_name),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textMuted
                            )
                        )
                    }
                    BasicTextField(
                        value = nameInput,
                        onValueChange = onNameChange,
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
        }

        // Campo Icon
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = AppR.string.new_item_label_icon),
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
                    .background(LocalMotokoColors.current.activeTab),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = when (tabType) {
                            SelectorTab.WALLET -> stringResource(id = AppR.string.new_item_label_wallet)
                            SelectorTab.CATEGORY -> stringResource(id = AppR.string.sub_bottom_sheet_icon_folder)
                            else -> stringResource(id = AppR.string.new_item_label_account)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .background(LocalMotokoColors.current.textMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lucide_ic_chevron_down),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Contenido específico de la pestaña Category.
 */
@Composable
private fun CategoryTabContent(
    categoriesList: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onNewCategoryClick: () -> Unit
) {
    val foodOption = stringResource(id = AppR.string.cat_food)
    val servicesOption = stringResource(id = AppR.string.sub_bottom_sheet_cat_services)
    val entertainmentOption = stringResource(id = AppR.string.cat_entertainment)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Botón de Creación: New category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewCategoryClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_category),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_layout_grid),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de Selección: Food, Services, Entertainment
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val listToShow = categoriesList.ifEmpty { listOf(foodOption, servicesOption, entertainmentOption) }
            listToShow.forEach { category ->
                SelectionCard(
                    text = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelect(category) }
                )
            }
        }
    }
}
