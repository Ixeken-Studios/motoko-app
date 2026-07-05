package com.ixeken.motoko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de acceso a datos (DAO) para interactuar con la tabla de transacciones.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC, id DESC LIMIT 10")
    fun getLatestTransactions(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY timestamp DESC, id DESC")
    fun getAllTransactions(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(CASE WHEN isIncome = 1 THEN amount ELSE -amount END) FROM transactions WHERE accountId = :accountId")
    fun getTotalBalance(accountId: Long): Flow<Double?>

    @Query("SELECT SUM(CASE WHEN isIncome = 1 THEN amount ELSE -amount END) FROM transactions WHERE accountId = :accountId AND wallet = :wallet")
    fun getBalanceByWallet(accountId: Long, wallet: WalletType): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun reassignCategory(oldCategory: String, newCategory: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countByAccountId(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE wallet = :wallet")
    suspend fun countByWallet(wallet: WalletType): Int

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Long)

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsRaw(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
}
