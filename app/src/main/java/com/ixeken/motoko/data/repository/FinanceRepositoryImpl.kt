package com.ixeken.motoko.data.repository

import androidx.room.withTransaction
import com.ixeken.motoko.data.local.AppDatabase
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.local.dao.AccountDao
import com.ixeken.motoko.data.local.dao.SubscriptionDao
import com.ixeken.motoko.data.local.dao.TransactionDao
import com.ixeken.motoko.data.local.entity.AccountEntity
import com.ixeken.motoko.data.local.entity.SubscriptionEntity
import com.ixeken.motoko.data.local.entity.TransactionEntity
import com.ixeken.motoko.domain.model.Account
import com.ixeken.motoko.domain.model.Subscription
import com.ixeken.motoko.domain.model.Transaction
import com.ixeken.motoko.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.ixeken.motoko.data.local.BillingPeriod

/**
 * Implementación concreta del repositorio financiero de la aplicación.
 * Realiza los mapeos de base de datos a modelos de dominio.
 */
class FinanceRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val subscriptionDao: SubscriptionDao
) : FinanceRepository {

    private fun AccountEntity.toDomain() = Account(id, name)
    private fun Account.toEntity() = AccountEntity(id, name)

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        accountId = accountId,
        title = title,
        amount = amount,
        isIncome = isIncome,
        wallet = wallet,
        category = category,
        iconName = iconName,
        timestamp = timestamp,
        note = note,
        receiptPath = receiptPath
    )
    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        accountId = accountId,
        title = title,
        amount = amount,
        isIncome = isIncome,
        wallet = wallet,
        category = category,
        iconName = iconName,
        timestamp = timestamp,
        note = note,
        receiptPath = receiptPath
    )

    private fun SubscriptionEntity.toDomain() = Subscription(
        id = id,
        accountId = accountId,
        name = name,
        amount = amount,
        wallet = wallet,
        billingPeriod = billingPeriod,
        startDate = startDate,
        iconName = iconName,
        category = category,
        note = note
    )
    private fun Subscription.toEntity() = SubscriptionEntity(
        id = id,
        accountId = accountId,
        name = name,
        amount = amount,
        wallet = wallet,
        billingPeriod = billingPeriod,
        startDate = startDate,
        iconName = iconName,
        category = category,
        note = note
    )

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertAccount(name: String): Long {
        return accountDao.insertAccount(AccountEntity(name = name))
    }

    override suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account.toEntity())
    }

    /**
     * Proyecta dinámicamente y genera transacciones virtuales a partir de la fecha de inicio
     * y frecuencia de cada suscripción periódica activa del usuario.
     */
    private fun getSubscriptionTransactions(subscriptions: List<SubscriptionEntity>, now: Long): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        for (sub in subscriptions) {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = sub.startDate
            
            // Limpiar horas para consistencia en la comparación de fechas
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            
            val targetDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            var index = 0
            
            while (calendar.timeInMillis <= now) {
                val timestamp = calendar.timeInMillis
                val txId = -(sub.id * 1000000L + index)
                
                transactions.add(
                    Transaction(
                        id = txId,
                        accountId = sub.accountId,
                        title = sub.name,
                        amount = sub.amount,
                        isIncome = false,
                        wallet = sub.wallet,
                        category = "Subscriptions",
                        iconName = sub.iconName,
                        timestamp = timestamp,
                        note = sub.note,
                        receiptPath = null
                    )
                )
                
                index++
                when (sub.billingPeriod) {
                    BillingPeriod.MONTHLY -> {
                        calendar.add(java.util.Calendar.MONTH, 1)
                        val maxDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, java.lang.Math.min(targetDay, maxDay))
                    }
                    BillingPeriod.ANNUAL -> {
                        calendar.add(java.util.Calendar.YEAR, 1)
                        val maxDay = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, java.lang.Math.min(targetDay, maxDay))
                    }
                }
            }
        }
        return transactions
    }

    override fun getLatestTransactions(accountId: Long): Flow<List<Transaction>> {
        return getAllTransactions(accountId).map { list -> list.take(10) }
    }

    override fun getAllTransactions(accountId: Long): Flow<List<Transaction>> {
        return combine(
            transactionDao.getAllTransactions(accountId),
            subscriptionDao.getSubscriptionsByAccount(accountId)
        ) { txList, subList ->
            val realTxs = txList.map { it.toDomain() }
            val subTxs = getSubscriptionTransactions(subList, System.currentTimeMillis())
            (realTxs + subTxs).sortedWith(compareByDescending<Transaction> { it.timestamp }.thenByDescending { it.id })
        }
    }

    override fun getTotalBalance(accountId: Long): Flow<Double> {
        return combine(
            transactionDao.getTotalBalance(accountId),
            subscriptionDao.getSubscriptionsByAccount(accountId)
        ) { dbBalance, subList ->
            val dbVal = dbBalance ?: 0.0
            val subTxs = getSubscriptionTransactions(subList, System.currentTimeMillis())
            val subTotalExpense = subTxs.sumOf { it.amount }
            dbVal - subTotalExpense
        }
    }

    override fun getBalanceByWallet(accountId: Long, wallet: WalletType): Flow<Double> {
        return combine(
            transactionDao.getBalanceByWallet(accountId, wallet),
            subscriptionDao.getSubscriptionsByAccount(accountId)
        ) { dbBalance, subList ->
            val dbVal = dbBalance ?: 0.0
            val walletSubs = subList.filter { it.wallet == wallet }
            val subTxs = getSubscriptionTransactions(walletSubs, System.currentTimeMillis())
            val subTotalExpense = subTxs.sumOf { it.amount }
            dbVal - subTotalExpense
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override fun getSubscriptions(accountId: Long): Flow<List<Subscription>> {
        return subscriptionDao.getSubscriptionsByAccount(accountId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription.toEntity())
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription.toEntity())
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun getSubscriptionById(id: Long): Subscription? {
        return subscriptionDao.getSubscriptionById(id)?.toDomain()
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun deleteAccounts(accounts: List<Account>) {
        accountDao.deleteAccounts(accounts.map { it.toEntity() })
    }

    override suspend fun reassignTransactionsCategory(oldCategory: String, newCategory: String) {
        transactionDao.reassignCategory(oldCategory, newCategory)
    }

    override suspend fun reassignSubscriptionsCategory(oldCategory: String, newCategory: String) {
        subscriptionDao.reassignCategory(oldCategory, newCategory)
    }

    override suspend fun countTransactionsByWallet(wallet: WalletType): Int {
        return transactionDao.countByWallet(wallet)
    }

    override suspend fun countSubscriptionsByWallet(wallet: WalletType): Int {
        return subscriptionDao.countByWallet(wallet)
    }

    override suspend fun countTransactionsByAccount(accountId: Long): Int {
        return transactionDao.countByAccountId(accountId)
    }

    override suspend fun countSubscriptionsByAccount(accountId: Long): Int {
        return subscriptionDao.countByAccountId(accountId)
    }

    override suspend fun deleteAccountWithCascade(account: Account) {
        val entity = account.toEntity()
        transactionDao.deleteByAccountId(entity.id)
        subscriptionDao.deleteByAccountId(entity.id)
        accountDao.deleteAccount(entity)
    }

    override suspend fun getAllAccountsRaw(): List<AccountEntity> {
        return accountDao.getAllAccountsRaw()
    }

    override suspend fun getAllTransactionsRaw(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsRaw()
    }

    override suspend fun getAllSubscriptionsRaw(): List<SubscriptionEntity> {
        return subscriptionDao.getAllSubscriptionsRaw()
    }

    override suspend fun restoreBackup(
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>,
        subscriptions: List<SubscriptionEntity>
    ) {
        database.withTransaction {
            transactionDao.deleteAllTransactions()
            subscriptionDao.deleteAllSubscriptions()
            accountDao.deleteAllAccounts()

            accountDao.insertAccounts(accounts)
            transactionDao.insertTransactions(transactions)
            subscriptionDao.insertSubscriptions(subscriptions)
        }
    }

    override suspend fun clearAllData() {
        database.withTransaction {
            transactionDao.deleteAllTransactions()
            subscriptionDao.deleteAllSubscriptions()
            accountDao.deleteAllAccounts()
        }
    }
}
