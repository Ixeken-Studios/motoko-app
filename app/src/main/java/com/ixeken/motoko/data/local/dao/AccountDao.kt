package com.ixeken.motoko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.motoko.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de acceso a datos (DAO) para interactuar con la tabla de cuentas.
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccounts(accounts: List<AccountEntity>)

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsRaw(): List<AccountEntity>
}
