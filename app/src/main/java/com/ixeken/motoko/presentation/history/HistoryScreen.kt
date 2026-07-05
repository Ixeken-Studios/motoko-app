package com.ixeken.motoko.presentation.history

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import com.ixeken.motoko.ui.theme.MotokoAnimation

// ---------------------------------------------------------------------------
// Modelos de datos estáticos para la maquetación visual de prueba
// ---------------------------------------------------------------------------

/** Tipo de movimiento para determinar color y signo del monto. */
enum class TxType { INCOME, EXPENSE }

/** Representa una fila de transacción dentro de un grupo de fecha. */
data class HistoryItem(
    val nameRes: Int = 0,
    val categoryRes: Int = 0,
    val iconRes: Int,
    val amount: String,
    val type: TxType,
    val nameStr: String? = null,
    val categoryStr: String? = null,
    val domainTransaction: com.ixeken.motoko.domain.model.Transaction? = null
)

/** Agrupa transacciones bajo un título de fecha. */
data class HistoryGroup(
    val dateText: String,
    val items: List<HistoryItem>
)

// ---------------------------------------------------------------------------
// Datos estáticos de prueba (se sustituirán por flujos Room en la siguiente sesión)
// ---------------------------------------------------------------------------

internal val staticGroups: List<HistoryGroup> by lazy {
    listOf(
        HistoryGroup(
            dateText = "Today",
            items = listOf(
                HistoryItem(AppR.string.item_ice_cream,           AppR.string.cat_food,          R.drawable.lucide_ic_apple,              "75.00",   TxType.EXPENSE),
                HistoryItem(AppR.string.history_item_internet,    AppR.string.cat_house,         R.drawable.lucide_ic_house,              "381.00",  TxType.EXPENSE),
                HistoryItem(AppR.string.history_item_cinema,      AppR.string.cat_entertainment, R.drawable.lucide_ic_ticket,             "99.00",   TxType.EXPENSE),
                HistoryItem(AppR.string.history_item_metro,       AppR.string.cat_transport,     R.drawable.lucide_ic_train_front,        "75.00",   TxType.EXPENSE),
                HistoryItem(AppR.string.item_xbox_sale,           AppR.string.cat_income,        R.drawable.lucide_ic_circle_dollar_sign, "500.00",  TxType.INCOME)
            )
        ),
        HistoryGroup(
            dateText = "Yesterday",
            items = listOf(
                HistoryItem(AppR.string.history_item_rp_league,     AppR.string.cat_gaming,        R.drawable.lucide_ic_gamepad_2,       "349.00",  TxType.EXPENSE),
                HistoryItem(AppR.string.history_item_youtube_music, AppR.string.cat_subscriptions, R.drawable.lucide_ic_clapperboard,    "149.00",  TxType.EXPENSE),
                HistoryItem(AppR.string.history_item_smartphone,    AppR.string.cat_shopping,      R.drawable.lucide_ic_shopping_cart,   "9999.00", TxType.EXPENSE)
            )
        )
    )
}

// ---------------------------------------------------------------------------
// Pantalla principal - solo contenido scrollable
// ---------------------------------------------------------------------------

