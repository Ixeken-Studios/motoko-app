package com.ixeken.motoko.data.local

import android.content.Context
import com.ixeken.motoko.R

/**
 * Representa los diferentes tipos de billetera en los que se puede almacenar dinero.
 */
enum class WalletType {
    CASH,       // Efectivo
    BANK,       // Cuenta Bancaria / Débito
    SAVINGS     // Cuenta de Ahorros / Inversión
}

/**
 * Resuelve el nombre visible de una billetera basándose en los recursos localizados y la lista del usuario (walletsList).
 */
fun resolveWalletName(
    walletType: WalletType?,
    walletsList: List<String>,
    context: Context
): String {
    val cashDefault = context.getString(R.string.sub_bottom_sheet_option_cash)
    val cardDefault = context.getString(R.string.sub_bottom_sheet_option_debit)
    val savingsDefault = context.getString(R.string.sub_bottom_sheet_option_savings)

    if (walletType == null) return cashDefault

    return when (walletType) {
        WalletType.CASH -> {
            walletsList.firstOrNull { it.equals(cashDefault, ignoreCase = true) } ?: cashDefault
        }
        WalletType.SAVINGS -> {
            walletsList.firstOrNull { it.equals(savingsDefault, ignoreCase = true) } ?: savingsDefault
        }
        WalletType.BANK -> {
            walletsList.firstOrNull { it.equals(cardDefault, ignoreCase = true) }
                ?: walletsList.firstOrNull { 
                    !it.equals(cashDefault, ignoreCase = true) && !it.equals(savingsDefault, ignoreCase = true) 
                }
                ?: cardDefault
        }
    }
}
