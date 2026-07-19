package com.ixeken.motoko.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import com.ixeken.motoko.presentation.resolveIconRes
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import com.ixeken.motoko.ui.theme.LocalAnimationsEnabled
import com.ixeken.motoko.ui.theme.MotokoAnimation
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Pantalla que representa el Dashboard con maquetación estática premium.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    isPrivacyEnabled: Boolean,
    hideBudgetCard: Boolean,
    coloredElementsEnabled: Boolean,
    currencySymbol: String,
    walletsList: List<String> = emptyList(),
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showWalletDialog by remember { mutableStateOf(false) }
    val isWide = com.ixeken.motoko.presentation.isWideScreen()

    val budgetSection = @Composable {
        if (!hideBudgetCard) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = AppR.string.dashboard_budget),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showWalletDialog = true }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.selectedWallet.isEmpty()) stringResource(id = AppR.string.dashboard_budget_total) else uiState.selectedWallet,
                            color = LocalMotokoColors.current.textOnDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_chevron_down),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.iconOnDark,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.primaryDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_waves),
                                contentDescription = null,
                                tint = LocalMotokoColors.current.iconOnDark.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(id = AppR.string.dashboard_budget_card_name),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textOnDark.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Icon(
                                painter = painterResource(id = com.composables.icons.lucide.R.drawable.lucide_ic_nfc),
                                contentDescription = null,
                                tint = LocalMotokoColors.current.iconOnDark.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = AppR.string.dashboard_budget_balance),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalMotokoColors.current.textOnDark.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val balanceColor = if (!isPrivacyEnabled && uiState.totalBalance < 0.0 && coloredElementsEnabled) {
                                    LocalMotokoColors.current.colorExpense
                                } else {
                                    LocalMotokoColors.current.textOnDark
                                }
                                Text(
                                    text = if (isPrivacyEnabled) "$currencySymbol • • •" else com.ixeken.motoko.util.CurrencyFormatter.format(uiState.totalBalance, currencySymbol, isPrivacyEnabled = false, abbreviate = true),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = balanceColor
                                )
                            }

                            Text(
                                text = uiState.balanceDate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = LocalMotokoColors.current.textOnDark.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    val latestSection = @Composable {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = AppR.string.dashboard_latest),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.recentTransactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = stringResource(id = AppR.string.dashboard_no_transactions),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                }
            } else {
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
                        uiState.recentTransactions.forEachIndexed { index, tx ->
                            val animState = remember(tx.id) { Animatable(0f) }
                            val animationsEnabled = LocalAnimationsEnabled.current
                            val animSpec = MotokoAnimation.screenSpec<Float>()
                            LaunchedEffect(tx.id) {
                                if (animationsEnabled) {
                                    kotlinx.coroutines.delay(index * 50L)
                                }
                                animState.animateTo(
                                    targetValue = 1f,
                                    animationSpec = animSpec
                                )
                            }

                            val amountText = if (isPrivacyEnabled) {
                                "• • •"
                            } else {
                                val sign = if (tx.isIncome) "+" else "-"
                                val formattedAmt = String.format(java.util.Locale.US, "%.2f", tx.amount)
                                "$sign$currencySymbol $formattedAmt"
                            }
                            val amountColor = if (coloredElementsEnabled) {
                                if (tx.isIncome) LocalMotokoColors.current.colorIncome else LocalMotokoColors.current.colorExpense
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                            val formattedDate = try {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(tx.timestamp))
                            } catch (e: Exception) {
                                ""
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        alpha = animState.value
                                        translationY = (1f - animState.value) * 50f
                                    }
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val sign = if (tx.isIncome) "+" else "-"
                                        val amtFormatted = String.format(java.util.Locale.US, "%.2f", tx.amount)
                                        val amtStr = "$sign$currencySymbol $amtFormatted"
                                        val itemType = if (tx.isIncome) com.ixeken.motoko.presentation.newitem.ItemType.INCOME else com.ixeken.motoko.presentation.newitem.ItemType.EXPENSE
                                        val walletStr = com.ixeken.motoko.data.local.resolveWalletName(tx.wallet, walletsList, context)
                                        val accountStr = tx.accountId.toString()
                                        onTransactionClick(
                                            tx.id,
                                            tx.title,
                                            amtStr,
                                            itemType,
                                            walletStr,
                                            tx.category,
                                            formattedDate,
                                            tx.note ?: "",
                                            accountStr,
                                            tx.iconName,
                                            tx.receiptPath ?: ""
                                        )
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val categoryIcon = resolveIconRes(tx.iconName)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = categoryIcon),
                                        contentDescription = null,
                                        tint = LocalMotokoColors.current.iconOnDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${tx.category} • ${com.ixeken.motoko.data.local.resolveWalletName(tx.wallet, walletsList, context)}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LocalMotokoColors.current.textMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = amountText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor
                                )
                            }

                            if (index < uiState.recentTransactions.lastIndex) {
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

    if (isWide) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(LocalMotokoColors.current.primaryLight)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                budgetSection()
            }
            Box(modifier = Modifier.weight(1f)) {
                latestSection()
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(LocalMotokoColors.current.primaryLight),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (!hideBudgetCard) {
                item { budgetSection() }
            }
            item { latestSection() }
        }
    }

    if (showWalletDialog) {
        val totalOption = stringResource(id = AppR.string.dashboard_budget_total)
        val options = listOf(totalOption) + walletsList
        Dialog(onDismissRequest = { showWalletDialog = false }) {
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
                        LazyColumn(
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
                                            color = if (option == uiState.selectedWallet || (uiState.selectedWallet.isEmpty() && option == totalOption)) LocalMotokoColors.current.activeTab else LocalMotokoColors.current.primaryDark,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setSelectedWallet(if (option == totalOption) "" else option)
                                            showWalletDialog = false
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
                        modifier = Modifier.clickable { showWalletDialog = false }
                    )
                }
            }
        }
    }
}
