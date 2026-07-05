package com.ixeken.motoko

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import android.content.Context
import androidx.test.espresso.Espresso
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ixeken.motoko.data.local.AppDatabase
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.preferences.UserPreferences
import com.ixeken.motoko.data.repository.FinanceRepositoryImpl
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.repository.FinanceRepository
import com.ixeken.motoko.presentation.dashboard.DashboardViewModel
import com.ixeken.motoko.presentation.history.HistoryViewModel
import com.ixeken.motoko.presentation.main.MainScreen
import com.ixeken.motoko.presentation.main.MainViewModel
import com.ixeken.motoko.presentation.newitem.ItemType
import com.ixeken.motoko.presentation.newitem.NewItemViewModel
import com.ixeken.motoko.presentation.settings.ManageTarget
import com.ixeken.motoko.presentation.settings.ManageViewModel
import com.ixeken.motoko.presentation.settings.OperationMode
import com.ixeken.motoko.presentation.subscription.SubscriptionsViewModel
import com.ixeken.motoko.ui.theme.MotokoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Suite de pruebas de integración y UI automatizadas para la aplicación Motoko.
 * Cubre los flujos de lectura reactiva, creación de transacciones, selectores con sub-formularios,
 * edición en caliente, borrado con silenciado en DataStore y eliminación en lote de Ajustes.
 */
@RunWith(AndroidJUnit4::class)
class MotokoIntegrationTest {

