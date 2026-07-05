package com.ixeken.motoko.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una cuenta de usuario independiente (perfil o contexto).
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
