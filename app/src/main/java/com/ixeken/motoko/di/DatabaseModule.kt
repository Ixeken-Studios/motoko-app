package com.ixeken.motoko.di

import android.content.Context
import androidx.room.Room
import com.ixeken.motoko.data.local.AppDatabase
import com.ixeken.motoko.data.local.dao.AccountDao
import com.ixeken.motoko.data.local.dao.SubscriptionDao
import com.ixeken.motoko.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para la inyección de dependencias relacionadas con la base de datos Room.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "motoko.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
}
