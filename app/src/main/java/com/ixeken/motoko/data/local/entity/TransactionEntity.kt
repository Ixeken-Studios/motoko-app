package com.ixeken.motoko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ixeken.motoko.data.local.WalletType

/**
 * Entidad que representa un movimiento financiero (ingreso o gasto).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
