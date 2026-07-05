package com.ixeken.motoko.presentation.newitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.domain.model.Subscription
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado del formulario de creación para nuevos movimientos o suscripciones.
 */
data class NewItemUiState(
    val selectedType: ItemType = ItemType.INCOME,
    val name: String = "",
    val amount: String = "",
    val wallet: String = "",
    val date: String = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
    val note: String = "",
    val icon: String = "",
    val account: String = "",
    val category: String = "",
    val receipt: String = "",
    val period: String = "",
    val wallets: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val accounts: List<String> = emptyList()
)

/**
 * Resuelve el icono predeterminado asociado a una categoría conocida del sistema.
 * Las categorías personalizadas usan "Folder" como icono genérico.
 */
fun defaultIconForCategory(category: String): String = when (category) {
    "Food" -> "Soup"
    "House" -> "House"
    "Entertainment" -> "Ticket"
    "Transport" -> "Train"
    "Gaming" -> "Gamepad"
    "Shopping" -> "ShoppingCart"
    "Others" -> "Folder"
    "Income" -> "TrendingUp"
    "Subscriptions" -> "Refresh"
    else -> "Folder"
}

/**
 * ViewModel encargado del flujo de creación y validaciones de un nuevo registro.
 */
@HiltViewModel
class NewItemViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewItemUiState())
    val uiState: StateFlow<NewItemUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewItemUiState()
        )

    private val _saveEvent = Channel<Unit>(Channel.BUFFERED)
    val saveEvent = _saveEvent.receiveAsFlow()

    init {
        // Suscripción reactiva a las cuentas en Room
        viewModelScope.launch {
            repository.getAccounts().collect { list ->
                _uiState.update { state ->
                    state.copy(
                        accounts = list.map { it.name },
                        account = state.account.ifBlank { list.firstOrNull()?.name ?: "Personal" }
                    )
                }
            }
        }
        // Suscripción reactiva al catálogo de billeteras en DataStore
        viewModelScope.launch {
            userPreferences.walletsCatalog.collect { wallets ->
                _uiState.update { it.copy(wallets = wallets) }
            }
        }
        // Suscripción reactiva al catálogo de categorías en DataStore
        viewModelScope.launch {
            userPreferences.categoriesCatalog.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTypeSelected(type: ItemType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onAmountChanged(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onWalletChanged(wallet: String) {
        _uiState.update { it.copy(wallet = wallet) }
    }

    fun onDateChanged(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onIconChanged(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun onAccountChanged(account: String) {
        _uiState.update { it.copy(account = account) }
    }

    fun onCategoryChanged(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onReceiptChanged(receipt: String) {
        _uiState.update { it.copy(receipt = receipt) }
    }

    fun onPeriodChanged(period: String) {
        _uiState.update { it.copy(period = period) }
    }

    /** Agrega una billetera nueva al catálogo compartido y la selecciona. */
    fun createNewWallet(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.walletsCatalog.first()
            if (!current.contains(name)) {
                userPreferences.setWalletsCatalog(current + name)
            }
            _uiState.update { it.copy(wallet = name) }
        }
    }

    /** Agrega una categoría nueva al catálogo compartido y la selecciona. */
    fun createNewCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.categoriesCatalog.first()
            if (!current.contains(name)) {
                userPreferences.setCategoriesCatalog(current + name)
            }
            _uiState.update { it.copy(category = name) }
        }
    }

    fun createNewAccount(name: String) {

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(name)
            _uiState.update {
                it.copy(account = name)
            }
        }
    }

    fun saveItem() {
        val state = _uiState.value
        if (state.name.isBlank()) return
        val parsedAmount = state.amount.toDoubleOrNull() ?: 0.0
        if (parsedAmount <= 0.0) return
        if (state.wallet.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repository.getAccounts().first()
            val accountId = accounts.firstOrNull { it.name == state.account }?.id
                ?: accounts.firstOrNull()?.id
                ?: 1L

            val walletType = when (state.wallet.lowercase()) {
                "cash", "efectivo" -> WalletType.CASH
                "savings", "ahorros" -> WalletType.SAVINGS
                else -> WalletType.BANK
            }

            val timestamp = try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.parse(state.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            if (state.selectedType == ItemType.SUBSCRIPTION) {
                val billingPeriod = when (state.period.lowercase()) {
                    "annual", "anual" -> BillingPeriod.ANNUAL
                    else -> BillingPeriod.MONTHLY
                }
                val subscription = Subscription(
                    id = 0L,
                    accountId = accountId,
                    name = state.name,
                    amount = parsedAmount,
                    wallet = walletType,
                    billingPeriod = billingPeriod,
                    startDate = timestamp,
                    category = "Subscriptions",
                    iconName = state.icon.ifBlank { defaultIconForCategory("Subscriptions") },
                    note = state.note.ifBlank { null }
                )
                repository.insertSubscription(subscription)
            } else {
                val isIncome = state.selectedType == ItemType.INCOME
                val resolvedCategory = if (isIncome) "Income" else state.category.ifBlank { "Others" }
                val transaction = Transaction(
                    id = 0L,
                    accountId = accountId,
                    title = state.name,
                    amount = parsedAmount,
                    isIncome = isIncome,
                    wallet = walletType,
                    category = resolvedCategory,
                    iconName = state.icon.ifBlank { defaultIconForCategory(resolvedCategory) },
                    timestamp = timestamp,
                    note = state.note.ifBlank { null },
                    receiptPath = state.receipt.ifBlank { null }
                )
                repository.insertTransaction(transaction)
            }

            _saveEvent.send(Unit)
            
            // Restablecer el estado del formulario con valores limpios y la fecha de hoy por defecto
            val today = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
            _uiState.update {
                it.copy(
                    name = "",
                    amount = "",
                    wallet = "",
                    date = today,
                    note = "",
                    icon = "",
                    category = "",
                    receipt = "",
                    period = ""
                )
            }
        }
    }

    private var _tempFilePath: String? = null

    // Genera un URI seguro para que la cámara del dispositivo guarde la captura temporal
    fun createTempImageUri(context: android.content.Context): android.net.Uri? {
        return try {
            val cacheDir = context.cacheDir
            val tempFile = java.io.File.createTempFile("receipt_", ".jpg", cacheDir)
            _tempFilePath = tempFile.absolutePath
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.ixeken.motoko.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            null
        }
    }

    // Actualiza la ruta del recibo en el estado del formulario con la foto de la cámara
    fun onReceiptCaptured() {
        val path = _tempFilePath ?: return
        _uiState.update { it.copy(receipt = path) }
    }

    // Copia asíncronamente el archivo seleccionado de la galería al directorio de caché local
    fun onReceiptSelectedFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val cacheDir = context.cacheDir
                val tempFile = java.io.File.createTempFile("receipt_gallery_", ".jpg", cacheDir)
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val path = tempFile.absolutePath
                _uiState.update { it.copy(receipt = path) }
            } catch (e: Exception) {
                // Manejo silencioso de errores de E/S
            }
        }
    }
}
