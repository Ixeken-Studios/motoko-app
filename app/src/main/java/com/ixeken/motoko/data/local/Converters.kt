package com.ixeken.motoko.data.local

import androidx.room.TypeConverter

/**
 * Conversores de tipos de Room para permitir almacenar enums en la base de datos SQLite.
 */
class Converters {
    @TypeConverter
    fun toWalletType(value: String): WalletType = WalletType.valueOf(value)

    @TypeConverter
    fun fromWalletType(value: WalletType): String = value.name

    @TypeConverter
    fun toBillingPeriod(value: String): BillingPeriod = BillingPeriod.valueOf(value)

    @TypeConverter
    fun fromBillingPeriod(value: BillingPeriod): String = value.name
}
