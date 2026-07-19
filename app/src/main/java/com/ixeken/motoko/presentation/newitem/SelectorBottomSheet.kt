package com.ixeken.motoko.presentation.newitem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
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
import com.ixeken.motoko.presentation.responsiveWidth
import com.ixeken.motoko.presentation.isWideScreen
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import androidx.compose.ui.text.style.TextOverflow

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
    coloredElementsEnabled: Boolean = true,
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
                .responsiveWidth(600.dp)
                .align(Alignment.CenterHorizontally)
                .wrapContentHeight()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            // 1. Título de la Sección y Botones (Cabecera Superior)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                if (isCreatingMode) {
                    // Botón Save (izq de X)
                    Box(
                        modifier = Modifier
                            .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (nameInput.isNotBlank()) {
                                    animateDismiss {
                                        onQuickCreate(activeTab, nameInput)
                                        val createdWalletOrCat = if (activeTab == SelectorTab.WALLET || activeTab == SelectorTab.CATEGORY) nameInput else selectedWalletOrCategory
                                        val createdAccount = if (activeTab == SelectorTab.ACCOUNT) nameInput else selectedAccount
                                        onConfirmSelection(createdWalletOrCat, selectedIcon, createdAccount)
                                        nameInput = ""
                                        isCreatingMode = false
                                    }
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
                                painter = painterResource(id = R.drawable.lucide_ic_save),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = AppR.string.new_item_btn_save),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Botón Close X (der)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (isCreatingMode) {
                                isCreatingMode = false
                            } else {
                                animateDismiss {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.lucide_ic_x),
                        contentDescription = stringResource(id = AppR.string.dialog_close),
                        tint = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Contenido Dinámico con interceptación de gestos en el cuerpo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .nestedScroll(stopSheetSwipeConnection)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, _ -> }
                    }
            ) {
                if (isCreatingMode) {
                    CreationFormContent(
                        tabType = activeTab,
                        nameInput = nameInput,
                        onNameChange = { nameInput = it },
                        selectedIcon = selectedIcon,
                        onIconSelect = { selectedIcon = it }
                    )
                } else {
                    when (activeTab) {
                        SelectorTab.WALLET -> {
                            WalletTabContent(
                                walletsList = walletsList,
                                selectedWallet = selectedWalletOrCategory,
                                onWalletSelect = { wallet ->
                                    animateDismiss {
                                        selectedWalletOrCategory = wallet
                                        onConfirmSelection(wallet, selectedIcon, selectedAccount)
                                    }
                                },
                                onNewWalletClick = { isCreatingMode = true }
                            )
                        }
                        SelectorTab.CATEGORY -> {
                            CategoryTabContent(
                                categoriesList = categoriesList,
                                selectedCategory = selectedWalletOrCategory,
                                onCategorySelect = { category ->
                                    animateDismiss {
                                        selectedWalletOrCategory = category
                                        onConfirmSelection(category, selectedIcon, selectedAccount)
                                    }
                                },
                                onNewCategoryClick = { isCreatingMode = true }
                            )
                        }
                        SelectorTab.ICON -> {
                            IconTabContent(
                                selectedIcon = selectedIcon,
                                onIconSelect = { icon ->
                                    animateDismiss {
                                        selectedIcon = icon
                                        onConfirmSelection(selectedWalletOrCategory, icon, selectedAccount)
                                    }
                                }
                            )
                        }
                        SelectorTab.ACCOUNT -> {
                            AccountTabContent(
                                accountsList = accountsList,
                                selectedAccount = selectedAccount,
                                onAccountSelect = { account ->
                                    animateDismiss {
                                        selectedAccount = account
                                        onConfirmSelection(selectedWalletOrCategory, selectedIcon, account)
                                    }
                                },
                                onNewAccountClick = { isCreatingMode = true }
                            )
                        }
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

internal fun getWalletIconRes(wallet: String): Int {
    if (wallet.isBlank()) return R.drawable.lucide_ic_wallet
    return curatedIcons.firstOrNull { it.first.equals(wallet, ignoreCase = true) }?.second
        ?: when (wallet.lowercase()) {
            "cash", "efectivo" -> R.drawable.lucide_ic_banknote
            "debit", "débito", "debit card", "tarjeta de débito", "card" -> R.drawable.lucide_ic_credit_card
            "savings", "ahorros", "ahorro" -> R.drawable.lucide_ic_coins
            else -> R.drawable.lucide_ic_wallet
        }
}

@Composable
private fun WalletGridItem(
    walletName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = getWalletIconRes(walletName)
    val circleBg = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark
    val iconTint = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.iconOnDark
    val itemShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(circleBg, shape = itemShape)
                .clip(itemShape)
                .then(
                    if (isSelected) Modifier.border(2.dp, LocalMotokoColors.current.colorIncome, itemShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = walletName,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = walletName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) LocalMotokoColors.current.textPrimary else LocalMotokoColors.current.textMuted
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
    val cashOption = stringResource(id = AppR.string.sub_bottom_sheet_option_cash)
    val cardOption = stringResource(id = AppR.string.sub_bottom_sheet_option_debit)
    val savingsOption = stringResource(id = AppR.string.sub_bottom_sheet_option_savings)
    val listToShow = walletsList.ifEmpty { listOf(cashOption, cardOption, savingsOption) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val itemsPerRow = if (isWideScreen()) 5 else 3
        val rows = listToShow.chunked(itemsPerRow)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { wallet ->
                        WalletGridItem(
                            walletName = wallet,
                            isSelected = selectedWallet == wallet,
                            onClick = { onWalletSelect(wallet) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(itemsPerRow - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Inferior: + Nueva cartera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewWalletClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_wallet),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

private data class IconCategory(
    val title: String,
    val icons: List<Pair<String, Int>>
)

private val categorizedIcons: List<IconCategory> = listOf(
    IconCategory(
        title = "Finance & Money",
        icons = listOf(
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
            Pair("Store", R.drawable.lucide_ic_store)
        )
    ),
    IconCategory(
        title = "Food & Drinks",
        icons = listOf(
            Pair("Soup", R.drawable.lucide_ic_soup),
            Pair("Pizza", R.drawable.lucide_ic_pizza),
            Pair("Apple", R.drawable.lucide_ic_apple),
            Pair("Coffee", R.drawable.lucide_ic_coffee),
            Pair("Wine", R.drawable.lucide_ic_wine),
            Pair("Beer", R.drawable.lucide_ic_beer),
            Pair("Utensils", R.drawable.lucide_ic_utensils),
            Pair("LeafyGreen", R.drawable.lucide_ic_leafy_green)
        )
    ),
    IconCategory(
        title = "Transport & Travel",
        icons = listOf(
            Pair("Train", R.drawable.lucide_ic_train_front),
            Pair("Car", R.drawable.lucide_ic_car),
            Pair("Bus", R.drawable.lucide_ic_bus),
            Pair("Bike", R.drawable.lucide_ic_bike),
            Pair("Plane", R.drawable.lucide_ic_plane),
            Pair("Ship", R.drawable.lucide_ic_ship),
            Pair("Fuel", R.drawable.lucide_ic_fuel),
            Pair("Truck", R.drawable.lucide_ic_truck),
            Pair("Navigation", R.drawable.lucide_ic_navigation),
            Pair("Globe", R.drawable.lucide_ic_globe),
            Pair("Mountain", R.drawable.lucide_ic_mountain),
            Pair("Waves", R.drawable.lucide_ic_waves),
            Pair("Umbrella", R.drawable.lucide_ic_umbrella),
            Pair("Sun", R.drawable.lucide_ic_sun),
            Pair("Tent", R.drawable.lucide_ic_tent),
            Pair("Sailboat", R.drawable.lucide_ic_sailboat),
            Pair("MapPin", R.drawable.lucide_ic_map_pin)
        )
    ),
    IconCategory(
        title = "Shopping",
        icons = listOf(
            Pair("ShoppingCart", R.drawable.lucide_ic_shopping_cart),
            Pair("ShoppingBag", R.drawable.lucide_ic_shopping_bag),
            Pair("Gift", R.drawable.lucide_ic_gift),
            Pair("Package", R.drawable.lucide_ic_package),
            Pair("Gem", R.drawable.lucide_ic_gem),
            Pair("Scissors", R.drawable.lucide_ic_scissors)
        )
    ),
    IconCategory(
        title = "Entertainment",
        icons = listOf(
            Pair("Ticket", R.drawable.lucide_ic_ticket),
            Pair("Gamepad", R.drawable.lucide_ic_gamepad_2),
            Pair("Music", R.drawable.lucide_ic_music),
            Pair("Film", R.drawable.lucide_ic_film),
            Pair("Headphones", R.drawable.lucide_ic_headphones),
            Pair("Tv", R.drawable.lucide_ic_tv),
            Pair("Trophy", R.drawable.lucide_ic_trophy),
            Pair("Medal", R.drawable.lucide_ic_medal)
        )
    ),
    IconCategory(
        title = "Home & Utilities",
        icons = listOf(
            Pair("House", R.drawable.lucide_ic_house),
            Pair("Plug", R.drawable.lucide_ic_plug),
            Pair("Wifi", R.drawable.lucide_ic_wifi),
            Pair("Lamp", R.drawable.lucide_ic_lamp),
            Pair("Key", R.drawable.lucide_ic_key),
            Pair("Wrench", R.drawable.lucide_ic_wrench),
            Pair("Hammer", R.drawable.lucide_ic_hammer),
            Pair("Warehouse", R.drawable.lucide_ic_warehouse),
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
    ),
    IconCategory(
        title = "Health & Wellness",
        icons = listOf(
            Pair("Heart", R.drawable.lucide_ic_heart),
            Pair("Hospital", R.drawable.lucide_ic_hospital),
            Pair("Pill", R.drawable.lucide_ic_pill),
            Pair("Stethoscope", R.drawable.lucide_ic_stethoscope),
            Pair("Dumbbell", R.drawable.lucide_ic_dumbbell),
            Pair("Thermometer", R.drawable.lucide_ic_thermometer)
        )
    ),
    IconCategory(
        title = "Education & Work",
        icons = listOf(
            Pair("GraduationCap", R.drawable.lucide_ic_graduation_cap),
            Pair("Book", R.drawable.lucide_ic_book_open),
            Pair("School", R.drawable.lucide_ic_school),
            Pair("Pen", R.drawable.lucide_ic_pen),
            Pair("Laptop", R.drawable.lucide_ic_laptop),
            Pair("Monitor", R.drawable.lucide_ic_monitor),
            Pair("Phone", R.drawable.lucide_ic_phone),
            Pair("Printer", R.drawable.lucide_ic_printer)
        )
    ),
    IconCategory(
        title = "People & Pets",
        icons = listOf(
            Pair("User", R.drawable.lucide_ic_user),
            Pair("Users", R.drawable.lucide_ic_users),
            Pair("Baby", R.drawable.lucide_ic_baby),
            Pair("Cat", R.drawable.lucide_ic_cat),
            Pair("Dog", R.drawable.lucide_ic_dog),
            Pair("Fish", R.drawable.lucide_ic_fish)
        )
    )
)

/**
 * Catálogo curado de iconos relevantes para finanzas personales.
 * Se inicializa una sola vez y se reutiliza en cada recomposición.
 */
private val curatedIcons: List<Pair<String, Int>> = categorizedIcons.flatMap { it.icons }

/**
 * Contenido específico de la pestaña Icon con búsqueda funcional y catálogo categorizado.
 */
@Composable
internal fun IconTabContent(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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
                        color = LocalMotokoColors.current.textOnDark,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
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

        Spacer(modifier = Modifier.height(16.dp))

        // Área de scroll de iconos ampliada con secciones
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (searchQuery.isNotBlank()) {
                val matchingIcons = curatedIcons.filter { (name, _) ->
                    name.contains(searchQuery, ignoreCase = true)
                }
                Text(
                    text = "Search results",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textPrimary
                    )
                )
                val rows = matchingIcons.chunked(6)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { iconRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            iconRow.forEach { (name, resId) ->
                                IconGridTile(
                                    name = name,
                                    resId = resId,
                                    isSelected = selectedIcon == name,
                                    onClick = { onIconSelect(name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(6 - iconRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                categorizedIcons.forEach { section ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textPrimary
                            )
                        )
                        val rows = section.icons.chunked(6)
                        rows.forEach { iconRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                iconRow.forEach { (name, resId) ->
                                    IconGridTile(
                                        name = name,
                                        resId = resId,
                                        isSelected = selectedIcon == name,
                                        onClick = { onIconSelect(name) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(6 - iconRow.size) {
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
private fun IconGridTile(
    name: String,
    resId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemShape = RoundedCornerShape(16.dp)
    val bg = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark
    val tint = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.iconOnDark

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(color = bg, shape = itemShape)
            .clip(itemShape)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, LocalMotokoColors.current.colorIncome, itemShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = name,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

internal fun getAccountIconRes(account: String): Int {
    if (account.isBlank()) return R.drawable.lucide_ic_user
    return curatedIcons.firstOrNull { it.first.equals(account, ignoreCase = true) }?.second
        ?: when (account.lowercase()) {
            "personal" -> R.drawable.lucide_ic_user
            "work", "trabajo" -> R.drawable.lucide_ic_briefcase
            "joint", "mancomunada", "compartida" -> R.drawable.lucide_ic_users
            else -> R.drawable.lucide_ic_user
        }
}

@Composable
private fun AccountGridItem(
    accountName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = getAccountIconRes(accountName)
    val circleBg = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark
    val iconTint = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.iconOnDark
    val itemShape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(circleBg, shape = itemShape)
                .clip(itemShape)
                .then(
                    if (isSelected) Modifier.border(2.dp, LocalMotokoColors.current.colorIncome, itemShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = accountName,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = accountName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) LocalMotokoColors.current.textPrimary else LocalMotokoColors.current.textMuted
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
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
    val listToShow = accountsList.ifEmpty { listOf(personalOption, workOption, jointOption) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val rows = listToShow.chunked(3)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { account ->
                        AccountGridItem(
                            accountName = account,
                            isSelected = selectedAccount == account,
                            onClick = { onAccountSelect(account) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Inferior: + Nueva cuenta
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewAccountClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_account),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
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
    selectedIcon: String = "Folder",
    onIconSelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showIconPicker by remember { mutableStateOf(false) }

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
                            color = Color.White,
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                        ),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Selección de Icono (Se despliega únicamente al pulsar)
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
                    .background(LocalMotokoColors.current.activeTab)
                    .clickable { showIconPicker = !showIconPicker }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedIcon.isEmpty()) stringResource(id = AppR.string.new_item_placeholder_icon) else selectedIcon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textOnDark,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = getCategoryIconRes(selectedIcon)),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (showIconPicker) {
                Spacer(modifier = Modifier.height(12.dp))
                IconTabContent(
                    selectedIcon = selectedIcon,
                    onIconSelect = {
                        onIconSelect(it)
                        showIconPicker = false
                    }
                )
            }
        }
    }
}

internal fun getCategoryIconRes(category: String): Int {
    if (category.isBlank()) return R.drawable.lucide_ic_layout_grid
    val directMatch = curatedIcons.firstOrNull { it.first.equals(category, ignoreCase = true) }?.second
    if (directMatch != null) return directMatch

    val iconName = defaultIconForCategory(category)
    val defaultMatch = curatedIcons.firstOrNull { it.first.equals(iconName, ignoreCase = true) }?.second
    if (defaultMatch != null) return defaultMatch

    return when (category) {
        "Food", "Restaurants" -> R.drawable.lucide_ic_soup
        "Groceries" -> R.drawable.lucide_ic_shopping_cart
        "House" -> R.drawable.lucide_ic_house
        "Utilities" -> R.drawable.lucide_ic_zap
        "Transport" -> R.drawable.lucide_ic_train_front
        "Health" -> R.drawable.lucide_ic_heart
        "Entertainment" -> R.drawable.lucide_ic_ticket
        "Subscriptions" -> R.drawable.lucide_ic_ticket
        "Gaming" -> R.drawable.lucide_ic_gamepad_2
        "Shopping" -> R.drawable.lucide_ic_shopping_bag
        "Education" -> R.drawable.lucide_ic_graduation_cap
        "Travel" -> R.drawable.lucide_ic_plane
        else -> R.drawable.lucide_ic_folder_code
    }
}

@Composable
private fun CategoryGridItem(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = getCategoryIconRes(categoryName)
    val circleBg = if (isSelected) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark
    val iconTint = if (isSelected) LocalMotokoColors.current.textOnDark else LocalMotokoColors.current.iconOnDark

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val itemShape = RoundedCornerShape(16.dp)
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(circleBg, shape = itemShape)
                .clip(itemShape)
                .then(
                    if (isSelected) Modifier.border(2.dp, LocalMotokoColors.current.colorIncome, itemShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = categoryName,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) LocalMotokoColors.current.textPrimary else LocalMotokoColors.current.textMuted
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
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
    val listToShow = categoriesList.ifEmpty { listOf(foodOption, servicesOption, entertainmentOption) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Cuadrícula de 3 columnas de categorías
        val rows = listToShow.chunked(3)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { category ->
                        CategoryGridItem(
                            categoryName = category,
                            isSelected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Inferior: + Nueva categoría
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(LocalMotokoColors.current.primaryDark, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNewCategoryClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = AppR.string.sub_bottom_sheet_new_category),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}
