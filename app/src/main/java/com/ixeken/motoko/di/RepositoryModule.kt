package com.ixeken.motoko.di

import com.ixeken.motoko.data.repository.FinanceRepositoryImpl
import com.ixeken.motoko.domain.repository.FinanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para enlazar los contratos de repositorio con sus respectivas implementaciones.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFinanceRepository(
        repositoryImpl: FinanceRepositoryImpl
    ): FinanceRepository
}