/**
 * Contenido scrollable de la pantalla de Historial.
 * La cabecera oscura (título, búsqueda, filtros) la gestiona MainScreen
 * mediante HistoryHeaderContent, igual que DashboardHeaderContent.
 *
 * @param isPrivacyEnabled cuando es verdadero, los montos se ocultan con la máscara • • •
 * @param searchQuery texto actual del campo de búsqueda (reservado para conexión Room)
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    isPrivacyEnabled: Boolean,
    searchQuery: String,
    coloredElementsEnabled: Boolean,
    currencySymbol: String,
    swipeToDeleteEnabled: Boolean = true,
    onSwipeDelete: (transactionId: Long) -> Unit = {},
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val timeStr = when (timeFilter) {
        HistoryTimeFilter.ALL -> stringResource(id = AppR.string.history_filter_time_all)
        HistoryTimeFilter.TODAY -> stringResource(id = AppR.string.history_filter_time_today)
        HistoryTimeFilter.YESTERDAY -> stringResource(id = AppR.string.history_filter_time_yesterday)
        HistoryTimeFilter.THIS_WEEK -> stringResource(id = AppR.string.history_filter_time_week)
        HistoryTimeFilter.THIS_MONTH -> stringResource(id = AppR.string.history_filter_time_month)
        HistoryTimeFilter.THIS_YEAR -> stringResource(id = AppR.string.history_filter_time_year)
    }

    val secondaryStr = if (categoryFilter.isNotEmpty()) {
        categoryFilter
    } else {
        when (typeFilter) {
            HistoryTypeFilter.ALL -> stringResource(id = AppR.string.history_filter_type_all)
            HistoryTypeFilter.INCOME -> stringResource(id = AppR.string.history_filter_type_income)
            HistoryTypeFilter.EXPENSE -> stringResource(id = AppR.string.history_filter_type_expense)
        }
    }

    val filterSummary = "$timeStr | $secondaryStr"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Etiqueta de estado alineada a la derecha como primer item del scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = filterSummary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }
        val groups = when (val state = uiState) {
            is HistoryUiState.Success -> state.groups
            else -> emptyList()
        }
        if (groups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = AppR.string.history_no_transactions),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                }
            }
        } else {
            groups.forEach { group ->
                // Título de fecha suelto sobre fondo gris (fuera de la Card)
                item(key = "header_${group.dateText}") {
                    Text(
                        text = group.dateText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                // Cada transacción como item individual con SwipeToDismissBox
                items(
                    group.items,
                    key = { it.domainTransaction?.id ?: (it.nameStr ?: it.nameRes.toString()) + "_" + it.amount + "_" + it.type.name }
                ) { item: HistoryItem ->

                    // Estado local por ítem para el diálogo de confirmación de borrado
                    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.animateItem(placementSpec = MotokoAnimation.screenSpec())) {
                        val isSwipeAllowedForItem = swipeToDeleteEnabled && item.categoryStr?.equals("Subscriptions", ignoreCase = true) != true
                        if (isSwipeAllowedForItem) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                positionalThreshold = { it * 0.4f }
                            )

                            // Detecta cuando el gesto completa el umbral y muestra el diálogo
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteConfirmDialog = true
                                    dismissState.reset() // el ítem regresa mientras el diálogo está abierto
                                }
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                LocalMotokoColors.current.colorExpense,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(end = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.lucide_ic_trash_2),
                                            contentDescription = stringResource(id = AppR.string.desc_history_clear),
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            ) {
                                TransactionCard(
                                    item = item,
                                    isPrivacyEnabled = isPrivacyEnabled,
                                    dateText = group.dateText,
                                    coloredElementsEnabled = coloredElementsEnabled,
                                    currencySymbol = currencySymbol,
                                    onTransactionClick = onTransactionClick
                                )
                            }
                        } else {
                            // Swipe desactivado: se muestra la card directamente sin SwipeToDismissBox
                            TransactionCard(
                                item = item,
                                isPrivacyEnabled = isPrivacyEnabled,
                                dateText = group.dateText,
                                coloredElementsEnabled = coloredElementsEnabled,
                                currencySymbol = currencySymbol,
                                onTransactionClick = onTransactionClick
                            )
                        }
                    }

                    // Diálogo de confirmación de borrado (compartido entre swipe y futuras acciones)
                    if (showDeleteConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = false },
                            title = {
                                Text(
                                    text = stringResource(AppR.string.swipe_delete_confirm_title),
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textPrimary
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(AppR.string.swipe_delete_confirm_message),
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    fontSize = 14.sp,
                                    color = LocalMotokoColors.current.textMuted
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showDeleteConfirmDialog = false
                                        item.domainTransaction?.let { tx ->
                                            onSwipeDelete(tx.id)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = stringResource(AppR.string.btn_yes),
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalMotokoColors.current.colorExpense
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                    Text(
                                        text = stringResource(AppR.string.btn_no),
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
    }
}

// ---------------------------------------------------------------------------
// Componentes internos del contenido scrollable
// ---------------------------------------------------------------------------

/**
 * Card de transacción reutilizable. Usada tanto con SwipeToDismissBox como sin él.
 */
