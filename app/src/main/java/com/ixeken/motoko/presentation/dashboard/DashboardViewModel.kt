package com.ixeken.motoko.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa el período de tiempo seleccionado para filtrar el Dashboard.
 */
enum class DashboardPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR
}

/**
 * Representa el modo de visualización del contenido de categorías en la cabecera.
 */
enum class DashboardViewMode {
    CHART,
    PIE,
    LIST
}

data class DashboardCategoryItem(
    val name: String,
    val totalAmount: Double,
    val isIncome: Boolean,
    val iconName: String
)

/**
 * Estado visual para la pantalla del Dashboard.
 */
data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val balanceDate: String = "18/06",
    val expensesAmount: Double = 0.0,
    val incomeAmount: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categories: List<DashboardCategoryItem> = emptyList(),
    val isPrivacyEnabled: Boolean = false,
    val selectedPeriod: DashboardPeriod = DashboardPeriod.DAY,
    val selectedWallet: String = ""
)

/**
 * ViewModel que gestiona los datos de negocio y estado visual del Dashboard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val activeAccountIdFlow: Flow<Long> = preferences.activeAccountId
        .flatMapLatest { id ->
            if (id != null) {
                flowOf(id)
            } else {
                repository.getAccounts().map { accounts ->
                    accounts.firstOrNull()?.id ?: 1L
                }
            }
        }

    /**
     * Devuelve el período de tiempo actualmente seleccionado.
     */
    val selectedPeriod: StateFlow<DashboardPeriod> = preferences.dashboardPeriod
        .map { name ->
            runCatching { DashboardPeriod.valueOf(name) }.getOrDefault(DashboardPeriod.DAY)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardPeriod.DAY
        )

    /**
     * Establece el nuevo período de tiempo para filtrar el Dashboard.
     */
    fun setPeriod(period: DashboardPeriod) {
        viewModelScope.launch {
            preferences.setDashboardPeriod(period.name)
        }
    }

    val viewMode: StateFlow<DashboardViewMode> = preferences.dashboardViewMode
        .map { name ->
            runCatching { DashboardViewMode.valueOf(name) }.getOrDefault(DashboardViewMode.LIST)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardViewMode.LIST
        )

    fun setViewMode(mode: DashboardViewMode) {
        viewModelScope.launch {
            preferences.setDashboardViewMode(mode.name)
        }
    }

    private val _selectedWallet = MutableStateFlow("")
    val selectedWallet: StateFlow<String> = _selectedWallet.asStateFlow()

    /**
     * Establece el filtro de cartera seleccionada.
     */
    fun setSelectedWallet(wallet: String) {
        _selectedWallet.value = wallet
    }

    private val balanceFlow: Flow<Double?> = combine(
        activeAccountIdFlow,
        _selectedWallet
    ) { accountId, wallet ->
        Pair(accountId, wallet)
    }.flatMapLatest { (accountId, wallet) ->
        val walletType = if (wallet.isBlank()) {
            null
        } else {
            when (wallet.lowercase()) {
                "cash", "efectivo" -> WalletType.CASH
                "savings", "ahorros" -> WalletType.SAVINGS
                else -> WalletType.BANK
            }
        }
        if (walletType != null) {
            repository.getBalanceByWallet(accountId, walletType)
        } else {
            repository.getTotalBalance(accountId)
        }
    }

    private val walletAndPeriodFlow: Flow<Pair<String, DashboardPeriod>> = combine(
        _selectedWallet,
        selectedPeriod
    ) { wallet, period ->
        Pair(wallet, period)
    }

    val uiState: StateFlow<DashboardUiState> = activeAccountIdFlow
        .flatMapLatest { accountId ->
            combine(
                balanceFlow,
                repository.getAllTransactions(accountId),
                repository.getLatestTransactions(accountId),
                walletAndPeriodFlow,
                preferences.privacyMode,
                preferences.categoryIcons
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val totalBalance = args[0] as Double?
                @Suppress("UNCHECKED_CAST")
                val allTx = args[1] as List<Transaction>
                @Suppress("UNCHECKED_CAST")
                val latestTx = args[2] as List<Transaction>
                @Suppress("UNCHECKED_CAST")
                val walletAndPeriod = args[3] as Pair<String, DashboardPeriod>
                val (wallet, period) = walletAndPeriod
                val privacyEnabled = args[4] as Boolean
                @Suppress("UNCHECKED_CAST")
                val categoryIconsMap = args[5] as Map<String, String>

                val now = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)

                val startTime = when (period) {
                    DashboardPeriod.DAY -> calendar.timeInMillis
                    DashboardPeriod.WEEK -> {
                        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                        calendar.timeInMillis
                    }
                    DashboardPeriod.MONTH -> {
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                        calendar.timeInMillis
                    }
                    DashboardPeriod.YEAR -> {
                        calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                        calendar.timeInMillis
                    }
                }

                val periodTx = allTx.filter { it.timestamp >= startTime }
                val income = periodTx.filter { it.isIncome }.sumOf { it.amount }
                val expenses = periodTx.filter { !it.isIncome }.sumOf { it.amount }

                val balanceDate = if (latestTx.isNotEmpty()) {
                    val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(latestTx.first().timestamp))
                } else {
                    "18/06"
                }

                val categoryMap = periodTx.groupBy { if (it.isIncome) "Income" else it.category }
                val categories = categoryMap.map { (catName, txs) ->
                    val total = txs.sumOf { it.amount }
                    val isIncome = txs.any { it.isIncome }
                    val categoryIcon = categoryIconsMap[catName] ?: com.ixeken.motoko.presentation.newitem.defaultIconForCategory(catName)
                    DashboardCategoryItem(catName, total, isIncome, categoryIcon)
                }.sortedByDescending { it.totalAmount }

                DashboardUiState(
                    totalBalance = totalBalance ?: 0.0,
                    balanceDate = balanceDate,
                    expensesAmount = expenses,
                    incomeAmount = income,
                    recentTransactions = latestTx.map { tx ->
                        val finalIcon = categoryIconsMap[tx.category] ?: tx.iconName
                        tx.copy(iconName = finalIcon)
                    },
                    categories = categories,
                    isPrivacyEnabled = privacyEnabled,
                    selectedPeriod = period,
                    selectedWallet = wallet
                )
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
}