    companion object {
        private var testDataStore: DataStore<Preferences>? = null

        fun getTestDataStore(context: Context): DataStore<Preferences> {
            return testDataStore ?: synchronized(this) {
                testDataStore ?: PreferenceDataStoreFactory.create(
                    produceFile = { File(context.filesDir, "test_preferences.preferences_pb") }
                ).also { testDataStore = it }
            }
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()


    private lateinit var db: AppDatabase
    private lateinit var repository: FinanceRepository
    private lateinit var userPreferences: UserPreferences

    private lateinit var mainViewModel: MainViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var subscriptionsViewModel: SubscriptionsViewModel
    private lateinit var newItemViewModel: NewItemViewModel
    private lateinit var manageViewModel: ManageViewModel

    private var personalAccountId: Long = 0L
    private var workAccountId: Long = 0L

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Base de datos temporal en memoria
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // DataStore temporal en archivo aislado singleton
        val testDataStore = getTestDataStore(context)
        runBlocking {
            testDataStore.edit { it.clear() }
        }
        userPreferences = UserPreferences(testDataStore)

        repository = FinanceRepositoryImpl(
            accountDao = db.accountDao(),
            transactionDao = db.transactionDao(),
            subscriptionDao = db.subscriptionDao()
        )

        // Insertar cuentas base en la base de datos temporal primero
        runBlocking {
            personalAccountId = repository.insertAccount("Personal")
            workAccountId = repository.insertAccount("Work")
        }

        // Instanciación manual de ViewModels para inyectar dependencias en memoria
        mainViewModel = MainViewModel(repository, userPreferences)
        dashboardViewModel = DashboardViewModel(repository, userPreferences)
        historyViewModel = HistoryViewModel(repository, userPreferences)
        subscriptionsViewModel = SubscriptionsViewModel(repository, userPreferences)
        newItemViewModel = NewItemViewModel(repository)
        manageViewModel = ManageViewModel(repository)
    }

    @Test
    fun verifyDashboardAndHistory_reactsToActiveAccount() {
        // Insertar datos de prueba para verificar reactividad de cuentas
        runBlocking {
            // Personal Account Transactions
            repository.insertTransaction(
                Transaction(
                    id = 1L,
                    accountId = personalAccountId,
                    title = "Xbox Controller",
                    amount = 60.0,
                    isIncome = false,
                    wallet = WalletType.CASH,
                    category = "Entertainment",
                    iconName = "Ticket",
                    timestamp = System.currentTimeMillis(),
                    note = null,
                    receiptPath = null
                )
            )
            repository.insertTransaction(
                Transaction(
                    id = 2L,
                    accountId = personalAccountId,
                    title = "Netflix Premium",
                    amount = 15.0,
                    isIncome = false,
                    wallet = WalletType.BANK,
                    category = "Food",
                    iconName = "Soup",
                    timestamp = System.currentTimeMillis(),
                    note = null,
                    receiptPath = null
                )
            )
            // Work Account Transactions
            repository.insertTransaction(
                Transaction(
                    id = 3L,
                    accountId = workAccountId,
                    title = "Freelance Design",
                    amount = 500.0,
                    isIncome = true,
                    wallet = WalletType.BANK,
                    category = "Income",
                    iconName = "Dollar",
                    timestamp = System.currentTimeMillis(),
                    note = null,
                    receiptPath = null
                )
            )
        }

        // Establecer contenido en la ComposeTestRule
        composeTestRule.setContent {
            MotokoTheme(appFont = "space_mono") {
                MainScreen(
                    viewModel = mainViewModel,
                    dashboardViewModel = dashboardViewModel,
                    historyViewModel = historyViewModel,
                    subscriptionsViewModel = subscriptionsViewModel,
                    newItemViewModel = newItemViewModel,
                    manageViewModel = manageViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 1. Verificar Dashboard muestra balance de cuenta 'Personal' por defecto (60 + 15 de gasto = -75.0)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("$ -75.0").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("$ -75.0").assertIsDisplayed()

        // 2. Cambiar selector de cuenta superior a 'Work'
        composeTestRule.onNodeWithText("Personal").performClick()
        composeTestRule.onNodeWithText("Work").performClick()

        // 3. Verificar reactividad del Dashboard actualizando balance a la cuenta Work ($ 500.0)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("$ 500.0").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("$ 500.0").assertIsDisplayed()

        // 4. Cambiar al Historial y comprobar que reacciona a la cuenta activa
        composeTestRule.onAllNodesWithContentDescription("Navigation tab")[1].performClick()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Freelance Design").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Freelance Design").assertIsDisplayed()
        composeTestRule.onNodeWithText("Xbox Controller").assertDoesNotExist()

        // Regresar a cuenta Personal para probar filtro de búsqueda
        composeTestRule.onNodeWithText("Work").performClick()
        composeTestRule.onNodeWithText("Personal").performClick()

        // 5. Probar barra de búsqueda en historial
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Xbox")
        Espresso.closeSoftKeyboard()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Xbox Controller").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Xbox Controller").assertIsDisplayed()
        composeTestRule.onNodeWithText("Netflix Premium").assertDoesNotExist()
    }

    @Test
    fun verifyNewItemForm_savesDataCorrectly() {
        composeTestRule.setContent {
            MotokoTheme(appFont = "space_mono") {
                MainScreen(
                    viewModel = mainViewModel,
                    dashboardViewModel = dashboardViewModel,
                    historyViewModel = historyViewModel,
                    subscriptionsViewModel = subscriptionsViewModel,
                    newItemViewModel = newItemViewModel,
                    manageViewModel = manageViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 1. Abrir formulario de creación
        composeTestRule.onNodeWithContentDescription("Add transaction").performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onAllNodes(hasSetTextAction())[0].assertIsDisplayed()
                true
            } catch (e: Exception) {
                false
            }
        }

        // 2. Llenar campos de texto (Name, Amount)
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Netflix")
        Espresso.closeSoftKeyboard()
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("15.99")
        Espresso.closeSoftKeyboard()

        // 3. Abrir SelectorBottomSheet haciendo clic en Wallet
        composeTestRule.onAllNodes(hasAnySibling(hasText("Wallet")) and hasClickAction())[0].performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithText("New wallet").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText("New wallet").assertIsDisplayed()

        // 4. Click en 'New wallet' y rellenar sub-formulario
        composeTestRule.onNodeWithText("New wallet").performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onAllNodes(hasSetTextAction()).onLast().assertIsDisplayed()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onAllNodes(hasSetTextAction()).onLast().performTextInput("My Custom Wallet")
        Espresso.closeSoftKeyboard()
        
        // Click en Guardar del sub-formulario
        composeTestRule.onNode(hasText("Save") and hasAnySibling(hasText("Cancel"))).performClick()

        // 5. Confirmar selección de la nueva billetera en el modal general
        // Hay dos botones de "Save" en pantalla (el de la pantalla NewItemScreen y el del SelectorBottomSheet).
        // Presionamos el del SelectorBottomSheet (segundo nodo en el árbol jerárquico).
        composeTestRule.onAllNodesWithText("Save")[1].performClick()

        // 6. Hacer clic en el botón principal Save del formulario para persistir en Room
        composeTestRule.onNodeWithText("Save").performClick()

        // 7. Verificar que el formulario se cerró y regresó al Dashboard (la pantalla Dashboard muestra el título Budget)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Budget").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Budget").assertIsDisplayed()
    }

    @Test
    fun verifyItemDetail_andManageScreen_batchDelete() {
        // Insertar datos iniciales para edición y borrado
        runBlocking {
            repository.insertTransaction(
                Transaction(
                    id = 42L,
                    accountId = personalAccountId,
                    title = "Test Transaction",
                    amount = 50.0,
                    isIncome = false,
                    wallet = WalletType.CASH,
                    category = "Food",
                    iconName = "Soup",
                    timestamp = System.currentTimeMillis(),
                    note = "Original Note",
                    receiptPath = null
                )
            )
        }

        composeTestRule.setContent {
            MotokoTheme(appFont = "space_mono") {
                MainScreen(
                    viewModel = mainViewModel,
                    dashboardViewModel = dashboardViewModel,
                    historyViewModel = historyViewModel,
                    subscriptionsViewModel = subscriptionsViewModel,
                    newItemViewModel = newItemViewModel,
                    manageViewModel = manageViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Navegar a pestaña History
        composeTestRule.onAllNodesWithContentDescription("Navigation tab")[1].performClick()

        // 1. Abrir detalle de la transacción
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Test Transaction").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Test Transaction").performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithText("Original Note").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText("Original Note").assertExists()

        // 2. Modo Edición
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onAllNodes(hasSetTextAction())[1].assertExists()
                true
            } catch (e: Exception) {
                false
            }
        }
        // Name field in edit sheet (index 1, index 0 is search bar under the sheet)
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextReplacement("Edited Transaction")
        Espresso.closeSoftKeyboard()

        // Cancelar edición y comprobar que no cambia
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onAllNodesWithText("Test Transaction").assertCountEquals(2)

        // 3. Confirmar Eliminación con silenciado en DataStore
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithText("This item will be permanently deleted. Are you sure?").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        composeTestRule.onNodeWithText("This item will be permanently deleted. Are you sure?").assertExists()

        // Marcar checkbox "Don't show this again" y confirmar con "Yes"
        composeTestRule.onNodeWithText("Don't show this again").performClick()
        composeTestRule.onNodeWithText("Yes").performClick()

        // Comprobar que desaparece de Room y la preferencia se guarda
        runBlocking {
            var txDeleted = false
            for (i in 1..30) {
                if (repository.getTransactionById(42L) == null) {
                    txDeleted = true
                    break
                }
                kotlinx.coroutines.delay(100)
            }
            assertTrue("Transaction was not deleted from DB", txDeleted)

            var warningMuted = false
            for (i in 1..30) {
                if (userPreferences.muteDeleteWarnings.first()) {
                    warningMuted = true
                    break
                }
                kotlinx.coroutines.delay(100)
            }
            assertTrue("Delete warnings were not muted in DataStore", warningMuted)
        }

        // 4. Gestión en lote de Ajustes
        // Navegar a Ajustes
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Manage categories & accounts").performClick()

        // Activar modo eliminación
        composeTestRule.onNodeWithText("Delete").performClick()

        // Seleccionar elementos ("Food" y "House")
        composeTestRule.onNodeWithText("Food").performClick()
        composeTestRule.onNodeWithText("House").performClick()

        // Validar aserción visual de selección en el ViewModel
        assertTrue(manageViewModel.uiState.value.selectedItemsForDelete.contains("Food"))
        assertTrue(manageViewModel.uiState.value.selectedItemsForDelete.contains("House"))

        // Guardar cambios en lote
        composeTestRule.onNodeWithText("Save").performClick()

        // Validar que el catálogo de categorías en el ViewModel se redujo y no contiene las eliminadas
        val finalCategories = manageViewModel.uiState.value.categoriesList.map { it.first }
        assertFalse(finalCategories.contains("Food"))
        assertFalse(finalCategories.contains("House"))
    }
}
