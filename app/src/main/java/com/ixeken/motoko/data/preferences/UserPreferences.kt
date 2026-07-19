package com.ixeken.motoko.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Gestor de almacenamiento persistente para las preferencias del usuario usando DataStore.
 */
class UserPreferences(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val ACTIVE_ACCOUNT_ID = longPreferencesKey("active_account_id")
        val DOCK_ALIGNMENT = stringPreferencesKey("dock_alignment")
        val APP_FONT = stringPreferencesKey("app_font")
        val MUTE_DELETE_WARNINGS = booleanPreferencesKey("mute_delete_warnings")
        val HIDE_BUDGET_CARD = booleanPreferencesKey("hide_budget_card")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val COLORED_ELEMENTS = booleanPreferencesKey("colored_elements")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLETS_CATALOG = stringPreferencesKey("wallets_catalog")
        val CATEGORIES_CATALOG = stringPreferencesKey("categories_catalog")
        val SWIPE_TO_DELETE = booleanPreferencesKey("swipe_to_delete")
        val TEXT_SIZE_INDEX = intPreferencesKey("text_size_index")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val CATEGORY_ICONS = stringPreferencesKey("category_icons")
        val CHECK_UPDATE_ON_START = booleanPreferencesKey("check_update_on_start")
        val DASHBOARD_VIEW_MODE = stringPreferencesKey("dashboard_view_mode")
        val DASHBOARD_PERIOD = stringPreferencesKey("dashboard_period")
    }

    // Separador interno usado para serializar listas en DataStore
    private val SEPARATOR = "|"

    /**
     * Flujo reactivo que emite el estado del modo privacidad.
     */
    val privacyMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.PRIVACY_MODE] ?: false
        }

    /**
     * Flujo reactivo que emite el ID de la cuenta activa seleccionada.
     */
    val activeAccountId: Flow<Long?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ACTIVE_ACCOUNT_ID]
        }

    /**
     * Flujo reactivo que emite la alineación ergonómica del menú ("center", "left", "right").
     */
    val dockAlignment: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.DOCK_ALIGNMENT] ?: "center"
        }

    /**
     * Flujo reactivo que emite la fuente seleccionada ("default", "space_mono", "space_grotesk", "dm_sans", "inter").
     */
    val appFont: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.APP_FONT] ?: "space_mono"
        }
 
    /**
     * Flujo reactivo que emite si las advertencias de borrado están silenciadas.
     */
    val muteDeleteWarnings: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.MUTE_DELETE_WARNINGS] ?: false
        }

    /**
     * Guarda el estado del modo privacidad.
     */
    suspend fun setPrivacyMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PRIVACY_MODE] = enabled
        }
    }

    /**
     * Guarda la cuenta activa seleccionada.
     */
    suspend fun setActiveAccountId(accountId: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_ACCOUNT_ID] = accountId
        }
    }

    suspend fun clearActiveAccountId() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_ACCOUNT_ID)
        }
    }

    /**
     * Guarda la alineación del menú flotante.
     */
    suspend fun setDockAlignment(alignment: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DOCK_ALIGNMENT] = alignment
        }
    }

    /**
     * Guarda la fuente seleccionada.
     */
    suspend fun setAppFont(fontName: String) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_FONT] = fontName
        }
    }

    /**
     * Guarda la preferencia de silenciado de advertencias de borrado.
     */
    suspend fun setMuteDeleteWarnings(mute: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.MUTE_DELETE_WARNINGS] = mute
        }
    }

    /**
     * Flujo reactivo que emite si la tarjeta de balance del home debe estar oculta.
     */
    val hideBudgetCard: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.HIDE_BUDGET_CARD] ?: false
        }

    /**
     * Guarda la preferencia de ocultar la tarjeta de balance.
     */
    suspend fun setHideBudgetCard(hide: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HIDE_BUDGET_CARD] = hide
        }
    }

    /**
     * Flujo reactivo que emite si el bloqueo biométrico está activado.
     */
    val appLock: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.APP_LOCK] ?: false
        }

    /**
     * Guarda la preferencia del bloqueo biométrico de la aplicación.
     */
    suspend fun setAppLock(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_LOCK] = enabled
        }
    }

    /**
     * Flujo reactivo que emite si los elementos deben colorearse o ser monocromáticos.
     */
    val coloredElements: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.COLORED_ELEMENTS] ?: true
        }

    /**
     * Guarda la preferencia de elementos coloreados.
     */
    suspend fun setColoredElements(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.COLORED_ELEMENTS] = enabled
        }
    }


    /**
     * Flujo reactivo que emite si se debe comprobar actualizaciones al iniciar la aplicación.
     */
    val checkUpdateOnStart: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.CHECK_UPDATE_ON_START] ?: false
        }

    /**
     * Guarda la preferencia de verificación de actualizaciones al iniciar.
     */
    suspend fun setCheckUpdateOnStart(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.CHECK_UPDATE_ON_START] = enabled
        }
    }

    /**
     * Flujo reactivo que emite el modo de tema seleccionado ("light", "dark", "system").
     */
    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.THEME_MODE] ?: "light"
        }

    /**
     * Guarda la preferencia del modo de tema.
     */
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode
        }
    }

    /** Flujo reactivo del catálogo de billeteras. Sin default: el usuario crea las suyas. */
    val walletsCatalog: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.WALLETS_CATALOG]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    /** Flujo reactivo del catálogo de categorías. Pre-instaladas por defecto. */
    val categoriesCatalog: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.CATEGORIES_CATALOG]
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: listOf(
                    "Food",
                    "Restaurants",
                    "Groceries",
                    "House",
                    "Utilities",
                    "Transport",
                    "Health",
                    "Entertainment",
                    "Subscriptions",
                    "Gaming",
                    "Shopping",
                    "Education",
                    "Travel",
                    "Others"
                )
        }

    /** Persiste el catálogo completo de billeteras. */
    suspend fun setWalletsCatalog(wallets: List<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.WALLETS_CATALOG] = wallets.joinToString(SEPARATOR)
        }
    }

    /** Persiste el catálogo completo de categorías (solo nombres). */
    suspend fun setCategoriesCatalog(categories: List<String>) {
        dataStore.edit { preferences ->
            preferences[Keys.CATEGORIES_CATALOG] = categories.joinToString(SEPARATOR)
        }
    }

    /** Flujo reactivo de iconos personalizados de categorías. */
    val categoryIcons: Flow<Map<String, String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val serialized = preferences[Keys.CATEGORY_ICONS] ?: ""
            if (serialized.isBlank()) {
                emptyMap()
            } else {
                serialized.split(SEPARATOR)
                    .filter { it.contains(":") }
                    .associate {
                        val parts = it.split(":", limit = 2)
                        parts[0] to parts[1]
                    }
            }
        }

    /** Asocia un icono personalizado a una categoría. */
    suspend fun setCategoryIcon(categoryName: String, iconName: String) {
        dataStore.edit { preferences ->
            val currentMap = (preferences[Keys.CATEGORY_ICONS] ?: "")
                .split(SEPARATOR)
                .filter { it.contains(":") }
                .associate {
                    val parts = it.split(":", limit = 2)
                    parts[0] to parts[1]
                }
                .toMutableMap()
            currentMap[categoryName] = iconName
            preferences[Keys.CATEGORY_ICONS] = currentMap.map { "${it.key}:${it.value}" }.joinToString(SEPARATOR)
        }
    }

    /** Flujo reactivo que indica si el swipe para eliminar en Historial está activo. */
    val swipeToDeleteEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.SWIPE_TO_DELETE] ?: true
        }

    /** Persiste la preferencia de swipe-to-delete. */
    suspend fun setSwipeToDeleteEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.SWIPE_TO_DELETE] = enabled
        }
    }

    /** Flujo reactivo del índice de tamaño de texto (0-4, default 2). */
    val textSizeIndex: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.TEXT_SIZE_INDEX] ?: 1
        }

    /** Persiste el índice de tamaño de texto. */
    suspend fun setTextSizeIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.TEXT_SIZE_INDEX] = index
        }
    }

    /**
     * Flujo reactivo que emite el símbolo de la moneda seleccionada.
     */
    val currencySymbol: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.CURRENCY_SYMBOL] ?: "$"
        }

    /**
     * Guarda la preferencia del símbolo de la moneda.
     */
    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { preferences ->
            preferences[Keys.CURRENCY_SYMBOL] = symbol
        }
    }

    /**
     * Flujo reactivo que emite si las animaciones están habilitadas.
     */
    val animationsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ANIMATIONS_ENABLED] ?: true
        }

    /**
     * Guarda la preferencia de animaciones habilitadas.
     */
    suspend fun setAnimationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ANIMATIONS_ENABLED] = enabled
        }
    }

    val isFirstRun: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.IS_FIRST_RUN] ?: true
        }

    suspend fun setFirstRunComplete() {
        dataStore.edit { preferences ->
            preferences[Keys.IS_FIRST_RUN] = false
        }
    }

    suspend fun setFirstRun(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_FIRST_RUN] = value
        }
    }

    val dashboardViewMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.DASHBOARD_VIEW_MODE] ?: "LIST"
        }

    suspend fun setDashboardViewMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DASHBOARD_VIEW_MODE] = mode
        }
    }

    val dashboardPeriod: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[Keys.DASHBOARD_PERIOD] ?: "DAY"
        }

    suspend fun setDashboardPeriod(period: String) {
        dataStore.edit { preferences ->
            preferences[Keys.DASHBOARD_PERIOD] = period
        }
    }
}