@Composable
private fun TransactionCard(
    item: HistoryItem,
    isPrivacyEnabled: Boolean,
    dateText: String,
    coloredElementsEnabled: Boolean,
    currencySymbol: String,
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalMotokoColors.current.surfaceCard
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            HistoryTransactionRow(
                item = item,
                isPrivacyEnabled = isPrivacyEnabled,
                dateText = dateText,
                coloredElementsEnabled = coloredElementsEnabled,
                currencySymbol = currencySymbol,
                onTransactionClick = onTransactionClick
            )
        }
    }
}

/**
 * Fila individual de transacción: icono oscuro 48dp, nombre, categoría,
 * monto con color por tipo y ChevronRight.
 */
@Composable
private fun HistoryTransactionRow(
    item: HistoryItem,
    isPrivacyEnabled: Boolean,
    dateText: String,
    coloredElementsEnabled: Boolean,
    currencySymbol: String,
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit
) {
    val amountText = if (isPrivacyEnabled) {
        "• • •"
    } else {
        val sign = if (item.type == TxType.INCOME) "+" else "-"
        "$sign$currencySymbol ${item.amount}"
    }
    val amountColor = if (coloredElementsEnabled) {
        if (item.type == TxType.INCOME) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.colorExpense
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val resolvedName = item.nameStr ?: context.getString(item.nameRes)
                val resolvedCategory = item.categoryStr ?: context.getString(item.categoryRes)
                val resolvedWallet = when (item.domainTransaction?.wallet) {
                    com.ixeken.motoko.data.local.WalletType.CASH -> "Cash"
                    com.ixeken.motoko.data.local.WalletType.SAVINGS -> "Savings"
                    com.ixeken.motoko.data.local.WalletType.BANK -> "Debit Card"
                    null -> if (item.type == TxType.INCOME) "Cash" else "Debit Card"
                }
                val resolvedAccount = item.domainTransaction?.accountId?.toString() ?: context.getString(AppR.string.sub_bottom_sheet_account_personal)
                val mappedType = if (item.type == TxType.INCOME) {
                    com.ixeken.motoko.presentation.newitem.ItemType.INCOME
                } else {
                    com.ixeken.motoko.presentation.newitem.ItemType.EXPENSE
                }
                
                val iconName = item.domainTransaction?.iconName ?: when (item.iconRes) {
                    R.drawable.lucide_ic_apple -> "Apple"
                    R.drawable.lucide_ic_house -> "House"
                    R.drawable.lucide_ic_ticket -> "Ticket"
                    R.drawable.lucide_ic_train_front -> "Train"
                    R.drawable.lucide_ic_circle_dollar_sign -> "Dollar"
                    R.drawable.lucide_ic_gamepad_2 -> "Gamepad"
                    R.drawable.lucide_ic_clapperboard -> "Clapperboard"
                    R.drawable.lucide_ic_shopping_cart -> "ShoppingCart"
                    else -> "Dollar"
                }

                val noteText = item.domainTransaction?.note ?: when (item.nameRes) {
                    AppR.string.item_ice_cream -> "Delicious ice cream"
                    AppR.string.history_item_internet -> "Monthly internet bill"
                    AppR.string.history_item_cinema -> "Cinema tickets with friends"
                    AppR.string.history_item_metro -> "Metro card recharge"
                    AppR.string.item_xbox_sale -> "Sold my old console"
                    AppR.string.history_item_rp_league -> "League of Legends RP recharge"
                    AppR.string.history_item_youtube_music -> "Youtube Music premium subscription"
                    AppR.string.history_item_smartphone -> "Brand new smartphone purchase"
                    else -> ""
                }

                val receiptPath = item.domainTransaction?.receiptPath ?: ""
                onTransactionClick(
                    item.domainTransaction?.id ?: 0L,
                    resolvedName,
                    if (item.type == TxType.INCOME) "+$currencySymbol ${item.amount}" else "-$currencySymbol ${item.amount}",
                    mappedType,
                    resolvedWallet,
                    resolvedCategory,
                    dateText,
                    noteText,
                    resolvedAccount,
                    iconName,
                    receiptPath
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.iconOnDark,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.nameStr ?: stringResource(id = item.nameRes),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.categoryStr ?: stringResource(id = item.categoryRes),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = LocalMotokoColors.current.textMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amountText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = amountColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.lucide_ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
