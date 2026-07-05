package com.ixeken.motoko.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.ixeken.motoko.R
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.composables.icons.lucide.R as LucideR

/** Estado visual para la pantalla de gestión del catálogo. */
data class ManageUiState(
    val activeTarget: ManageTarget = ManageTarget.CATEGORY,
    val mode: OperationMode = OperationMode.IDLE,
    val categoriesList: List<Pair<String, Int>> = emptyList(),
    val walletsList: List<String> = emptyList(),
    val accountsList: List<String> = emptyList(),
    val selectedItemsForDelete: Set<String> = emptySet()
)

/**
 * ViewModel que gestiona las operaciones de catálogo en Ajustes (Categorías, Billeteras, Cuentas).
 * Persiste wallets y categorías en DataStore para que NewItemViewModel las consuma reactivamente.
 */
@HiltViewModel
class ManageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FinanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _activeTarget = MutableStateFlow(ManageTarget.CATEGORY)
    private val _mode = MutableStateFlow(OperationMode.IDLE)
    private val _selectedItemsForDelete = MutableStateFlow<Set<String>>(emptySet())
    private val _showDeleteWarning = MutableStateFlow(false)
    private val _deleteWarningMessage = MutableStateFlow("")
    private val _deleteWarningNames = MutableStateFlow<Set<String>>(emptySet())
    private val _deleteWarningTarget = MutableStateFlow(ManageTarget.CATEGORY)

    val showDeleteWarning: StateFlow<Boolean> = _showDeleteWarning.asStateFlow()
    val deleteWarningMessage: StateFlow<String> = _deleteWarningMessage.asStateFlow()
    val deleteWarningTarget: StateFlow<ManageTarget> = _deleteWarningTarget.asStateFlow()

    val uiState: StateFlow<ManageUiState> = combine(
        _activeTarget,
        _mode,
        userPreferences.categoriesCatalog,
        userPreferences.walletsCatalog,
        repository.getAccounts(),
        _selectedItemsForDelete,
        userPreferences.categoryIcons
    ) { flows ->
        val target = flows[0] as ManageTarget
        val mode = flows[1] as OperationMode
        @Suppress("UNCHECKED_CAST")
        val categoryNames = flows[2] as List<String>
        @Suppress("UNCHECKED_CAST")
        val wallets = flows[3] as List<String>
        @Suppress("UNCHECKED_CAST")
        val accounts = flows[4] as List<com.ixeken.motoko.domain.model.Account>
        @Suppress("UNCHECKED_CAST")
        val selectedForDelete = flows[5] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val categoryIconsMap = flows[6] as Map<String, String>

        ManageUiState(
            activeTarget = target,
            mode = mode,
            categoriesList = categoryNames.map { name ->
                val customIconName = categoryIconsMap[name]
                val iconRes = if (customIconName != null) {
                    com.ixeken.motoko.presentation.resolveIconRes(customIconName)
                } else {
                    iconResForCategory(name)
                }
                Pair(name, iconRes)
            },
            walletsList = wallets,
            accountsList = accounts.map { it.name },
            selectedItemsForDelete = selectedForDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManageUiState()
    )

    fun setActiveTarget(target: ManageTarget) {
        _activeTarget.value = target
        _selectedItemsForDelete.value = emptySet()
    }

    fun setMode(mode: OperationMode) {
        _mode.value = mode
        _selectedItemsForDelete.value = emptySet()
    }

    fun toggleSelectedItemForDelete(item: String) {
        _selectedItemsForDelete.update { set ->
            if (set.contains(item)) set - item else set + item
        }
    }

    fun clearSelectedItemsForDelete() {
        _selectedItemsForDelete.value = emptySet()
        _mode.value = OperationMode.IDLE
    }

    fun addCategory(name: String, iconName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.categoriesCatalog.first()
            if (!current.contains(name)) {
                userPreferences.setCategoriesCatalog(current + name)
            }
            userPreferences.setCategoryIcon(name, iconName)
        }
    }

    fun editCategory(oldName: String, newName: String, iconName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = userPreferences.categoriesCatalog.first()
                .map { if (it == oldName) newName else it }
            userPreferences.setCategoriesCatalog(updated)
            userPreferences.setCategoryIcon(newName, iconName)
        }
    }

    fun deleteCategories(names: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            names.forEach { name ->
                repository.reassignTransactionsCategory(name, "Others")
                repository.reassignSubscriptionsCategory(name, "Others")
            }
            val updated = userPreferences.categoriesCatalog.first()
                .filterNot { names.contains(it) }
            userPreferences.setCategoriesCatalog(updated)
        }
    }

    fun addWallet(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.walletsCatalog.first()
            if (!current.contains(name)) {
                userPreferences.setWalletsCatalog(current + name)
            }
        }
    }

    fun editWallet(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = userPreferences.walletsCatalog.first()
                .map { if (it == oldName) newName else it }
            userPreferences.setWalletsCatalog(updated)
        }
    }

    fun requestDeleteWallets(names: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            var totalCount = 0
            names.forEach { walletName ->
                val walletType = when (walletName.lowercase()) {
                    "cash", "efectivo" -> WalletType.CASH
                    "savings", "ahorros" -> WalletType.SAVINGS
                    "debit card", "tarjeta de débito", "débito", "debit" -> WalletType.BANK
                    else -> null
                }
                if (walletType != null) {
                    totalCount += repository.countTransactionsByWallet(walletType)
                    totalCount += repository.countSubscriptionsByWallet(walletType)
                }
            }
            if (totalCount > 0) {
                _deleteWarningMessage.value = context.getString(R.string.manage_delete_warning_wallet, totalCount)
                _deleteWarningNames.value = names
                _deleteWarningTarget.value = ManageTarget.WALLET
                _showDeleteWarning.value = true
            } else {
                deleteWalletsDirect(names)
            }
        }
    }

    private suspend fun deleteWalletsDirect(names: Set<String>) {
        val updated = userPreferences.walletsCatalog.first()
            .filterNot { names.contains(it) }
        userPreferences.setWalletsCatalog(updated)
    }

    fun confirmDeleteWithCascade() {
        val names = _deleteWarningNames.value
        val target = _deleteWarningTarget.value
        _showDeleteWarning.value = false
        viewModelScope.launch(Dispatchers.IO) {
            when (target) {
                ManageTarget.WALLET -> deleteWalletsDirect(names)
                ManageTarget.ACCOUNT -> {
                    val accounts = repository.getAccounts().first()
                    val targets = accounts.filter { names.contains(it.name) }
                    targets.forEach { account ->
                        repository.deleteAccountWithCascade(account)
                    }
                }
                else -> {}
            }
            _selectedItemsForDelete.value = emptySet()
            _mode.value = OperationMode.IDLE
        }
    }

    fun cancelDeleteWarning() {
        _showDeleteWarning.value = false
    }

    fun requestDeleteAccounts(names: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repository.getAccounts().first()
            val targets = accounts.filter { names.contains(it.name) }
            var totalCount = 0
            targets.forEach { account ->
                totalCount += repository.countTransactionsByAccount(account.id)
                totalCount += repository.countSubscriptionsByAccount(account.id)
            }
            if (totalCount > 0) {
                _deleteWarningMessage.value = context.getString(R.string.manage_delete_warning_account, totalCount)
                _deleteWarningNames.value = names
                _deleteWarningTarget.value = ManageTarget.ACCOUNT
                _showDeleteWarning.value = true
            } else {
                deleteAccountsDirect(names)
            }
        }
    }

    private suspend fun deleteAccountsDirect(names: Set<String>) {
        val accounts = repository.getAccounts().first()
        val targets = accounts.filter { names.contains(it.name) }
        if (targets.isNotEmpty()) {
            repository.deleteAccounts(targets)
        }
    }

    fun addAccount(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(name)
        }
    }

    fun editAccount(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repository.getAccounts().first()
            val match = accounts.firstOrNull { it.name == oldName }
            if (match != null) {
                repository.updateAccount(match.copy(name = newName))
            }
        }
    }
}

/** Resuelve el icono para un nombre de categoría conocido; usa Folder para las personalizadas. */
private fun iconResForCategory(name: String): Int = when (name) {
    "Food"          -> LucideR.drawable.lucide_ic_soup
    "Restaurants"   -> LucideR.drawable.lucide_ic_soup
    "Groceries"     -> LucideR.drawable.lucide_ic_shopping_cart
    "House"         -> LucideR.drawable.lucide_ic_house
    "Utilities"     -> LucideR.drawable.lucide_ic_zap
    "Transport"     -> LucideR.drawable.lucide_ic_train_front
    "Health"        -> LucideR.drawable.lucide_ic_heart_pulse
    "Entertainment" -> LucideR.drawable.lucide_ic_ticket
    "Subscriptions" -> LucideR.drawable.lucide_ic_repeat
    "Gaming"        -> LucideR.drawable.lucide_ic_gamepad_2
    "Shopping"      -> LucideR.drawable.lucide_ic_shopping_bag
    "Education"     -> LucideR.drawable.lucide_ic_book_open
    "Travel"        -> LucideR.drawable.lucide_ic_plane
    else            -> LucideR.drawable.lucide_ic_folder_code
}
