package com.ixeken.motoko.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Filtro por tipo de transacción para el historial.
 */
enum class HistoryTypeFilter {
    ALL,
    INCOME,
    EXPENSE
}

/**
 * Filtro por período de tiempo para el historial.
 */
enum class HistoryTimeFilter {
    ALL,
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR
}

/**
 * Estado visual para la pantalla del Historial.
 */
sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(
        val groups: List<HistoryGroup> = emptyList()
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

/**
 * ViewModel que gestiona los datos de negocio y estado visual del Historial.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _typeFilter = MutableStateFlow(HistoryTypeFilter.ALL)
    val typeFilter: StateFlow<HistoryTypeFilter> = _typeFilter

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val tx = repository.getTransactionById(transactionId) ?: return@launch
            repository.deleteTransaction(tx)
        }
    }

    fun setTypeFilter(filter: HistoryTypeFilter) {
        _typeFilter.value = filter
    }

    private val _timeFilter = MutableStateFlow(HistoryTimeFilter.ALL)
    val timeFilter: StateFlow<HistoryTimeFilter> = _timeFilter

    fun setTimeFilter(filter: HistoryTimeFilter) {
        _timeFilter.value = filter
    }

    private val _categoryFilter = MutableStateFlow("")
    val categoryFilter: StateFlow<String> = _categoryFilter

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

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

    private fun mapIconNameToDrawable(iconName: String?): Int {
        return com.ixeken.motoko.presentation.resolveIconRes(iconName ?: "Folder")
    }

    private fun getGroupDateText(timestamp: Long): String {
        val now = java.util.Calendar.getInstance()
        val time = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        
        val isSameYear = now.get(java.util.Calendar.YEAR) == time.get(java.util.Calendar.YEAR)
        val isSameDayOfYear = now.get(java.util.Calendar.DAY_OF_YEAR) == time.get(java.util.Calendar.DAY_OF_YEAR)
        
        if (isSameYear && isSameDayOfYear) {
            return "Today"
        }
        
        val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(java.util.Calendar.YEAR) == time.get(java.util.Calendar.YEAR) &&
                yesterday.get(java.util.Calendar.DAY_OF_YEAR) == time.get(java.util.Calendar.DAY_OF_YEAR)
                
        if (isYesterday) {
            return "Yesterday"
        }
        
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun getTimeFilterStartTime(filter: HistoryTimeFilter): Long? {
        if (filter == HistoryTimeFilter.ALL) return null
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return when (filter) {
            HistoryTimeFilter.TODAY -> calendar.timeInMillis
            HistoryTimeFilter.YESTERDAY -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                calendar.timeInMillis
            }
            HistoryTimeFilter.THIS_WEEK -> {
                calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.timeInMillis
            }
            HistoryTimeFilter.THIS_MONTH -> {
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            HistoryTimeFilter.THIS_YEAR -> {
                calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            HistoryTimeFilter.ALL -> null
        }
    }

    val uiState: StateFlow<HistoryUiState> = activeAccountIdFlow
        .flatMapLatest { accountId ->
            combine(
                repository.getAllTransactions(accountId),
                _searchQuery,
                _typeFilter,
                _timeFilter,
                _categoryFilter,
                preferences.categoryIcons
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val transactions = args[0] as List<com.ixeken.motoko.domain.model.Transaction>
                val query = args[1] as String
                val typeFilter = args[2] as HistoryTypeFilter
                val timeFilter = args[3] as HistoryTimeFilter
                val categoryFilter = args[4] as String
                @Suppress("UNCHECKED_CAST")
                val categoryIconsMap = args[5] as Map<String, String>

                val typeFiltered = when (typeFilter) {
                    HistoryTypeFilter.ALL -> transactions
                    HistoryTypeFilter.INCOME -> transactions.filter { it.isIncome }
                    HistoryTypeFilter.EXPENSE -> transactions.filter { !it.isIncome }
                }

                val timeStart = getTimeFilterStartTime(timeFilter)
                val timeFiltered = if (timeStart != null) {
                    typeFiltered.filter { it.timestamp >= timeStart }
                } else {
                    typeFiltered
                }

                val categoryFiltered = if (categoryFilter.isEmpty()) {
                    timeFiltered
                } else {
                    timeFiltered.filter { it.category.equals(categoryFilter, ignoreCase = true) }
                }

                val filtered = if (query.isBlank()) {
                    categoryFiltered
                } else {
                    categoryFiltered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        (it.note?.contains(query, ignoreCase = true) == true)
                    }
                }

                val groupedMap = filtered.groupBy { getGroupDateText(it.timestamp) }
                val groups = groupedMap.map { (dateText, txList) ->
                    HistoryGroup(
                        dateText = dateText,
                        items = txList.map { tx ->
                            val finalIcon = categoryIconsMap[tx.category] ?: tx.iconName
                            HistoryItem(
                                nameStr = tx.title,
                                categoryStr = tx.category,
                                iconRes = mapIconNameToDrawable(finalIcon),
                                amount = String.format(java.util.Locale.US, "%.2f", tx.amount),
                                type = if (tx.isIncome) TxType.INCOME else TxType.EXPENSE,
                                domainTransaction = tx.copy(iconName = finalIcon)
                            )
                        }
                    )
                }

                HistoryUiState.Success(groups = groups)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState.Loading
        )
}
