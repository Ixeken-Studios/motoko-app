package com.ixeken.motoko.domain.model

import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.data.local.WalletType

/**
 * Representa una cuenta de usuario o perfil independiente.
 */
data class Account(
    val id: Long,
    val name: String
)

/**
 * Representa un movimiento financiero en la capa de negocio.
 */
data class Transaction(
    val id: Long,
    val accountId: Long,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val wallet: WalletType,
    val category: String,
    val iconName: String,
    val timestamp: Long,
    val note: String?,
    val receiptPath: String?
)

/**
 * Representa una suscripción periódica en la capa de negocio.
 */
data class Subscription(
    val id: Long,
    val accountId: Long,
    val name: String,
    val amount: Double,
    val wallet: WalletType,
    val billingPeriod: BillingPeriod,
    val startDate: Long,
    val iconName: String,
    val category: String,
    val note: String? = null
)
