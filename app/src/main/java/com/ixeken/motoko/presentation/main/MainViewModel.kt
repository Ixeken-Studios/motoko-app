package com.ixeken.motoko.presentation.main

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.domain.model.Account
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.repository.FinanceRepository
import com.ixeken.motoko.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * ViewModel encargado de orquestar el estado de la pantalla principal, cabecera superior y barra de pestañas.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    private val _currentTab = MutableStateFlow(MotokoTab.DASHBOARD)
    private val _selectedAccount = MutableStateFlow<Account?>(null)

    private val _backupMessage = Channel<String>(Channel.BUFFERED)
    val backupMessage = _backupMessage.receiveAsFlow()

    private val _backupError = MutableStateFlow<String?>(null)
    val backupError: StateFlow<String?> = _backupError

    private val _updateMessage = Channel<String>(Channel.BUFFERED)
    val updateMessage = _updateMessage.receiveAsFlow()

    var updateAvailableVersion by mutableStateOf<String?>(null)
        private set
    var updateHtmlUrl by mutableStateOf<String?>(null)
        private set
    private var hasCheckedUpdatesThisSession = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.getAccounts().first().isEmpty()) {
                repository.insertAccount("Personal")
            }
        }
    }

    fun clearBackupError() {
        _backupError.value = null
    }

    val uiState: StateFlow<MainScreenState> = combine(
        _currentTab,
        preferences.privacyMode,
        preferences.dockAlignment,
        repository.getAccounts(),
        _selectedAccount,
        preferences.appFont,
        preferences.muteDeleteWarnings,
        preferences.hideBudgetCard,
        preferences.appLock,
        preferences.coloredElements,
        preferences.themeMode,
        preferences.swipeToDeleteEnabled,
        preferences.textSizeIndex,
        preferences.currencySymbol,
        preferences.animationsEnabled,
        preferences.walletsCatalog,
        preferences.categoriesCatalog,
        preferences.checkUpdateOnStart
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val tab = flows[0] as MotokoTab
        val privacyEnabled = flows[1] as Boolean
        val alignmentStr = flows[2] as String
        @Suppress("UNCHECKED_CAST")
        val dbAccounts = flows[3] as List<com.ixeken.motoko.domain.model.Account>
        val activeAccount = flows[4] as com.ixeken.motoko.domain.model.Account?
        val fontStr = flows[5] as String
        val muteWarnings = flows[6] as Boolean
        val hideBudgetCardVal = flows[7] as Boolean
        val appLockVal = flows[8] as Boolean
        val coloredElementsVal = flows[9] as Boolean
        val themeModeVal = flows[10] as String
        val swipeToDeleteVal = flows[11] as Boolean
        val textSizeIndexVal = flows[12] as Int
        val currencySymbolVal = flows[13] as String
        val animationsEnabledVal = flows[14] as Boolean
        @Suppress("UNCHECKED_CAST")
        val walletsVal = flows[15] as List<String>
        @Suppress("UNCHECKED_CAST")
        val categoriesVal = flows[16] as List<String>
        val checkUpdateOnStartVal = flows[17] as Boolean

        // Solo leer las cuentas
        val accountsList = dbAccounts

        val selected = activeAccount ?: accountsList.firstOrNull()
        
        val composeAlignment = when (alignmentStr) {
            "left" -> Alignment.BottomStart
            "right" -> Alignment.BottomEnd
            else -> Alignment.BottomCenter
        }

        MainScreenState(
            currentTab = tab,
            isPrivacyEnabled = privacyEnabled,
            dockAlignment = composeAlignment,
            accounts = accountsList.map { it.name },
            selectedAccount = selected?.name ?: "Personal",
            appFont = fontStr,
            muteDeleteWarnings = muteWarnings,
            hideBudgetCard = hideBudgetCardVal,
            appLockEnabled = appLockVal,
            coloredElementsEnabled = coloredElementsVal,
            themeMode = themeModeVal,
            checkUpdateOnStart = checkUpdateOnStartVal,
            swipeToDeleteEnabled = swipeToDeleteVal,
            textSizeIndex = textSizeIndexVal,
            currencySymbol = currencySymbolVal,
            animationsEnabled = animationsEnabledVal,
            wallets = walletsVal,
            categories = categoriesVal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenState()
    )

    fun selectTab(tab: MotokoTab) {
        _currentTab.value = tab
    }

    fun togglePrivacy() {
        viewModelScope.launch {
            val current = uiState.value.isPrivacyEnabled
            preferences.setPrivacyMode(!current)
        }
    }

    fun selectAccount(accountName: String) {
        viewModelScope.launch {
            val accounts = repository.getAccounts().first()
            val match = accounts.firstOrNull { it.name == accountName }
            if (match != null) {
                _selectedAccount.value = match
                preferences.setActiveAccountId(match.id)
            }
        }
    }

    /**
     * Alterna la alineación del menú flotante de forma rotativa para demostración ergonómica.
     * Pasa de Center -> Left -> Right -> Center.
     */
    fun rotateAlignment() {
        viewModelScope.launch {
            val current = preferences.dockAlignment.first()
            val next = when (current) {
                "center" -> "left"
                "left" -> "right"
                else -> "center"
            }
            preferences.setDockAlignment(next)
        }
    }

    /**
     * Establece la alineación del menú flotante de manera directa.
     */
    fun setDockAlignment(alignment: String) {
        viewModelScope.launch {
            preferences.setDockAlignment(alignment)
        }
    }

    /**
     * Establece la fuente de la aplicación de manera directa.
     */
    fun setAppFont(fontName: String) {
        viewModelScope.launch {
            preferences.setAppFont(fontName)
        }
    }

    suspend fun getTransactionById(id: Long): com.ixeken.motoko.domain.model.Transaction? {
        return repository.getTransactionById(id)
    }

    suspend fun getSubscriptionById(id: Long): com.ixeken.motoko.domain.model.Subscription? {
        return repository.getSubscriptionById(id)
    }

    fun setMuteDeleteWarnings(mute: Boolean) {
        viewModelScope.launch {
            preferences.setMuteDeleteWarnings(mute)
        }
    }

    /**
     * Guarda la preferencia de ocultar la tarjeta de balance en el Home.
     */
    fun setHideBudgetCard(hide: Boolean) {
        viewModelScope.launch {
            preferences.setHideBudgetCard(hide)
        }
    }

    /**
     * Guarda la preferencia de bloqueo biométrico de la aplicación.
     */
    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAppLock(enabled)
        }
    }

    /**
     * Guarda la preferencia de elementos coloreados de la aplicación.
     */
    fun setColoredElements(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setColoredElements(enabled)
        }
    }


    /**
     * Guarda la preferencia del modo de tema (light, dark, system).
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setCheckUpdateOnStart(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setCheckUpdateOnStart(enabled)
        }
    }

    fun clearUpdateState() {
        updateAvailableVersion = null
        updateHtmlUrl = null
    }

    fun checkForUpdates(context: Context, manual: Boolean) {
        if (!manual && hasCheckedUpdatesThisSession) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!manual) {
                    val isAutoEnabled = preferences.checkUpdateOnStart.first()
                    if (!isAutoEnabled) return@launch
                }
                hasCheckedUpdatesThisSession = true
                val url = java.net.URL("https://api.github.com/repos/Ixeken-Studios/motoko-app/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "Motoko-App")

                if (connection.responseCode == 200) {
                    val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(jsonStr)
                    val rawTagName = json.getString("tag_name")
                    val htmlUrl = json.getString("html_url")

                    val latestVersion = rawTagName.trim().lowercase().removePrefix("v")
                    val currentVersion = context.getString(R.string.settings_motoko_version).trim().lowercase().removePrefix("v")

                    val isNewer = isVersionNewer(currentVersion, latestVersion)

                    if (isNewer) {
                        withContext(Dispatchers.Main) {
                            updateAvailableVersion = rawTagName
                            updateHtmlUrl = htmlUrl
                        }
                    } else if (manual) {
                        _updateMessage.send(context.getString(R.string.update_already_latest))
                    }
                } else {
                    if (manual) {
                        _updateMessage.send(context.getString(R.string.update_check_failed))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (manual) {
                    _updateMessage.send(context.getString(R.string.update_check_network_error))
                }
            }
        }
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val currVal = currentParts.getOrElse(i) { 0 }
            val latVal = latestParts.getOrElse(i) { 0 }
            if (latVal > currVal) return true
            if (currVal > latVal) return false
        }
        return false
    }

    /**
     * Guarda la preferencia de animaciones habilitadas.
     */
    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAnimationsEnabled(enabled)
        }
    }

    /**
     * Vacía la base de datos eliminando todos los registros y re-creando la cuenta por defecto.
     */
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            repository.insertAccount("Personal")
            preferences.setWalletsCatalog(emptyList())
            preferences.clearActiveAccountId()
        }
    }

    fun insertAccount(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAccount(name)
        }
    }

    suspend fun getAccountNameById(id: Long): String? {
        return repository.getAccounts().first().firstOrNull { it.id == id }?.name
    }

    fun updateItem(
        id: Long,
        type: com.ixeken.motoko.presentation.newitem.ItemType,
        name: String,
        amountStr: String,
        walletName: String,
        categoryName: String,
        dateStr: String,
        noteStr: String,
        accountName: String,
        iconName: String,
        receiptPath: String? = null,
        billingPeriodStr: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = repository.getAccounts().first()
            val accountId = accounts.firstOrNull { it.name == accountName }?.id
                ?: accounts.firstOrNull()?.id
                ?: 1L

            val walletType = when (walletName.lowercase()) {
                "cash", "efectivo" -> com.ixeken.motoko.data.local.WalletType.CASH
                "savings", "ahorros" -> com.ixeken.motoko.data.local.WalletType.SAVINGS
                else -> com.ixeken.motoko.data.local.WalletType.BANK
            }

            val cleanAmount = amountStr
                .replace("+", "")
                .replace("-", "")
                .replace(",", ".")
                .filter { it.isDigit() || it == '.' }
                .trim()
            val parsedAmount = cleanAmount.toDoubleOrNull() ?: 0.0

            val timestamp = try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }

            if (type == com.ixeken.motoko.presentation.newitem.ItemType.SUBSCRIPTION) {
                val billingPeriod = when ((billingPeriodStr ?: noteStr).lowercase()) {
                    "annual", "anual" -> com.ixeken.motoko.data.local.BillingPeriod.ANNUAL
                    else -> com.ixeken.motoko.data.local.BillingPeriod.MONTHLY
                }
                val subscription = com.ixeken.motoko.domain.model.Subscription(
                    id = id,
                    accountId = accountId,
                    name = name,
                    amount = parsedAmount,
                    wallet = walletType,
                    billingPeriod = billingPeriod,
                    startDate = timestamp,
                    iconName = iconName.ifBlank { "Dollar" },
                    category = "Subscriptions",
                    note = if (billingPeriodStr != null) noteStr.ifBlank { null } else null
                )
                repository.insertSubscription(subscription)
            } else {
                val transaction = com.ixeken.motoko.domain.model.Transaction(
                    id = id,
                    accountId = accountId,
                    title = name,
                    amount = parsedAmount,
                    isIncome = type == com.ixeken.motoko.presentation.newitem.ItemType.INCOME,
                    wallet = walletType,
                    category = categoryName.ifBlank { "Others" },
                    iconName = iconName.ifBlank { "Dollar" },
                    timestamp = timestamp,
                    note = noteStr.ifBlank { null },
                    receiptPath = receiptPath?.ifBlank { null }
                )
                repository.insertTransaction(transaction)
            }
        }
    }

    fun deleteItem(id: Long, type: com.ixeken.motoko.presentation.newitem.ItemType) {
        viewModelScope.launch(Dispatchers.IO) {
            if (type == com.ixeken.motoko.presentation.newitem.ItemType.SUBSCRIPTION) {
                val sub = repository.getSubscriptionById(id)
                if (sub != null) {
                    repository.deleteSubscription(sub)
                }
            } else {
                val tx = repository.getTransactionById(id)
                if (tx != null) {
                    repository.deleteTransaction(tx)
                }
            }
        }
    }

    // --- Undo de borrado de transacciones ---

    /** Última transacción eliminada, guardada en memoria para permitir deshacer. */
    private val _lastDeletedTransaction = MutableStateFlow<Transaction?>(null)

    /**
     * Elimina una transacción y la guarda temporalmente para poder deshacerla.
     * Usar en lugar de deleteItem cuando el borrado proviene del historial o del detalle.
     */
    fun deleteTransactionWithUndo(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val tx = repository.getTransactionById(id) ?: return@launch
            repository.deleteTransaction(tx)
            _lastDeletedTransaction.value = tx
        }
    }

    /**
     * Re-inserta la última transacción eliminada usando el mismo ID (Room REPLACE la restaura).
     * Limpia el estado de undo después de restaurar.
     */
    fun undoLastDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            val tx = _lastDeletedTransaction.value ?: return@launch
            repository.insertTransaction(tx)
            _lastDeletedTransaction.value = null
        }
    }

    /** Descarta la transacción pendiente de undo (llamar cuando el Snackbar expira). */
    fun clearUndo() {
        _lastDeletedTransaction.value = null
    }

    /** Guarda la preferencia de swipe-to-delete. */
    fun setSwipeToDeleteEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setSwipeToDeleteEnabled(enabled)
        }
    }

    /** Guarda el índice de tamaño de texto. */
    fun setTextSizeIndex(index: Int) {
        viewModelScope.launch {
            preferences.setTextSizeIndex(index)
        }
    }

    /**
     * Guarda la preferencia del símbolo de la moneda.
     */
    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            preferences.setCurrencySymbol(symbol)
        }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val accounts = repository.getAllAccountsRaw()
                val transactions = repository.getAllTransactionsRaw()
                val subscriptions = repository.getAllSubscriptionsRaw()

                val wallets = preferences.walletsCatalog.first()
                val categories = preferences.categoriesCatalog.first()
                val appFont = preferences.appFont.first()
                val dockAlignment = preferences.dockAlignment.first()
                val hideBudgetCard = preferences.hideBudgetCard.first()
                val appLock = preferences.appLock.first()
                val coloredElements = preferences.coloredElements.first()
                val themeMode = preferences.themeMode.first()
                val swipeToDelete = preferences.swipeToDeleteEnabled.first()
                val textSizeIndex = preferences.textSizeIndex.first()
                val currencySymbol = preferences.currencySymbol.first()
                val animationsEnabled = preferences.animationsEnabled.first()

                val settingsJson = JSONObject().apply {
                    put("walletsCatalog", JSONArray(wallets))
                    put("categoriesCatalog", JSONArray(categories))
                    put("appFont", appFont)
                    put("dockAlignment", dockAlignment)
                    put("hideBudgetCard", hideBudgetCard)
                    put("appLock", appLock)
                    put("coloredElements", coloredElements)
                    put("dynamicColor", false)
                    put("themeMode", themeMode)
                    put("swipeToDeleteEnabled", swipeToDelete)
                    put("textSizeIndex", textSizeIndex)
                    put("currencySymbol", currencySymbol)
                    put("animationsEnabled", animationsEnabled)
                }

                val accountsArray = JSONArray().apply {
                    accounts.forEach { acc ->
                        val obj = JSONObject().apply {
                            put("id", acc.id)
                            put("name", acc.name)
                        }
                        put(obj)
                    }
                }

                val transactionsArray = JSONArray().apply {
                    transactions.forEach { tx ->
                        val obj = JSONObject().apply {
                            put("id", tx.id)
                            put("accountId", tx.accountId)
                            put("title", tx.title)
                            put("amount", tx.amount)
                            put("isIncome", tx.isIncome)
                            put("wallet", tx.wallet.name)
                            put("category", tx.category)
                            put("iconName", tx.iconName)
                            put("timestamp", tx.timestamp)
                            put("note", tx.note ?: JSONObject.NULL)
                            put("receiptPath", tx.receiptPath ?: JSONObject.NULL)
                        }
                        put(obj)
                    }
                }

                val subscriptionsArray = JSONArray().apply {
                    subscriptions.forEach { sub ->
                        val obj = JSONObject().apply {
                            put("id", sub.id)
                            put("accountId", sub.accountId)
                            put("name", sub.name)
                            put("amount", sub.amount)
                            put("wallet", sub.wallet.name)
                            put("billingPeriod", sub.billingPeriod.name)
                            put("startDate", sub.startDate)
                            put("iconName", sub.iconName)
                            put("category", sub.category)
                            put("note", sub.note ?: JSONObject.NULL)
                        }
                        put(obj)
                    }
                }

                val backupJson = JSONObject().apply {
                    put("accounts", accountsArray)
                    put("transactions", transactionsArray)
                    put("subscriptions", subscriptionsArray)
                    put("settings", settingsJson)
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(backupJson.toString(2).toByteArray(Charsets.UTF_8))
                }

                _backupMessage.send(context.getString(com.ixeken.motoko.R.string.backup_export_success))
            } catch (e: Exception) {
                e.printStackTrace()
                _backupError.value = context.getString(com.ixeken.motoko.R.string.backup_export_error)
            }
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("Cannot open input stream")

                val backupJson = JSONObject(content)
                if (!backupJson.has("settings") || !backupJson.has("accounts") || 
                    !backupJson.has("transactions") || !backupJson.has("subscriptions")) {
                    throw Exception("Missing required backup tables")
                }

                val settingsJson = backupJson.getJSONObject("settings")

                val walletsArray = settingsJson.optJSONArray("walletsCatalog") ?: JSONArray()
                val parsedWallets = mutableListOf<String>()
                for (i in 0 until walletsArray.length()) {
                    parsedWallets.add(walletsArray.getString(i))
                }

                val categoriesArray = settingsJson.optJSONArray("categoriesCatalog") ?: JSONArray()
                val parsedCategories = mutableListOf<String>()
                for (i in 0 until categoriesArray.length()) {
                    parsedCategories.add(categoriesArray.getString(i))
                }

                val hideBudgetCard = settingsJson.optBoolean("hideBudgetCard", false)
                val appLock = settingsJson.optBoolean("appLock", false)
                val swipeToDeleteEnabled = settingsJson.optBoolean("swipeToDeleteEnabled", true)
                val currencySymbol = settingsJson.optString("currencySymbol", "$")

                val accountsArray = backupJson.getJSONArray("accounts")
                val parsedAccounts = mutableListOf<com.ixeken.motoko.data.local.entity.AccountEntity>()
                for (i in 0 until accountsArray.length()) {
                    val obj = accountsArray.getJSONObject(i)
                    parsedAccounts.add(
                        com.ixeken.motoko.data.local.entity.AccountEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name")
                        )
                    )
                }

                val transactionsArray = backupJson.getJSONArray("transactions")
                val parsedTransactions = mutableListOf<com.ixeken.motoko.data.local.entity.TransactionEntity>()
                for (i in 0 until transactionsArray.length()) {
                    val obj = transactionsArray.getJSONObject(i)
                    val walletStr = obj.getString("wallet")
                    val wallet = com.ixeken.motoko.data.local.WalletType.valueOf(walletStr)

                    parsedTransactions.add(
                        com.ixeken.motoko.data.local.entity.TransactionEntity(
                            id = obj.getLong("id"),
                            accountId = obj.getLong("accountId"),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            isIncome = obj.getBoolean("isIncome"),
                            wallet = wallet,
                            category = obj.getString("category"),
                            iconName = obj.getString("iconName"),
                            timestamp = obj.getLong("timestamp"),
                            note = if (obj.isNull("note")) null else obj.getString("note"),
                            receiptPath = if (obj.isNull("receiptPath")) null else obj.getString("receiptPath")
                        )
                    )
                }

                val subscriptionsArray = backupJson.getJSONArray("subscriptions")
                val parsedSubscriptions = mutableListOf<com.ixeken.motoko.data.local.entity.SubscriptionEntity>()
                for (i in 0 until subscriptionsArray.length()) {
                    val obj = subscriptionsArray.getJSONObject(i)
                    val walletStr = obj.getString("wallet")
                    val wallet = com.ixeken.motoko.data.local.WalletType.valueOf(walletStr)
                    val billingPeriodStr = obj.getString("billingPeriod")
                    val billingPeriod = com.ixeken.motoko.data.local.BillingPeriod.valueOf(billingPeriodStr)

                    parsedSubscriptions.add(
                        com.ixeken.motoko.data.local.entity.SubscriptionEntity(
                            id = obj.getLong("id"),
                            accountId = obj.getLong("accountId"),
                            name = obj.getString("name"),
                            amount = obj.getDouble("amount"),
                            wallet = wallet,
                            billingPeriod = billingPeriod,
                            startDate = obj.getLong("startDate"),
                            iconName = obj.getString("iconName"),
                            category = obj.getString("category"),
                            note = if (obj.isNull("note")) null else obj.getString("note")
                        )
                    )
                }

                repository.restoreBackup(parsedAccounts, parsedTransactions, parsedSubscriptions)

                preferences.setWalletsCatalog(parsedWallets)
                preferences.setCategoriesCatalog(parsedCategories)
                preferences.setHideBudgetCard(hideBudgetCard)
                preferences.setAppLock(appLock)
                preferences.setSwipeToDeleteEnabled(swipeToDeleteEnabled)
                preferences.setCurrencySymbol(currencySymbol)

                if (parsedAccounts.isNotEmpty()) {
                    val firstAcc = parsedAccounts.first()
                    preferences.setActiveAccountId(firstAcc.id)
                    _selectedAccount.value = Account(id = firstAcc.id, name = firstAcc.name)
                }

                _backupMessage.send(context.getString(com.ixeken.motoko.R.string.backup_import_success))
            } catch (e: Exception) {
                e.printStackTrace()
                _backupError.value = context.getString(com.ixeken.motoko.R.string.backup_invalid_file_error)
            }
        }
    }
}
