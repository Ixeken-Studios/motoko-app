package com.ixeken.motoko.presentation.subscription

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R
import com.ixeken.motoko.R as AppR
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ixeken.motoko.ui.theme.LocalMotokoColors
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.ixeken.motoko.ui.theme.LocalAnimationsEnabled
import com.ixeken.motoko.ui.theme.MotokoAnimation

data class SubscriptionItem(
    val nameRes: Int = 0,
    val amount: String,
    val dateRes: Int = 0,
    val iconRes: Int,
    val nameStr: String? = null,
    val dateStr: String? = null,
    val domainSubscription: com.ixeken.motoko.domain.model.Subscription? = null
)

/**
 * Pantalla que representa las Suscripciones con maquetación estática premium.
 */
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionsViewModel,
    isPrivacyEnabled: Boolean,
    currencySymbol: String,
    billingFilter: String = "",
    walletsList: List<String> = emptyList(),
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rawSubscriptions = when (val state = uiState) {
        is SubscriptionsUiState.Success -> state.subscriptions
        else -> emptyList()
    }
    val monthlyLabel = stringResource(id = AppR.string.billing_period_monthly)
    val annualLabel = stringResource(id = AppR.string.billing_period_annual)
    val subscriptions = androidx.compose.runtime.remember(rawSubscriptions, billingFilter, monthlyLabel, annualLabel) {
        when (billingFilter) {
            monthlyLabel -> rawSubscriptions.filter { it.domainSubscription?.billingPeriod == com.ixeken.motoko.data.local.BillingPeriod.MONTHLY }
            annualLabel -> rawSubscriptions.filter { it.domainSubscription?.billingPeriod == com.ixeken.motoko.data.local.BillingPeriod.ANNUAL }
            else -> rawSubscriptions
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Etiqueta de Estado
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val statusLabel = if (billingFilter == stringResource(id = AppR.string.billing_period_annual)) {
                    stringResource(id = AppR.string.billing_period_annual)
                } else {
                    stringResource(id = AppR.string.sub_status_label)
                }
                Text(
                    text = statusLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalMotokoColors.current.textMuted
                )
            }
        }

        if (subscriptions.isEmpty()) {
            item {
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
                            text = stringResource(id = AppR.string.subscriptions_no_subscriptions),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                }
            }
        } else {
            // 2. Tarjeta Única que contiene todos los elementos
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
                        subscriptions.forEachIndexed { index, sub ->
                            SubscriptionRow(
                                sub = sub,
                                isPrivacyEnabled = isPrivacyEnabled,
                                currencySymbol = currencySymbol,
                                walletsList = walletsList,
                                index = index,
                                onTransactionClick = onTransactionClick
                            )

                            if (index < subscriptions.lastIndex) {
                                val lineColor = LocalMotokoColors.current.colorLines
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
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

@Composable
private fun SubscriptionRow(
    sub: SubscriptionItem,
    isPrivacyEnabled: Boolean,
    currencySymbol: String,
    walletsList: List<String> = emptyList(),
    index: Int = 0,
    onTransactionClick: (id: Long, name: String, amount: String, type: com.ixeken.motoko.presentation.newitem.ItemType, wallet: String, category: String, date: String, note: String, account: String, iconName: String, receiptPath: String) -> Unit
) {
    val animState = remember(sub.domainSubscription?.id ?: sub.nameStr ?: sub.nameRes) { Animatable(0f) }
    val animationsEnabled = LocalAnimationsEnabled.current
    val animSpec = MotokoAnimation.screenSpec<Float>()
    LaunchedEffect(sub.domainSubscription?.id ?: sub.nameStr ?: sub.nameRes) {
        if (animationsEnabled) {
            kotlinx.coroutines.delay(index * 40L)
        }
        animState.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    val amountText = if (isPrivacyEnabled) "• • •" else "$currencySymbol ${sub.amount}"
    val context = androidx.compose.ui.platform.LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animState.value
                translationY = (1f - animState.value) * 40f
            }
            .clickable {
                val resolvedName = sub.nameStr ?: context.getString(sub.nameRes)
                val resolvedWallet = com.ixeken.motoko.data.local.resolveWalletName(sub.domainSubscription?.wallet, walletsList, context)
                val resolvedAccount = sub.domainSubscription?.accountId?.toString() ?: "Personal"
                val billingPeriodStr = when (sub.domainSubscription?.billingPeriod) {
                    com.ixeken.motoko.data.local.BillingPeriod.ANNUAL -> "Annual"
                    else -> "Monthly"
                }
                val iconName = sub.domainSubscription?.iconName ?: "Dollar"
                val categoryName = sub.domainSubscription?.category ?: "Others"
                
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val formattedDate = sub.domainSubscription?.let { sdf.format(java.util.Date(it.startDate)) } ?: (sub.dateStr ?: "")

                onTransactionClick(
                    sub.domainSubscription?.id ?: 0L,
                    resolvedName,
                    "$currencySymbol ${sub.amount}",
                    com.ixeken.motoko.presentation.newitem.ItemType.SUBSCRIPTION,
                    resolvedWallet,
                    categoryName,
                    formattedDate,
                    sub.domainSubscription?.note ?: "",
                    resolvedAccount,
                    iconName,
                    ""
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Elemento Izquierdo: Icono
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = sub.iconRes),
                contentDescription = null,
                tint = LocalMotokoColors.current.iconOnDark,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Cuerpo Central: Nombre, monto y fecha de facturación
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = sub.nameStr ?: stringResource(id = sub.nameRes),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amountText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub.dateStr ?: stringResource(id = sub.dateRes),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = LocalMotokoColors.current.textMuted
            )
        }

        // Elemento Derecho: ChevronRight
        Icon(
            painter = painterResource(id = R.drawable.lucide_ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
    }
}
