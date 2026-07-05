package com.ixeken.motoko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ixeken.motoko.data.local.BillingPeriod
import com.ixeken.motoko.data.local.WalletType

/**
 * Entidad que representa una suscripción periódica recurrente.
 */
@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
