package com.ixeken.motoko.data.local

/**
 * Representa los diferentes tipos de billetera en los que se puede almacenar dinero.
 */
enum class WalletType {
    CASH,       // Efectivo
    BANK,       // Cuenta Bancaria / Débito
    SAVINGS     // Cuenta de Ahorros / Inversión
}
