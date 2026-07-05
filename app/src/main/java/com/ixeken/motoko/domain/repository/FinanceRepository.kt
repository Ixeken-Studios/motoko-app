package com.ixeken.motoko.domain.repository

import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.domain.model.Account
import com.ixeken.motoko.domain.model.Subscription
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.data.local.entity.AccountEntity
import com.ixeken.motoko.data.local.entity.SubscriptionEntity
import com.ixeken.motoko.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de acceso unificado para el manejo de cuentas, transacciones y suscripciones.
 */
interface FinanceRepository {

    // --- Gestión de Cuentas ---

    /**
     * Obtiene el flujo de todas las cuentas registradas.
     */
    fun getAccounts(): Flow<List<Account>>

    /**
     * Registra una nueva cuenta y retorna su ID de base de datos.
     */
    suspend fun insertAccount(name: String): Long

    /**
     * Elimina una cuenta registrada.
     */
    suspend fun deleteAccount(account: Account)

    // --- Gestión de Transacciones ---

    /**
     * Obtiene las últimas 5 transacciones de una cuenta activa.
     */
    fun getLatestTransactions(accountId: Long): Flow<List<Transaction>>

    /**
     * Obtiene el historial completo de transacciones para una cuenta activa.
     */
    fun getAllTransactions(accountId: Long): Flow<List<Transaction>>

    /**
     * Obtiene el balance total neto de la cuenta. Retorna 0.0 si no hay transacciones.
     */
    fun getTotalBalance(accountId: Long): Flow<Double>

    /**
     * Obtiene el balance neto filtrado por tipo de billetera. Retorna 0.0 si no hay transacciones.
     */
    fun getBalanceByWallet(accountId: Long, wallet: WalletType): Flow<Double>

    /**
     * Registra o actualiza una transacción financiera.
     */
    suspend fun insertTransaction(transaction: Transaction)

    /**
     * Elimina una transacción.
     */
    suspend fun deleteTransaction(transaction: Transaction)

    // --- Gestión de Suscripciones ---

    /**
     * Obtiene todas las suscripciones activas vinculadas a una cuenta.
     */
    fun getSubscriptions(accountId: Long): Flow<List<Subscription>>

    /**
     * Registra o actualiza una suscripción.
     */
    suspend fun insertSubscription(subscription: Subscription)

    /**
     * Elimina una suscripción.
     */
    suspend fun deleteSubscription(subscription: Subscription)

    /**
     * Obtiene una transacción por su ID único.
     */
    suspend fun getTransactionById(id: Long): Transaction?

    /**
     * Obtiene una suscripción por su ID único.
     */
    suspend fun getSubscriptionById(id: Long): Subscription?

    /**
     * Actualiza una cuenta preexistente.
     */
    suspend fun updateAccount(account: Account)

    /**
     * Elimina una lista de cuentas en lote.
     */
    suspend fun deleteAccounts(accounts: List<Account>)

    /**
     * Reasigna la categoría de todas las transacciones con el nombre antiguo al nuevo.
     */
    suspend fun reassignTransactionsCategory(oldCategory: String, newCategory: String)

    /**
     * Reasigna la categoría de todas las suscripciones con el nombre antiguo al nuevo.
     */
    suspend fun reassignSubscriptionsCategory(oldCategory: String, newCategory: String)

    /** Cuenta las transacciones asociadas a una wallet. */
    suspend fun countTransactionsByWallet(wallet: WalletType): Int

    /** Cuenta las suscripciones asociadas a una wallet. */
    suspend fun countSubscriptionsByWallet(wallet: WalletType): Int

    /** Cuenta las transacciones asociadas a una cuenta. */
    suspend fun countTransactionsByAccount(accountId: Long): Int

    /** Cuenta las suscripciones asociadas a una cuenta. */
    suspend fun countSubscriptionsByAccount(accountId: Long): Int

    /** Elimina todas las transacciones y suscripciones de una cuenta, luego la cuenta. */
    suspend fun deleteAccountWithCascade(account: Account)

    // --- Gestión de Respaldo ---

    /**
     * Obtiene todas las cuentas en su formato original de base de datos para respaldo.
     */
    suspend fun getAllAccountsRaw(): List<AccountEntity>

    /**
     * Obtiene todas las transacciones en su formato original de base de datos para respaldo.
     */
    suspend fun getAllTransactionsRaw(): List<TransactionEntity>

    /**
     * Obtiene todas las suscripciones en su formato original de base de datos para respaldo.
     */
    suspend fun getAllSubscriptionsRaw(): List<SubscriptionEntity>

    /**
     * Restaura los datos del respaldo reemplazando todas las tablas en una transacción Room.
     */
    suspend fun restoreBackup(
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>,
        subscriptions: List<SubscriptionEntity>
    )

    /**
     * Elimina absolutamente todos los datos de la base de datos (vaciar base de datos).
     */
    suspend fun clearAllData()
}
