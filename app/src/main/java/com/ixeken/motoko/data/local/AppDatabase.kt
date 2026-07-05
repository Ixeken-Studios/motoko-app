package com.ixeken.motoko.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ixeken.motoko.data.local.dao.AccountDao
import com.ixeken.motoko.data.local.dao.SubscriptionDao
import com.ixeken.motoko.data.local.dao.TransactionDao
import com.ixeken.motoko.data.local.entity.AccountEntity
import com.ixeken.motoko.data.local.entity.SubscriptionEntity
import com.ixeken.motoko.data.local.entity.TransactionEntity

/**
 * Base de datos principal de Room de la aplicación Motoko.
 */
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        SubscriptionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun subscriptionDao(): SubscriptionDao
}
