package com.ixeken.motoko.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ixeken.motoko.data.local.WalletType
import com.ixeken.motoko.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de acceso a datos (DAO) para interactuar con la tabla de suscripciones.
 */
@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions WHERE accountId = :accountId")
    fun getSubscriptionsByAccount(accountId: Long): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    suspend fun getSubscriptionById(id: Long): SubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET category = :newCategory WHERE category = :oldCategory")
    suspend fun reassignCategory(oldCategory: String, newCategory: String)

    @Query("SELECT COUNT(*) FROM subscriptions WHERE accountId = :accountId")
    suspend fun countByAccountId(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM subscriptions WHERE wallet = :wallet")
    suspend fun countByWallet(wallet: WalletType): Int

    @Query("DELETE FROM subscriptions WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: Long)

    @Query("SELECT * FROM subscriptions")
    suspend fun getAllSubscriptionsRaw(): List<SubscriptionEntity>

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAllSubscriptions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<SubscriptionEntity>)
}
