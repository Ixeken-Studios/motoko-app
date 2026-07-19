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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import com.ixeken.motoko.ui.theme.LocalAnimationsEnabled
import com.ixeken.motoko.ui.theme.MotokoAnimation
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import com.ixeken.motoko.data.local.resolveWalletName

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
    walletsList: List<String> = emptyList(),
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

                // Toda la lista de transacciones de esta fecha agrupada en una sola Card (estilo ticket)
                item(key = "card_${group.dateText}") {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            group.items.forEachIndexed { index, item ->
                                var showDeleteConfirmDialog by remember { mutableStateOf(false) }

                                val isSwipeAllowedForItem = swipeToDeleteEnabled && item.categoryStr?.equals("Subscriptions", ignoreCase = true) != true
                                val itemKey = item.domainTransaction?.id ?: item.nameRes

                                val animState = remember(itemKey) { Animatable(0f) }
                                val animationsEnabled = LocalAnimationsEnabled.current
                                val animSpec = MotokoAnimation.screenSpec<Float>()
                                LaunchedEffect(itemKey) {
                                    if (animationsEnabled) {
                                        kotlinx.coroutines.delay(index * 40L)
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
                                            translationY = (1f - animState.value) * 40f
                                        }
                                ) {
                                    if (isSwipeAllowedForItem) {
                                        val coroutineScope = rememberCoroutineScope()
                                        val dismissState = rememberSwipeToDismissBoxState(
                                            positionalThreshold = { it * 0.4f },
                                            confirmValueChange = { value ->
                                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                                    showDeleteConfirmDialog = true
                                                    false
                                                } else {
                                                    false
                                                }
                                            }
                                        )

                                        SwipeToDismissBox(
                                            state = dismissState,
                                            enableDismissFromStartToEnd = false,
                                            enableDismissFromEndToStart = true,
                                            backgroundContent = {
                                                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled || dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else LocalMotokoColors.current.primaryDark,
                                                                RoundedCornerShape(12.dp)
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
                                            }
                                        ) {
                                            HistoryTransactionRow(
                                                item = item,
                                                isPrivacyEnabled = isPrivacyEnabled,
                                                dateText = group.dateText,
                                                coloredElementsEnabled = coloredElementsEnabled,
                                                currencySymbol = currencySymbol,
                                                walletsList = walletsList,
                                                onTransactionClick = onTransactionClick
                                            )
                                        }

                                        if (showDeleteConfirmDialog) {
                                            AlertDialog(
                                                onDismissRequest = {
                                                    showDeleteConfirmDialog = false
                                                    coroutineScope.launch { dismissState.reset() }
                                                },
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
                                                            coroutineScope.launch { dismissState.reset() }
                                                            item.domainTransaction?.let { tx ->
                                                                onSwipeDelete(tx.id)
                                                            }
                                                        }
                                                    ) {
                                                        Text(
                                                            text = stringResource(AppR.string.btn_yes),
                                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (coloredElementsEnabled) LocalMotokoColors.current.colorExpense else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                },
                                                dismissButton = {
                                                    TextButton(
                                                        onClick = {
                                                            showDeleteConfirmDialog = false
                                                            coroutineScope.launch { dismissState.reset() }
                                                        }
                                                    ) {
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
                                    } else {
                                        HistoryTransactionRow(
                                            item = item,
                                            isPrivacyEnabled = isPrivacyEnabled,
                                            dateText = group.dateText,
                                            coloredElementsEnabled = coloredElementsEnabled,
                                            currencySymbol = currencySymbol,
                                            walletsList = walletsList,
                                            onTransactionClick = onTransactionClick
                                        )
                                    }
                                }

                                if (index < group.items.lastIndex) {
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
                            }
                        }
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
    walletsList: List<String> = emptyList(),
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
            .clip(RoundedCornerShape(12.dp))
            .background(LocalMotokoColors.current.surfaceCard)
            .clickable {
                val resolvedName = item.nameStr ?: context.getString(item.nameRes)
                val resolvedCategory = item.categoryStr ?: context.getString(item.categoryRes)
                val resolvedWallet = resolveWalletName(item.domainTransaction?.wallet, walletsList, context)
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
            }
            .padding(vertical = 4.dp),
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.categoryStr ?: stringResource(id = item.categoryRes),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = LocalMotokoColors.current.textMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amountText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
